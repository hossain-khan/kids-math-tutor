/**
 * Shared worksheet storage and retrieval from Cloudflare KV.
 */

import type { ProblemSpec, GradeLevel } from '@/lib/schemas/challenge-schema';

export interface SharedWorksheet {
  id: string;
  type: 'explicit'; // Only custom worksheets
  title: string;
  subtitle?: string;
  description?: string;
  grades: GradeLevel[];
  problems: ProblemSpec[];
  createdAt: string; // ISO timestamp
  stats: {
    views: number;
    downloads: number;
  };
}

export interface WorksheetListItem {
  id: string;
  title: string;
  subtitle?: string;
  grades: GradeLevel[];
  problemCount: number;
  createdAt: string;
  stats: {
    views: number;
    downloads: number;
  };
}

export interface KVContext {
  env: {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    WORKSHEETS_KV: any; // KVNamespace from @cloudflare/workers-types
  };
}

/**
 * Save a shared worksheet to KV
 */
export async function saveWorksheet(
  context: KVContext,
  worksheet: SharedWorksheet,
): Promise<void> {
  const key = `worksheet:${worksheet.id}`;
  await context.env.WORKSHEETS_KV.put(key, JSON.stringify(worksheet));
}

/**
 * Get a worksheet by ID
 */
export async function getWorksheet(
  context: KVContext,
  id: string,
): Promise<SharedWorksheet | null> {
  try {
    const key = `worksheet:${id}`;
    const data = await context.env.WORKSHEETS_KV.get(key, 'json');
    return data as SharedWorksheet | null;
  } catch (error) {
    console.error('Failed to get worksheet:', error);
    return null;
  }
}

/**
 * Get list of worksheets with optional grade filter
 * Note: This is a simplified implementation. For production, consider:
 * - Using a separate index in KV for faster filtering
 * - Pagination
 * - Caching popular worksheets
 */
export async function listWorksheets(
  context: KVContext,
  filters?: {
    grades?: GradeLevel[];
    sortBy?: 'newest' | 'views' | 'downloads';
  },
): Promise<WorksheetListItem[]> {
  try {
    const worksheets: WorksheetListItem[] = [];

    // List all worksheet keys
    const listResult = await context.env.WORKSHEETS_KV.list({
      prefix: 'worksheet:',
    });

    // Fetch each worksheet
    for (const key of listResult.keys) {
      const data = await context.env.WORKSHEETS_KV.get(key.name, 'json');
      if (data) {
        const worksheet = data as SharedWorksheet;

        // Apply grade filter if provided
        if (filters?.grades && filters.grades.length > 0) {
          const hasMatchingGrade = worksheet.grades.some((grade) =>
            filters.grades!.includes(grade),
          );
          if (!hasMatchingGrade) continue;
        }

        worksheets.push({
          id: worksheet.id,
          title: worksheet.title,
          subtitle: worksheet.subtitle,
          grades: worksheet.grades,
          problemCount: worksheet.problems.length,
          createdAt: worksheet.createdAt,
          stats: worksheet.stats,
        });
      }
    }

    // Sort results
    const sortBy = filters?.sortBy || 'newest';
    worksheets.sort((a, b) => {
      switch (sortBy) {
        case 'views':
          return b.stats.views - a.stats.views;
        case 'downloads':
          return b.stats.downloads - a.stats.downloads;
        case 'newest':
        default:
          return (
            new Date(b.createdAt).getTime() -
            new Date(a.createdAt).getTime()
          );
      }
    });

    return worksheets;
  } catch (error) {
    console.error('Failed to list worksheets:', error);
    return [];
  }
}

/**
 * Increment view count for a worksheet
 */
export async function incrementViews(
  context: KVContext,
  id: string,
): Promise<void> {
  try {
    const worksheet = await getWorksheet(context, id);
    if (worksheet) {
      worksheet.stats.views += 1;
      await saveWorksheet(context, worksheet);
    }
  } catch (error) {
    console.error('Failed to increment views:', error);
  }
}

/**
 * Increment download count for a worksheet
 */
export async function incrementDownloads(
  context: KVContext,
  id: string,
): Promise<void> {
  try {
    const worksheet = await getWorksheet(context, id);
    if (worksheet) {
      worksheet.stats.downloads += 1;
      await saveWorksheet(context, worksheet);
    }
  } catch (error) {
    console.error('Failed to increment downloads:', error);
  }
}
