/**
 * Cloudflare Workers API for shared worksheets
 * Handles creating, listing, and managing shared math worksheets
 */

import { Hono } from 'hono';
import { nanoid } from 'nanoid';
import type { Context } from 'hono';
import type { GradeLevel, ProblemSpec } from '@/lib/schemas/challenge-schema';
import { ExplicitChallengeSpecSchema } from '@/lib/schemas/challenge-schema';
import { checkContentSafetyWithAI } from '@/lib/server/aiSafety';
import { bulkCheckSafety } from '@/lib/server/adminSafetyCheck';
import { detectGrades } from '@/lib/server/grades';
import {
  saveWorksheet,
  getWorksheet,
  listWorksheets,
  searchWorksheets,
  rateWorksheet,
  calculateWorksheetRatingStats,
  incrementViews,
  incrementDownloads,
  type SharedWorksheet,
} from '@/lib/server/worksheetStorage';
import {
  checkRateLimit,
  incrementShareCount,
  type RateLimitContext,
} from '@/lib/server/rateLimiter';

interface Env {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  KV: any; // KVNamespace from Cloudflare Workers
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  AI?: any; // Cloudflare Workers AI binding (optional)
  ADMIN_PASSWORD?: string; // Admin password from environment variables
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
 * Helper to verify admin token
 * Tokens are valid for 24 hours
 */
function verifyAdminToken(
  c: Context<{ Bindings: Env }>,
  password: string,
): string {
  const adminPassword = c.env.ADMIN_PASSWORD;
  if (!adminPassword || password !== adminPassword) {
    throw new Error('Invalid password');
  }

  // Create a simple token that includes timestamp
  // In production, use JWT or similar
  const expiryTime = Date.now() + 24 * 60 * 60 * 1000; // 24 hours
  const tokenStr = `${password}:${expiryTime}`;
  const token = btoa(tokenStr); // Use btoa instead of Buffer.from
  return token;
}

/**
 * Helper to validate admin token from request
 * Supports both environment variable (prod secrets) and var fallback (dev)
 */
function validateAdminToken(
  c: Context<{ Bindings: Env }>,
): boolean {
  const authHeader = c.req.header('Authorization');
  if (!authHeader?.startsWith('Bearer ')) {
    return false;
  }

  // Get admin password from env (secrets) or fall back to vars
  const adminPassword = c.env.ADMIN_PASSWORD;
  if (!adminPassword) {
    console.error('ADMIN_PASSWORD not configured in environment or secrets');
    return false;
  }

  const token = authHeader.slice(7);
  try {
    const decoded = atob(token); // Use atob instead of Buffer.from
    const [password, expiryStr] = decoded.split(':');
    const expiry = parseInt(expiryStr, 10);

    // Check if token has expired
    if (Date.now() > expiry) {
      return false;
    }

    // Verify password matches
    return password === adminPassword;
  } catch {
    return false;
  }
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

    // Check for inappropriate content using AI with fallback to bad-words
    const safetyResult = await checkContentSafetyWithAI(c.env, {
      title: worksheetData.title,
      subtitle: worksheetData.subtitle,
      description: worksheetData.description,
    });

    if (!safetyResult.safe) {
      return c.json(
        {
          error:
            'Worksheet contains inappropriate content for children. Please review and try again.',
          categories: safetyResult.categories || [],
          explanation: safetyResult.explanation,
          suggestion:
            'Please ensure all content is age-appropriate for K-2 children.',
          method: safetyResult.usingAI ? 'AI-based safety check' : 'Pattern-based filter',
          fallback: safetyResult.fallback || false,
        },
        400,
      );
    }

    // Check rate limit
    const clientIp = getClientIp(c);
    const rateLimitContext: RateLimitContext = {
      env: { KV: c.env.KV },
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
        averageRating: 0,
        ratingCount: 0,
      },
    };

    // Save to KV
    await saveWorksheet(
      { env: { KV: c.env.KV } },
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
      'newest') as 'newest' | 'views' | 'downloads' | 'ratings';
    const limit = parseInt(c.req.query('limit') || '20', 10);
    const offset = parseInt(c.req.query('offset') || '0', 10);

    const result = await listWorksheets(
      { env: { KV: c.env.KV } },
      {
        grades,
        sortBy,
        limit: Math.min(limit, 100), // Cap at 100
        offset,
      },
    );

    return c.json(result);
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
 * GET /api/v1/worksheets/search
 * Search worksheets by keyword with optional filters
 */
app.get('/api/v1/worksheets/search', async (c) => {
  try {
    const q = c.req.query('q');
    if (!q) {
      return c.json(
        {
          error: 'Search query (q) is required',
        },
        400,
      );
    }

    const grades = c.req.query('grades')?.split(',') as GradeLevel[] | undefined;
    const sortBy = (c.req.query('sort') ||
      'newest') as 'newest' | 'views' | 'downloads' | 'ratings';
    const limit = parseInt(c.req.query('limit') || '20', 10);
    const offset = parseInt(c.req.query('offset') || '0', 10);

    const result = await searchWorksheets(
      { env: { KV: c.env.KV } },
      q,
      {
        grades,
        sortBy,
        limit: Math.min(limit, 100), // Cap at 100
        offset,
      },
    );

    return c.json(result);
  } catch (error) {
    console.error('Search worksheets error:', error);
    return c.json(
      {
        error: 'Failed to search worksheets',
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
      { env: { KV: c.env.KV } },
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

    // Increment view count and fetch updated worksheet
    await incrementViews(
      { env: { KV: c.env.KV } },
      id,
    ).catch((err) => console.error('Failed to increment views:', err));

    // Fetch the updated worksheet with new view count
    const updatedWorksheet = await getWorksheet(
      { env: { KV: c.env.KV } },
      id,
    );

    return c.json(updatedWorksheet || worksheet);
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
      { env: { KV: c.env.KV } },
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
      { env: { KV: c.env.KV } },
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
 * POST /api/v1/worksheets/:id/rate
 * Rate a worksheet (1-5 stars)
 */
app.post('/api/v1/worksheets/:id/rate', async (c) => {
  try {
    const id = c.req.param('id');
    const body = await c.req.json();

    const worksheet = await getWorksheet(
      { env: { KV: c.env.KV } },
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

    const { rating, sessionId } = body;

    if (!rating || !sessionId) {
      return c.json(
        {
          error: 'Rating and sessionId are required',
        },
        400,
      );
    }

    if (typeof rating !== 'number' || rating < 1 || rating > 5) {
      return c.json(
        {
          error: 'Rating must be a number between 1 and 5',
        },
        400,
      );
    }

    // Save rating
    const success = await rateWorksheet(
      { env: { KV: c.env.KV } },
      id,
      rating,
      sessionId,
    );

    if (!success) {
      return c.json(
        {
          error: 'Failed to save rating',
        },
        500,
      );
    }

    // Get updated stats
    const stats = await calculateWorksheetRatingStats(
      { env: { KV: c.env.KV } },
      id,
    );

    return c.json(
      {
        success: true,
        stats,
      },
      201,
    );
  } catch (error) {
    console.error('Rating error:', error);
    return c.json(
      {
        error: 'Failed to rate worksheet',
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

/**
 * ADMIN ENDPOINTS
 * Protected endpoints for managing worksheets
 */

/**
 * POST /api/v1/admin/auth
 * Authenticate and get a token for admin access
 */
app.post('/api/v1/admin/auth', async (c) => {
  try {
    const body = await c.req.json();
    const { password } = body;

    if (!password) {
      return c.json(
        {
          error: 'Password is required',
        },
        400,
      );
    }

    const token = verifyAdminToken(c, password);
    const expiry = Date.now() + 24 * 60 * 60 * 1000;

    return c.json(
      {
        token,
        expiry,
        message: 'Authentication successful',
      },
      200,
    );
  } catch (error) {
    return c.json(
      {
        error: error instanceof Error ? error.message : 'Authentication failed',
      },
      401,
    );
  }
});

/**
 * GET /api/v1/admin/worksheets
 * List all shared worksheets (admin only)
 */
app.get('/api/v1/admin/worksheets', async (c) => {
  try {
    if (!validateAdminToken(c)) {
      return c.json(
        {
          error: 'Unauthorized',
        },
        401,
      );
    }

    const result = await listWorksheets(
      { env: { KV: c.env.KV } },
      {
        sortBy: 'newest',
        limit: 1000,
        offset: 0,
      },
    );

    return c.json({
      worksheets: result.items,
      total: result.total,
    });
  } catch (error) {
    console.error('Admin list worksheets error:', error);
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
 * DELETE /api/v1/admin/worksheets/:id
 * Delete a worksheet (admin only)
 */
app.delete('/api/v1/admin/worksheets/:id', async (c) => {
  try {
    if (!validateAdminToken(c)) {
      return c.json(
        {
          error: 'Unauthorized',
        },
        401,
      );
    }

    const id = c.req.param('id');

    // Check if worksheet exists
    const worksheet = await getWorksheet(
      { env: { KV: c.env.KV } },
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

    // Delete the worksheet
    await c.env.KV.delete(`worksheet:${id}`);

    // Also delete associated ratings
    await c.env.KV.delete(`ratings:${id}`);

    return c.json({
      success: true,
      message: 'Worksheet deleted successfully',
    });
  } catch (error) {
    console.error('Admin delete worksheet error:', error);
    return c.json(
      {
        error: 'Failed to delete worksheet',
        details: error instanceof Error ? error.message : 'Unknown error',
      },
      500,
    );
  }
});

/**
 * POST /api/v1/admin/check-safety
 * Perform bulk AI safety check on shared worksheets (admin only)
 */
app.post('/api/v1/admin/check-safety', async (c) => {
  try {
    if (!validateAdminToken(c)) {
      return c.json(
        {
          error: 'Unauthorized',
        },
        401,
      );
    }

    console.log(
      `[DEBUG] check-safety endpoint. c.env keys: ${Object.keys(c.env).join(', ')}`,
    );
    console.log(`[DEBUG] AI binding in c.env: ${!!c.env.AI}`);

    const body = await c.req.json();
    const worksheetIds = body.worksheetIds as string[] | undefined;

    // Perform bulk safety check
    const result = await bulkCheckSafety(c.env, worksheetIds);

    return c.json(result);
  } catch (error) {
    console.error('[ERROR] Admin safety check error:', error);
    return c.json(
      {
        error: 'Failed to perform safety check',
        details: error instanceof Error ? error.message : 'Unknown error',
      },
      500,
    );
  }
});

export default app;
