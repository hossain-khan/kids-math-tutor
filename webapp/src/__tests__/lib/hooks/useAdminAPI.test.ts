import { describe, it, expect, beforeEach, vi } from "vitest";
import { renderHook, waitFor } from "@testing-library/react";
import { useAdminAPI } from "@/lib/hooks/useAdminAPI";
import * as adminAuth from "@/lib/adminAuth";

// Mock fetch globally
global.fetch = vi.fn();

// Mock adminAuth module
vi.mock("@/lib/adminAuth", () => ({
  getAdminAuthToken: vi.fn(),
  isAdminAuthenticated: vi.fn(),
  clearAdminAuthToken: vi.fn(),
}));

describe("useAdminAPI", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    (global.fetch as any).mockClear();
  });

  describe("authenticate", () => {
    it("should authenticate with valid password", async () => {
      (global.fetch as any).mockResolvedValueOnce({
        ok: true,
        json: async () => ({
          token: "test-token-123",
          expiry: 1234567890,
        }),
      });

      const { result } = renderHook(() => useAdminAPI());

      const response = await result.current.authenticate("password123");

      expect(response).toEqual({
        token: "test-token-123",
        expiry: 1234567890,
      });

      expect(global.fetch).toHaveBeenCalledWith("/api/v1/admin/auth", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ password: "password123" }),
      });
    });

    it("should throw error on failed authentication", async () => {
      (global.fetch as any).mockResolvedValueOnce({
        ok: false,
        json: async () => ({
          error: "Invalid password",
        }),
      });

      const { result } = renderHook(() => useAdminAPI());

      await expect(
        result.current.authenticate("wrong-password"),
      ).rejects.toThrow("Invalid password");
    });

    it("should handle generic error messages", async () => {
      (global.fetch as any).mockResolvedValueOnce({
        ok: false,
        json: async () => ({}),
      });

      const { result } = renderHook(() => useAdminAPI());

      await expect(result.current.authenticate("password")).rejects.toThrow(
        "Authentication failed",
      );
    });
  });

  describe("fetchWorksheets", () => {
    it("should fetch worksheets with default parameters", async () => {
      vi.mocked(adminAuth.getAdminAuthToken).mockReturnValue("test-token");

      (global.fetch as any).mockResolvedValueOnce({
        ok: true,
        json: async () => ({
          worksheets: [
            {
              id: "ws-1",
              title: "Addition Practice",
              problemCount: 10,
              createdAt: "2024-01-01",
            },
          ],
          total: 1,
        }),
      });

      const { result } = renderHook(() => useAdminAPI());

      const response = await result.current.fetchWorksheets();

      expect(response.worksheets).toHaveLength(1);
      expect(response.total).toBe(1);
      expect(global.fetch).toHaveBeenCalledWith(
        "/api/v1/admin/worksheets?limit=20&offset=0",
        {
          headers: {
            Authorization: "Bearer test-token",
          },
        },
      );
    });

    it("should fetch worksheets with custom parameters", async () => {
      vi.mocked(adminAuth.getAdminAuthToken).mockReturnValue("test-token");

      (global.fetch as any).mockResolvedValueOnce({
        ok: true,
        json: async () => ({
          worksheets: [],
          total: 0,
        }),
      });

      const { result } = renderHook(() => useAdminAPI());

      await result.current.fetchWorksheets({
        limit: 50,
        offset: 100,
        search: "addition",
      });

      expect(global.fetch).toHaveBeenCalledWith(
        "/api/v1/admin/worksheets?limit=50&offset=100&search=addition",
        {
          headers: {
            Authorization: "Bearer test-token",
          },
        },
      );
    });

    it("should throw error when not authenticated", async () => {
      vi.mocked(adminAuth.getAdminAuthToken).mockReturnValue(null);

      const { result } = renderHook(() => useAdminAPI());

      await expect(result.current.fetchWorksheets()).rejects.toThrow(
        "Session expired, please login again",
      );
    });

    it("should throw error on failed fetch", async () => {
      vi.mocked(adminAuth.getAdminAuthToken).mockReturnValue("test-token");

      (global.fetch as any).mockResolvedValueOnce({
        ok: false,
      });

      const { result } = renderHook(() => useAdminAPI());

      await expect(result.current.fetchWorksheets()).rejects.toThrow(
        "Failed to fetch worksheets",
      );
    });
  });

  describe("deleteWorksheet", () => {
    it("should delete a worksheet", async () => {
      vi.mocked(adminAuth.getAdminAuthToken).mockReturnValue("test-token");

      (global.fetch as any).mockResolvedValueOnce({
        ok: true,
      });

      const { result } = renderHook(() => useAdminAPI());

      await result.current.deleteWorksheet("ws-123");

      expect(global.fetch).toHaveBeenCalledWith(
        "/api/v1/admin/worksheets/ws-123",
        {
          method: "DELETE",
          headers: {
            Authorization: "Bearer test-token",
          },
        },
      );
    });

    it("should throw error when not authenticated", async () => {
      vi.mocked(adminAuth.getAdminAuthToken).mockReturnValue(null);

      const { result } = renderHook(() => useAdminAPI());

      await expect(result.current.deleteWorksheet("ws-123")).rejects.toThrow(
        "Session expired, please login again",
      );
    });

    it("should throw error on failed delete", async () => {
      vi.mocked(adminAuth.getAdminAuthToken).mockReturnValue("test-token");

      (global.fetch as any).mockResolvedValueOnce({
        ok: false,
      });

      const { result } = renderHook(() => useAdminAPI());

      await expect(result.current.deleteWorksheet("ws-123")).rejects.toThrow(
        "Failed to delete worksheet",
      );
    });
  });

  describe("checkContentSafety", () => {
    it("should check content safety for worksheets", async () => {
      vi.mocked(adminAuth.getAdminAuthToken).mockReturnValue("test-token");

      (global.fetch as any).mockResolvedValueOnce({
        ok: true,
        json: async () => ({
          results: [
            {
              worksheetId: "ws-1",
              safe: true,
              categories: [],
              explanation: "",
              usingAI: true,
              confidence: 0.95,
              timestamp: "2024-01-01T00:00:00Z",
            },
          ],
          summary: {
            safe: 1,
            flagged: 0,
          },
        }),
      });

      const { result } = renderHook(() => useAdminAPI());

      const response = await result.current.checkContentSafety(["ws-1"]);

      expect(response.results).toHaveLength(1);
      expect(response.summary.safe).toBe(1);
      expect(response.summary.flagged).toBe(0);

      expect(global.fetch).toHaveBeenCalledWith("/api/v1/admin/check-safety", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: "Bearer test-token",
        },
        body: JSON.stringify({ worksheetIds: ["ws-1"] }),
      });
    });

    it("should throw error when not authenticated", async () => {
      vi.mocked(adminAuth.getAdminAuthToken).mockReturnValue(null);

      const { result } = renderHook(() => useAdminAPI());

      await expect(result.current.checkContentSafety(["ws-1"])).rejects.toThrow(
        "Session expired, please login again",
      );
    });

    it("should throw error on failed safety check", async () => {
      vi.mocked(adminAuth.getAdminAuthToken).mockReturnValue("test-token");

      (global.fetch as any).mockResolvedValueOnce({
        ok: false,
      });

      const { result } = renderHook(() => useAdminAPI());

      await expect(result.current.checkContentSafety(["ws-1"])).rejects.toThrow(
        "Failed to check content safety",
      );
    });
  });

  describe("isAuthenticated", () => {
    it("should return authentication status", () => {
      vi.mocked(adminAuth.isAdminAuthenticated).mockReturnValue(true);

      const { result } = renderHook(() => useAdminAPI());

      expect(result.current.isAuthenticated()).toBe(true);
    });

    it("should return false when not authenticated", () => {
      vi.mocked(adminAuth.isAdminAuthenticated).mockReturnValue(false);

      const { result } = renderHook(() => useAdminAPI());

      expect(result.current.isAuthenticated()).toBe(false);
    });
  });
});
