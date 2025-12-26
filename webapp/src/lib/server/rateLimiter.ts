/**
 * Rate limiting utilities for shared worksheet creation.
 * Limits users to 10 shares per day using Cloudflare KV.
 */

export interface RateLimitContext {
  env: {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    KV: any; // KVNamespace from @cloudflare/workers-types
  };
  clientIp: string;
}

const SHARES_PER_DAY = 10;

/**
 * Get rate limit key for a client IP and date
 */
function getRateLimitKey(ip: string, date: string): string {
  return `ratelimit:${ip}:${date}`;
}

/**
 * Get today's date in YYYY-MM-DD format (UTC)
 */
function getTodayDate(): string {
  const now = new Date();
  return now.toISOString().split('T')[0];
}

/**
 * Check if client has exceeded daily share limit
 * @returns { allowed: boolean, remaining: number, resetTime: number (ms) }
 */
export async function checkRateLimit(
  context: RateLimitContext,
): Promise<{
  allowed: boolean;
  remaining: number;
  resetTime: number;
}> {
  const today = getTodayDate();
  const key = getRateLimitKey(context.clientIp, today);

  try {
    // Get current count
    const countStr = await context.env.KV.get(key);
    const count = countStr ? parseInt(countStr, 10) : 0;

    const remaining = Math.max(0, SHARES_PER_DAY - count);
    const allowed = count < SHARES_PER_DAY;

    // Calculate reset time (next day at 00:00 UTC)
    const tomorrow = new Date();
    tomorrow.setUTCDate(tomorrow.getUTCDate() + 1);
    tomorrow.setUTCHours(0, 0, 0, 0);
    const resetTime = tomorrow.getTime();

    return {
      allowed,
      remaining,
      resetTime,
    };
  } catch (error) {
    console.error('Rate limit check failed:', error);
    // On error, allow the request (fail open)
    return {
      allowed: true,
      remaining: SHARES_PER_DAY,
      resetTime: 0,
    };
  }
}

/**
 * Increment share count for a client
 */
export async function incrementShareCount(
  context: RateLimitContext,
): Promise<void> {
  const today = getTodayDate();
  const key = getRateLimitKey(context.clientIp, today);

  try {
    const countStr = await context.env.KV.get(key);
    const count = countStr ? parseInt(countStr, 10) : 0;
    const newCount = count + 1;

    // Set with expiry of 24 hours (86400 seconds)
    await context.env.KV.put(key, newCount.toString(), {
      expirationTtl: 86400,
    });
  } catch (error) {
    console.error('Failed to increment share count:', error);
  }
}
