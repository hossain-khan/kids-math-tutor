/**
 * Admin authentication utilities
 * Handles token storage and validation for admin portal access
 */

const ADMIN_TOKEN_KEY = "admin_token";
const ADMIN_TOKEN_EXPIRY_KEY = "admin_token_expiry";

export function getAdminAuthToken(): string | null {
  const token = localStorage.getItem(ADMIN_TOKEN_KEY);
  const expiry = localStorage.getItem(ADMIN_TOKEN_EXPIRY_KEY);

  if (!token || !expiry) {
    return null;
  }

  // Check if token has expired
  if (Date.now() > parseInt(expiry, 10)) {
    clearAdminAuthToken();
    return null;
  }

  return token;
}

export function isAdminAuthenticated(): boolean {
  return getAdminAuthToken() !== null;
}

export function clearAdminAuthToken(): void {
  localStorage.removeItem(ADMIN_TOKEN_KEY);
  localStorage.removeItem(ADMIN_TOKEN_EXPIRY_KEY);
}
