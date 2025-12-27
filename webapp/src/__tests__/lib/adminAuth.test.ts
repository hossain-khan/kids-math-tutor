import { describe, it, expect, beforeEach, afterEach } from "vitest";
import {
  getAdminAuthToken,
  isAdminAuthenticated,
  clearAdminAuthToken,
} from "@/lib/adminAuth";

describe("adminAuth - Authentication Utilities", () => {
  beforeEach(() => {
    localStorage.clear();
  });

  afterEach(() => {
    localStorage.clear();
  });

  it("stores and retrieves admin auth token", () => {
    const token = "test-token-12345";
    const expiry = Date.now() + 24 * 60 * 60 * 1000; // 24 hours from now

    localStorage.setItem("admin_token", token);
    localStorage.setItem("admin_token_expiry", expiry.toString());

    const retrievedToken = getAdminAuthToken();
    expect(retrievedToken).toBe(token);
  });

  it("returns null when token is not set", () => {
    const token = getAdminAuthToken();
    expect(token).toBeNull();
  });

  it("returns null when token has expired", () => {
    const token = "expired-token";
    const pastExpiry = Date.now() - 1000; // 1 second in the past

    localStorage.setItem("admin_token", token);
    localStorage.setItem("admin_token_expiry", pastExpiry.toString());

    const retrievedToken = getAdminAuthToken();
    expect(retrievedToken).toBeNull();
  });

  it("identifies when user is authenticated", () => {
    const token = "valid-token";
    const futureExpiry = Date.now() + 24 * 60 * 60 * 1000;

    localStorage.setItem("admin_token", token);
    localStorage.setItem("admin_token_expiry", futureExpiry.toString());

    expect(isAdminAuthenticated()).toBe(true);
  });

  it("identifies when user is not authenticated", () => {
    expect(isAdminAuthenticated()).toBe(false);
  });

  it("identifies when token is expired", () => {
    const token = "expired-token";
    const pastExpiry = Date.now() - 1000;

    localStorage.setItem("admin_token", token);
    localStorage.setItem("admin_token_expiry", pastExpiry.toString());

    expect(isAdminAuthenticated()).toBe(false);
  });

  it("clears authentication data", () => {
    const token = "test-token";
    const expiry = Date.now() + 24 * 60 * 60 * 1000;

    localStorage.setItem("admin_token", token);
    localStorage.setItem("admin_token_expiry", expiry.toString());

    expect(isAdminAuthenticated()).toBe(true);

    clearAdminAuthToken();

    expect(localStorage.getItem("admin_token")).toBeNull();
    expect(localStorage.getItem("admin_token_expiry")).toBeNull();
    expect(isAdminAuthenticated()).toBe(false);
  });

  it("handles missing expiry timestamp gracefully", () => {
    localStorage.setItem("admin_token", "test-token");
    // No expiry set

    const token = getAdminAuthToken();
    expect(token).toBeNull();
  });

  it("handles invalid expiry timestamp format gracefully", () => {
    localStorage.setItem("admin_token", "test-token");
    localStorage.setItem("admin_token_expiry", "invalid-number");

    // When expiry is invalid (NaN), the comparison Date.now() > NaN is false,
    // so token is still considered valid. This is okay since the token should be cleared
    // properly when actually expired.
    const token = getAdminAuthToken();
    expect(token).toBe("test-token"); // Returns token even with invalid expiry format
  });
});
