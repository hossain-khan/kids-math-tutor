/**
 * Cloudflare Workers API for shared worksheets
 * Handles creating, listing, and managing shared math worksheets
 */

import { Hono } from 'hono';
import { nanoid } from 'nanoid';
import type { Context } from 'hono';
import type { GradeLevel, ProblemSpec } from '../lib/schemas/challenge-schema';
import { ExplicitChallengeSpecSchema } from '../lib/schemas/challenge-schema';
import { validateWorksheetContent } from '../lib/server/profanity';
import { detectGrades } from '../lib/server/grades';
import {
  saveWorksheet,
  getWorksheet,
  listWorksheets,
  incrementViews,
  incrementDownloads,
  type SharedWorksheet,
} from '../lib/server/worksheetStorage';
import {
  checkRateLimit,
  incrementShareCount,
  type RateLimitContext,
} from '../lib/server/rateLimiter';

interface Env {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  WORKSHEETS_KV: any; // KVNamespace from Cloudflare Workers
}

const app = new Hono<{ Bindings: Env }>();

/**
 * Helper to get client IP from request
 */
function getClientIp(c: Context<{ Bindings: Env }>): string {
  return (
    c.req.header('CF-Connecting-IP') ||
    c.req.header('X-Forwarded-For') ||
    'unknown'
  );
}

/**
 * POST /api/v1/worksheets/share
 * Share a custom worksheet to the community
 */
app.post('/api/v1/worksheets/share', async (c) => {
  try {
    // Get request body
    const body = await c.req.json();

    // Validate worksheet structure
    if (
      !body.title ||
      !Array.isArray(body.problems) ||
      body.problems.length === 0
    ) {
      return c.json(
        {
          error: 'Invalid worksheet: title and problems are required',
        },
        400,
      );
    }

    // Type casting for explicit challenge
    const worksheetData = {
      type: 'explicit' as const,
      title: body.title,
      subtitle: body.subtitle,
      description: body.description,
      problems: body.problems as ProblemSpec[],
    };

    // Validate against schema
    try {
      ExplicitChallengeSpecSchema.parse(worksheetData);
    } catch (error) {
      return c.json(
        {
          error: 'Invalid worksheet data format',
          details: error instanceof Error ? error.message : 'Unknown error',
        },
        400,
      );
    }

    // Check for profanity
    if (
      validateWorksheetContent({
        title: worksheetData.title,
        subtitle: worksheetData.subtitle,
        description: worksheetData.description,
      })
    ) {
      return c.json(
        {
          error:
            'Worksheet contains inappropriate content. Please review and try again.',
        },
        400,
      );
    }

    // Check rate limit
    const clientIp = getClientIp(c);
    const rateLimitContext: RateLimitContext = {
      env: { WORKSHEETS_KV: c.env.WORKSHEETS_KV },
      clientIp,
    };

    const rateLimit = await checkRateLimit(rateLimitContext);
    if (!rateLimit.allowed) {
      return c.json(
        {
          error: 'Daily share limit reached. You can share up to 10 worksheets per day.',
          remaining: rateLimit.remaining,
          resetTime: rateLimit.resetTime,
        },
        429,
      );
    }

    // Create shared worksheet
    const id = nanoid(12);
    const now = new Date().toISOString();

    const sharedWorksheet: SharedWorksheet = {
      id,
      type: 'explicit',
      title: worksheetData.title,
      subtitle: worksheetData.subtitle,
      description: worksheetData.description,
      grades: detectGrades(worksheetData.problems),
      problems: worksheetData.problems,
      createdAt: now,
      stats: {
        views: 0,
        downloads: 0,
      },
    };

    // Save to KV
    await saveWorksheet(
      { env: { WORKSHEETS_KV: c.env.WORKSHEETS_KV } },
      sharedWorksheet,
    );

    // Increment share count
    await incrementShareCount(rateLimitContext);

    return c.json(
      {
        id,
        shareUrl: `/worksheets/${id}`,
        shareLink: `${c.req.url.split('/api')[0]}/worksheets/${id}`,
      },
      201,
    );
  } catch (error) {
    console.error('Share worksheet error:', error);
    return c.json(
      {
        error: 'Failed to share worksheet',
        details: error instanceof Error ? error.message : 'Unknown error',
      },
      500,
    );
  }
});

/**
 * GET /api/v1/worksheets
 * List shared worksheets with optional filters
 */
app.get('/api/v1/worksheets', async (c) => {
  try {
    const grades = c.req.query('grades')?.split(',') as GradeLevel[] | undefined;
    const sortBy = (c.req.query('sort') ||
      'newest') as 'newest' | 'views' | 'downloads';

    const worksheets = await listWorksheets(
      { env: { WORKSHEETS_KV: c.env.WORKSHEETS_KV } },
      {
        grades,
        sortBy,
      },
    );

    return c.json(worksheets);
  } catch (error) {
    console.error('List worksheets error:', error);
    return c.json(
      {
        error: 'Failed to list worksheets',
        details: error instanceof Error ? error.message : 'Unknown error',
      },
      500,
    );
  }
});

/**
 * GET /api/v1/worksheets/:id
 * Get a single worksheet by ID
 */
app.get('/api/v1/worksheets/:id', async (c) => {
  try {
    const id = c.req.param('id');

    const worksheet = await getWorksheet(
      { env: { WORKSHEETS_KV: c.env.WORKSHEETS_KV } },
      id,
    );

    if (!worksheet) {
      return c.json(
        {
          error: 'Worksheet not found',
        },
        404,
      );
    }

    // Increment view count (async, don't await)
    incrementViews(
      { env: { WORKSHEETS_KV: c.env.WORKSHEETS_KV } },
      id,
    ).catch((err) => console.error('Failed to increment views:', err));

    return c.json(worksheet);
  } catch (error) {
    console.error('Get worksheet error:', error);
    return c.json(
      {
        error: 'Failed to get worksheet',
        details: error instanceof Error ? error.message : 'Unknown error',
      },
      500,
    );
  }
});

/**
 * POST /api/v1/worksheets/:id/download
 * Track worksheet download/usage
 */
app.post('/api/v1/worksheets/:id/download', async (c) => {
  try {
    const id = c.req.param('id');

    const worksheet = await getWorksheet(
      { env: { WORKSHEETS_KV: c.env.WORKSHEETS_KV } },
      id,
    );

    if (!worksheet) {
      return c.json(
        {
          error: 'Worksheet not found',
        },
        404,
      );
    }

    // Increment download count
    await incrementDownloads(
      { env: { WORKSHEETS_KV: c.env.WORKSHEETS_KV } },
      id,
    );

    return c.json({ success: true });
  } catch (error) {
    console.error('Download tracking error:', error);
    return c.json(
      {
        error: 'Failed to track download',
        details: error instanceof Error ? error.message : 'Unknown error',
      },
      500,
    );
  }
});

/**
 * Health check endpoint
 */
app.get('/api/health', (c) => {
  return c.json({ status: 'ok' });
});

export default app;
