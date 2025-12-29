/**
 * Shared worksheet storage and retrieval from Cloudflare KV.
 */

import type { ProblemSpec, GradeLevel } from "@/lib/schemas/challenge-schema";

export interface SharedWorksheet {
  id: string;
  type: "explicit"; // Only custom worksheets
  title: string;
  subtitle?: string;
  description?: string;
  grades: GradeLevel[];
  problems: ProblemSpec[];
  createdAt: string; // ISO timestamp
  creatorSessionId?: string; // Session ID of the creator (for deletion permission)
  stats: {
    views: number;
    downloads: number;
    averageRating: number;
    ratingCount: number;
  };
}

export interface WorksheetListItem {
  id: string;
  title: string;
  subtitle?: string;
  grades: GradeLevel[];
  problemCount: number;
  singleOperation?: string; // If all problems share the same operation
  createdAt: string;
  creatorSessionId?: string; // Session ID of the creator (for deletion permission)
  stats: {
    views: number;
    downloads: number;
    averageRating: number;
    ratingCount: number;
  };
}

export interface PaginatedResults<T> {
  items: T[];
  total: number;
  limit: number;
  offset: number;
  hasMore: boolean;
}

export interface KVContext {
  env: {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    KV: any; // KVNamespace from @cloudflare/workers-types
  };
}

/**
 * Determine if all problems in a worksheet share the same operation
 * Returns the operation type if all are the same, otherwise returns undefined
 */
function getSingleOperationType(problems: ProblemSpec[]): string | undefined {
  if (problems.length === 0) return undefined;

  const firstOperation = problems[0].operation;
  const allSame = problems.every((p) => p.operation === firstOperation);

  return allSame ? firstOperation : undefined;
}

/**
 * Save a shared worksheet to KV
 */
export async function saveWorksheet(
  context: KVContext,
  worksheet: SharedWorksheet,
): Promise<void> {
  const key = `worksheet:${worksheet.id}`;
  await context.env.KV.put(key, JSON.stringify(worksheet));
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
    const data = await context.env.KV.get(key, "json");
    return data as SharedWorksheet | null;
  } catch (error) {
    console.error("Failed to get worksheet:", error);
    return null;
  }
}

/**
 * Get list of worksheets with optional grade filter and pagination
 * Note: This is a simplified implementation. For production, consider:
 * - Using a separate index in KV for faster filtering
 * - Caching popular worksheets
 */
export async function listWorksheets(
  context: KVContext,
  filters?: {
    grades?: GradeLevel[];
    sortBy?: "newest" | "views" | "downloads" | "ratings";
    limit?: number;
    offset?: number;
  },
): Promise<PaginatedResults<WorksheetListItem>> {
  try {
    const worksheets: WorksheetListItem[] = [];
    const limit = filters?.limit || 20;
    const offset = filters?.offset || 0;

    // List all worksheet keys
    const listResult = await context.env.KV.list({
      prefix: "worksheet:",
    });

    // Fetch each worksheet
    for (const key of listResult.keys) {
      const data = await context.env.KV.get(key.name, "json");
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
          singleOperation: getSingleOperationType(worksheet.problems),
          createdAt: worksheet.createdAt,
          creatorSessionId: worksheet.creatorSessionId,
          stats: worksheet.stats,
        });
      }
    }

    // Sort results
    const sortBy = filters?.sortBy || "newest";
    worksheets.sort((a, b) => {
      switch (sortBy) {
        case "views":
          return b.stats.views - a.stats.views;
        case "downloads":
          return b.stats.downloads - a.stats.downloads;
        case "ratings":
          return b.stats.averageRating - a.stats.averageRating;
        case "newest":
        default:
          return (
            new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
          );
      }
    });

    // Apply pagination
    const total = worksheets.length;
    const paginatedItems = worksheets.slice(offset, offset + limit);
    const hasMore = offset + limit < total;

    return {
      items: paginatedItems,
      total,
      limit,
      offset,
      hasMore,
    };
  } catch (error) {
    console.error("Failed to list worksheets:", error);
    return {
      items: [],
      total: 0,
      limit: 20,
      offset: 0,
      hasMore: false,
    };
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
    console.error("Failed to increment views:", error);
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
    console.error("Failed to increment downloads:", error);
  }
}

/**
 * Search worksheets by keyword in title, subtitle, description
 */
export async function searchWorksheets(
  context: KVContext,
  searchQuery: string,
  filters?: {
    grades?: GradeLevel[];
    sortBy?: "newest" | "views" | "downloads" | "ratings";
    limit?: number;
    offset?: number;
  },
): Promise<PaginatedResults<WorksheetListItem>> {
  try {
    const query = searchQuery.toLowerCase().trim();
    if (!query) {
      return listWorksheets(context, filters);
    }

    const worksheets: WorksheetListItem[] = [];

    // List all worksheet keys
    const listResult = await context.env.KV.list({
      prefix: "worksheet:",
    });

    // Fetch and search each worksheet
    for (const key of listResult.keys) {
      const data = await context.env.KV.get(key.name, "json");
      if (data) {
        const worksheet = data as SharedWorksheet;

        // Check if search term matches title, subtitle, or description
        const matchesSearch =
          worksheet.title.toLowerCase().includes(query) ||
          worksheet.subtitle?.toLowerCase().includes(query) ||
          worksheet.description?.toLowerCase().includes(query);

        if (!matchesSearch) continue;

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
          singleOperation: getSingleOperationType(worksheet.problems),
          createdAt: worksheet.createdAt,
          creatorSessionId: worksheet.creatorSessionId,
          stats: worksheet.stats,
        });
      }
    }

    // Sort results
    const sortBy = filters?.sortBy || "newest";
    worksheets.sort((a, b) => {
      switch (sortBy) {
        case "views":
          return b.stats.views - a.stats.views;
        case "downloads":
          return b.stats.downloads - a.stats.downloads;
        case "ratings":
          return b.stats.averageRating - a.stats.averageRating;
        case "newest":
        default:
          return (
            new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
          );
      }
    });

    // Apply pagination
    const limit = filters?.limit || 20;
    const offset = filters?.offset || 0;
    const total = worksheets.length;
    const paginatedItems = worksheets.slice(offset, offset + limit);
    const hasMore = offset + limit < total;

    return {
      items: paginatedItems,
      total,
      limit,
      offset,
      hasMore,
    };
  } catch (error) {
    console.error("Failed to search worksheets:", error);
    return {
      items: [],
      total: 0,
      limit: 20,
      offset: 0,
      hasMore: false,
    };
  }
}

/**
 * Rate a worksheet (1-5 stars)
 * Prevents duplicate ratings from same session
 */
export async function rateWorksheet(
  context: KVContext,
  worksheetId: string,
  rating: number,
  sessionId: string,
): Promise<boolean> {
  try {
    if (rating < 1 || rating > 5) {
      throw new Error("Rating must be between 1 and 5");
    }

    const ratingKey = `rating:${worksheetId}:${sessionId}`;
    await context.env.KV.put(
      ratingKey,
      JSON.stringify({
        rating,
        timestamp: new Date().toISOString(),
      }),
    );

    // Update worksheet stats
    const worksheet = await getWorksheet(context, worksheetId);
    if (worksheet) {
      const stats = await calculateWorksheetRatingStats(context, worksheetId);
      worksheet.stats.averageRating = stats.averageRating;
      worksheet.stats.ratingCount = stats.ratingCount;
      await saveWorksheet(context, worksheet);
    }

    return true;
  } catch (error) {
    console.error("Failed to rate worksheet:", error);
    return false;
  }
}

/**
 * Calculate average rating and count for a worksheet
 */
export async function calculateWorksheetRatingStats(
  context: KVContext,
  worksheetId: string,
): Promise<{ averageRating: number; ratingCount: number }> {
  try {
    const ratings: number[] = [];
    const prefix = `rating:${worksheetId}:`;

    const listResult = await context.env.KV.list({
      prefix,
    });

    for (const key of listResult.keys) {
      const data = await context.env.KV.get(key.name, "json");
      if (data && typeof data === "object" && "rating" in data) {
        ratings.push((data as { rating: number }).rating);
      }
    }

    const averageRating =
      ratings.length > 0
        ? Math.round(
            (ratings.reduce((a, b) => a + b, 0) / ratings.length) * 10,
          ) / 10
        : 0;

    return {
      averageRating,
      ratingCount: ratings.length,
    };
  } catch (error) {
    console.error("Failed to calculate rating stats:", error);
    return { averageRating: 0, ratingCount: 0 };
  }
}

/**
 * Delete a worksheet by ID
 * Also deletes associated ratings
 */
export async function deleteWorksheet(
  context: KVContext,
  id: string,
): Promise<boolean> {
  try {
    const worksheetKey = `worksheet:${id}`;
    
    // Delete the worksheet
    await context.env.KV.delete(worksheetKey);
    
    // Delete all associated ratings
    const ratingsPrefix = `rating:${id}:`;
    const ratingsListResult = await context.env.KV.list({
      prefix: ratingsPrefix,
    });
    
    for (const key of ratingsListResult.keys) {
      await context.env.KV.delete(key.name);
    }
    
    return true;
  } catch (error) {
    console.error("Failed to delete worksheet:", error);
    return false;
  }
}
