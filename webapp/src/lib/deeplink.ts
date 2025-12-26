/**
 * Utilities for generating deeplinks for importing challenges into the Math Pup Tutor app.
 *
 * Challenge data should be validated using the challenge schema before generating a deeplink.
 * The Android app will perform additional validation on receipt using ChallengeJsonParser
 * to ensure data integrity and security.
 *
 * Usage:
 * ```tsx
 * const deeplink = generateDeeplink(challengeJson);
 * window.location.href = deeplink;
 * // Or for non-mobile, show a button that opens the link
 * ```
 */

/**
 * Generates a deeplink URL for importing a challenge into the Math Pup Tutor app.
 *
 * Format: `mathpup://import?json=<url-encoded-json>`
 *
 * Note: While this function accepts any JSON, the Android app will validate it against
 * the challenge schema using ChallengeJsonParser. For production use, ensure the data
 * is validated using the Zod schemas before generating the deeplink.
 *
 * @param jsonData The challenge JSON object or string to encode (should be pre-validated)
 * @returns The deeplink URL string
 */
export function generateDeeplink(jsonData: object | string): string {
  try {
    const jsonString =
      typeof jsonData === "string" ? jsonData : JSON.stringify(jsonData);
    const encodedJson = encodeURIComponent(jsonString);
    return `mathpup://import?json=${encodedJson}`;
  } catch (error) {
    console.error("Failed to generate deeplink:", error);
    return "";
  }
}

/**
 * Attempts to open a challenge in the Math Pup Tutor app using a deeplink.
 *
 * Falls back to returning the deeplink URL if the app is not installed
 * (e.g., on web browsers or devices without the app).
 *
 * @param jsonData The challenge JSON object or string to import
 * @returns The deeplink URL (can be used as fallback)
 */
export function openInApp(jsonData: object | string): string {
  const deeplink = generateDeeplink(jsonData);

  if (!deeplink) {
    console.error("Failed to generate deeplink");
    return "";
  }

  // Attempt to open the deeplink
  // This will only work on Android devices with the app installed
  window.location.href = deeplink;

  // Return the deeplink in case the app isn't installed
  // The redirect above will be a no-op if the app isn't available
  return deeplink;
}

/**
 * Checks if the deeplink would be available on the current device.
 *
 * Note: This is a heuristic check and may not be 100% accurate.
 * The deeplink will work on any Android device with the app installed.
 *
 * @returns True if likely on Android, false otherwise
 */
export function isLikelyAndroidDevice(): boolean {
  const userAgent = navigator.userAgent.toLowerCase();
  return /android/.test(userAgent);
}

/**
 * Gets a display URL for the deeplink (useful for showing as a QR code or shared link).
 *
 * Since deeplinks use a custom scheme, they won't work in web browsers.
 * This can be used to generate a web-accessible version if needed.
 *
 * @param deeplink The deeplink URL
 * @returns A description of the deeplink for display purposes
 */
export function getDeeplinkDisplay(deeplink: string): string {
  if (!deeplink) {
    return "Invalid deeplink";
  }

  // Extract the JSON parameter for display
  try {
    const url = new URL(deeplink.replace("mathpup://", "http://"));
    const json = url.searchParams.get("json");
    return json ? `mathpup://import?json=[${json.length} chars]` : deeplink;
  } catch {
    return deeplink;
  }
}
