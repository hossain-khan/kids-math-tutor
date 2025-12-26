/**
 * Session ID utility for anonymous user tracking
 * Used for rating deduplication without storing PII
 */

const SESSION_ID_KEY = 'worksheetRatingSessionId';

/**
 * Get or create a unique session ID
 * Stored in localStorage to persist across sessions
 */
export function getOrCreateSessionId(): string {
  try {
    let sessionId = localStorage.getItem(SESSION_ID_KEY);

    if (!sessionId) {
      // Generate a simple random ID (sufficient for deduplication)
      sessionId = `session_${Date.now()}_${Math.random().toString(36).substring(2, 9)}`;
      localStorage.setItem(SESSION_ID_KEY, sessionId);
    }

    return sessionId;
  } catch (error) {
    // Fallback if localStorage is not available
    console.warn('Unable to use localStorage for session ID:', error);
    return `session_${Date.now()}_${Math.random().toString(36).substring(2, 9)}`;
  }
}

/**
 * Clear the session ID (for testing or explicit logout)
 */
export function clearSessionId(): void {
  try {
    localStorage.removeItem(SESSION_ID_KEY);
  } catch (error) {
    console.warn('Unable to clear session ID:', error);
  }
}
