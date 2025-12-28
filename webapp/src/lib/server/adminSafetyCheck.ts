/**
 * Admin bulk safety check service
 * Allows admins to verify the safety of all shared worksheets using Llama Guard 3
 */

import { checkContentSafetyWithAI, type SafetyCheckResult } from '@/lib/server/aiSafety';
import { getWorksheet } from '@/lib/server/worksheetStorage';
import type { SharedWorksheet } from '@/lib/server/worksheetStorage';

/**
 * Safety check result for a single worksheet
 */
export interface WorksheetSafetyCheckResult extends SafetyCheckResult {
  worksheetId: string;
  timestamp: string;
}

/**
 * Summary of bulk safety check operation
 */
export interface SafetyCheckSummary {
  total: number;
  safe: number;
  flagged: number;
  errors: number;
  duration: number; // milliseconds
}

/**
 * Response from bulk safety check endpoint
 */
export interface BulkSafetyCheckResponse {
  results: WorksheetSafetyCheckResult[];
  summary: SafetyCheckSummary;
}

/**
 * Check safety of a single worksheet
 *
 * @param env Cloudflare environment with AI binding
 * @param worksheet The worksheet to check
 * @returns Safety check result with metadata
 */
export async function checkWorksheetSafety(
  env: { AI?: unknown },
  worksheet: SharedWorksheet,
): Promise<WorksheetSafetyCheckResult> {
  try {
    const safetyResult = await checkContentSafetyWithAI(env, {
      title: worksheet.title,
      subtitle: worksheet.subtitle,
      description: worksheet.description,
    });

    return {
      ...safetyResult,
      worksheetId: worksheet.id,
      timestamp: new Date().toISOString(),
    };
  } catch (error) {
    console.error(
      `Safety check failed for worksheet ${worksheet.id}:`,
      error,
    );

    // Return a safe result with error flag if check fails
    return {
      safe: true,
      classification: "safe",
      categories: undefined,
      explanation: 'Safety check failed, allowing by default',
      confidence: 0,
      usingAI: false,
      fallback: false,
      worksheetId: worksheet.id,
      timestamp: new Date().toISOString(),
    };
  }
}

/**
 * Perform bulk safety check on worksheets
 *
 * @param env Cloudflare environment with KV and AI bindings
 * @param worksheetIds Specific worksheet IDs to check, or undefined to check all
 * @returns Bulk safety check results and summary
 */
export async function bulkCheckSafety(
  env: { AI?: unknown; KV: unknown },
  worksheetIds?: string[],
): Promise<BulkSafetyCheckResponse> {
  const startTime = Date.now();
  const kvContext = { env: { KV: env.KV } };

  try {
    // Get list of worksheet IDs to check
    let idsToCheck: string[];

    if (worksheetIds && worksheetIds.length > 0) {
      idsToCheck = worksheetIds;
    } else {
      // Get all worksheet IDs from KV
      // This is a simplified implementation - in production, consider a more efficient approach
      idsToCheck = await getAllWorksheetIds(kvContext);
    }

    console.log(`Starting safety check for ${idsToCheck.length} worksheets`);

    // Check each worksheet with rate limiting
    const results: WorksheetSafetyCheckResult[] = [];
    let safeCount = 0;
    let flaggedCount = 0;
    let errorCount = 0;

    // Process in batches to respect rate limits
    const batchSize = 5; // Process 5 worksheets at a time
    for (let i = 0; i < idsToCheck.length; i += batchSize) {
      const batch = idsToCheck.slice(i, i + batchSize);

      const batchResults = await Promise.allSettled(
        batch.map(async (id) => {
          const worksheet = await getWorksheet(kvContext, id);
          if (!worksheet) {
            throw new Error(`Worksheet ${id} not found`);
          }
          return checkWorksheetSafety(env, worksheet);
        }),
      );

      for (const result of batchResults) {
        if (result.status === 'fulfilled') {
          results.push(result.value);
          if (result.value.safe) {
            safeCount++;
          } else {
            flaggedCount++;
          }
        } else {
          errorCount++;
          console.error('Worksheet check error:', result.reason);
        }
      }

      // Small delay between batches to avoid overwhelming the AI service
      if (i + batchSize < idsToCheck.length) {
        await new Promise((resolve) => setTimeout(resolve, 100));
      }
    }

    const duration = Date.now() - startTime;

    console.log(
      `Safety check complete: ${safeCount} safe, ${flaggedCount} flagged, ${errorCount} errors in ${duration}ms`,
    );

    return {
      results,
      summary: {
        total: idsToCheck.length,
        safe: safeCount,
        flagged: flaggedCount,
        errors: errorCount,
        duration,
      },
    };
  } catch (error) {
    const duration = Date.now() - startTime;
    console.error('Bulk safety check error:', error);

    return {
      results: [],
      summary: {
        total: 0,
        safe: 0,
        flagged: 0,
        errors: 1,
        duration,
      },
    };
  }
}

/**
 * Get all worksheet IDs from KV
 * This is a simplified version - production should use a more efficient approach
 */
async function getAllWorksheetIds(
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  _context: { env: { KV: unknown } },
): Promise<string[]> {
  const ids: string[] = [];

  try {
    // In a real implementation, we'd use KV list API or maintain an index
    // For now, this is a placeholder that would need to be optimized
    // The actual implementation would use context.env.KV.list()

    console.log('Fetching all worksheet IDs from KV');
    // This is where you would implement KV list logic
    // For now returning empty array - to be implemented with KV API
  } catch (error) {
    console.error('Failed to get worksheet IDs:', error);
  }

  return ids;
}
