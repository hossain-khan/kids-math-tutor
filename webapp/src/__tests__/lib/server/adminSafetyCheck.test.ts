import { describe, it, expect, vi, beforeEach, afterEach } from "vitest";
import {
  checkWorksheetSafety,
  bulkCheckSafety,
  type WorksheetSafetyCheckResult,
  type BulkSafetyCheckResponse,
} from "@/lib/server/adminSafetyCheck";
import * as aiSafety from "@/lib/server/aiSafety";
import * as worksheetStorage from "@/lib/server/worksheetStorage";
import type { SharedWorksheet } from "@/lib/server/worksheetStorage";

// Mock modules
vi.mock("@/lib/server/aiSafety");
vi.mock("@/lib/server/worksheetStorage");

describe("adminSafetyCheck module", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  describe("checkWorksheetSafety", () => {
    const mockWorksheet: SharedWorksheet = {
      id: "test-worksheet-1",
      type: "explicit",
      title: "Addition Practice",
      subtitle: "Learn Addition",
      description: "Basic addition problems",
      grades: ["K", "1"],
      problems: [],
      createdAt: "2025-01-10T00:00:00.000Z",
      stats: {
        views: 0,
        downloads: 0,
        averageRating: 0,
        ratingCount: 0,
      },
    };

    it("should check worksheet safety and return AI-based result", async () => {
      const mockAIResult = {
        safe: true,
        classification: "safe" as const,
        confidence: 0.95,
        usingAI: true,
        fallback: false,
      };

      vi.mocked(aiSafety.checkContentSafetyWithAI).mockResolvedValue(
        mockAIResult,
      );

      const result = await checkWorksheetSafety({}, mockWorksheet);

      expect(result).toMatchObject({
        safe: true,
        confidence: 0.95,
        usingAI: true,
        fallback: false,
        worksheetId: "test-worksheet-1",
      });
      expect(result.timestamp).toBeDefined();
      expect(aiSafety.checkContentSafetyWithAI).toHaveBeenCalledWith(
        {},
        expect.objectContaining({
          title: "Addition Practice",
        }),
      );
    });

    it("should check worksheet with all content fields", async () => {
      const mockAIResult = {
        safe: true,
        classification: "safe" as const,
        confidence: 0.95,
        usingAI: true,
      };

      vi.mocked(aiSafety.checkContentSafetyWithAI).mockResolvedValue(
        mockAIResult,
      );

      await checkWorksheetSafety({}, mockWorksheet);

      expect(aiSafety.checkContentSafetyWithAI).toHaveBeenCalledWith(
        {},
        {
          title: "Addition Practice",
          subtitle: "Learn Addition",
          description: "Basic addition problems",
        },
      );
    });

    it("should detect flagged content", async () => {
      const mockAIResult = {
        safe: false,
        classification: "unsafe" as const,
        categories: ["profanity"],
        explanation: "Content contains inappropriate language",
        confidence: 0.92,
        usingAI: true,
        fallback: false,
      };

      vi.mocked(aiSafety.checkContentSafetyWithAI).mockResolvedValue(
        mockAIResult,
      );

      const result = await checkWorksheetSafety({}, mockWorksheet);

      expect(result.safe).toBe(false);
      expect(result.categories).toContain("profanity");
      expect(result.explanation).toBeDefined();
    });

    it("should handle AI safety check errors gracefully", async () => {
      vi.mocked(aiSafety.checkContentSafetyWithAI).mockRejectedValue(
        new Error("AI service unavailable"),
      );

      const result = await checkWorksheetSafety({}, mockWorksheet);

      expect(result.safe).toBe(true); // Default to safe on error
      expect(result.usingAI).toBe(false);
      expect(result.fallback).toBe(false);
      expect(result.explanation).toBe(
        "Safety check failed, allowing by default",
      );
    });

    it("should include worksheet ID and timestamp in result", async () => {
      const mockAIResult = {
        safe: true,
        classification: "safe" as const,
        confidence: 0.95,
        usingAI: true,
      };

      vi.mocked(aiSafety.checkContentSafetyWithAI).mockResolvedValue(
        mockAIResult,
      );

      const result = await checkWorksheetSafety({}, mockWorksheet);

      expect(result.worksheetId).toBe("test-worksheet-1");
      expect(result.timestamp).toMatch(
        /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{3}Z$/,
      );
    });

    it("should handle worksheets with missing subtitle and description", async () => {
      const minimalWorksheet: SharedWorksheet = {
        ...mockWorksheet,
        subtitle: undefined,
        description: undefined,
      };

      const mockAIResult = {
        safe: true,
        classification: "safe" as const,
        confidence: 0.95,
        usingAI: true,
      };

      vi.mocked(aiSafety.checkContentSafetyWithAI).mockResolvedValue(
        mockAIResult,
      );

      const result = await checkWorksheetSafety({}, minimalWorksheet);

      expect(result.safe).toBe(true);
      expect(aiSafety.checkContentSafetyWithAI).toHaveBeenCalledWith(
        {},
        {
          title: mockWorksheet.title,
          subtitle: undefined,
          description: undefined,
        },
      );
    });
  });

  describe("bulkCheckSafety", () => {
    const mockWorksheets: Record<string, SharedWorksheet> = {
      "ws-1": {
        id: "ws-1",
        type: "explicit",
        title: "Safe Worksheet",
        grades: ["K"],
        problems: [],
        createdAt: "2025-01-10T00:00:00.000Z",
        stats: {
          views: 0,
          downloads: 0,
          averageRating: 0,
          ratingCount: 0,
        },
      },
      "ws-2": {
        id: "ws-2",
        type: "explicit",
        title: "Flagged Worksheet",
        grades: ["1"],
        problems: [],
        createdAt: "2025-01-10T00:00:00.000Z",
        stats: {
          views: 0,
          downloads: 0,
          averageRating: 0,
          ratingCount: 0,
        },
      },
      "ws-3": {
        id: "ws-3",
        type: "explicit",
        title: "Another Safe Worksheet",
        grades: ["2"],
        problems: [],
        createdAt: "2025-01-10T00:00:00.000Z",
        stats: {
          views: 0,
          downloads: 0,
          averageRating: 0,
          ratingCount: 0,
        },
      },
    };

    beforeEach(() => {
      // Mock getWorksheet to return worksheets by ID
      vi.mocked(worksheetStorage.getWorksheet).mockImplementation(
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        async (ctx: any, id: string) => mockWorksheets[id] || null,
      );

      // Mock aiSafety to return alternating safe/flagged results
      vi.mocked(aiSafety.checkContentSafetyWithAI).mockImplementation(
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        async (env: any, content: any) => {
          if (content.title.includes("Flagged")) {
            return {
              safe: false,
              classification: "unsafe",
              categories: ["profanity"],
              explanation: "Contains inappropriate content",
              confidence: 0.9,
              usingAI: true,
            };
          }
          return {
            safe: true,
            classification: "safe",
            confidence: 0.95,
            usingAI: true,
          };
        },
      );
    });

    it("should check all specified worksheets", async () => {
      const env = { AI: {}, KV: {} };
      const result = await bulkCheckSafety(env, ["ws-1", "ws-2", "ws-3"]);

      expect(result.results).toHaveLength(3);
      expect(result.summary.total).toBe(3);
      expect(worksheetStorage.getWorksheet).toHaveBeenCalledTimes(3);
    });

    it("should count safe and flagged worksheets correctly", async () => {
      const env = { AI: {}, KV: {} };
      const result = await bulkCheckSafety(env, ["ws-1", "ws-2", "ws-3"]);

      expect(result.summary.safe).toBe(2); // ws-1 and ws-3 are safe
      expect(result.summary.flagged).toBe(1); // ws-2 is flagged
    });

    it("should handle worksheet not found errors", async () => {
      const env = { AI: {}, KV: {} };
      const result = await bulkCheckSafety(env, [
        "ws-1",
        "non-existent",
        "ws-3",
      ]);

      expect(result.summary.total).toBe(3);
      // Non-existent worksheet throws error, so it counts as error
      expect(result.summary.errors).toBeGreaterThanOrEqual(1);
      // Successful checks are less than total
      expect(result.results.length).toBeLessThan(3);
    });

    it("should return results with worksheetId and timestamp", async () => {
      const env = { AI: {}, KV: {} };
      const result = await bulkCheckSafety(env, ["ws-1"]);

      expect(result.results[0]).toMatchObject({
        worksheetId: "ws-1",
        safe: true,
      });
      expect(result.results[0].timestamp).toBeDefined();
    });

    it("should return bulk check summary with duration", async () => {
      const env = { AI: {}, KV: {} };
      const result = await bulkCheckSafety(env, ["ws-1", "ws-2"]);

      expect(result.summary).toMatchObject({
        total: 2,
        safe: expect.any(Number),
        flagged: expect.any(Number),
        errors: expect.any(Number),
        duration: expect.any(Number),
      });
      expect(result.summary.duration).toBeGreaterThanOrEqual(0);
    });

    it("should handle empty worksheet list", async () => {
      const env = { AI: {}, KV: {} };
      const result = await bulkCheckSafety(env, []);

      expect(result.results).toHaveLength(0);
      expect(result.summary.total).toBe(0);
      expect(result.summary.safe).toBe(0);
      expect(result.summary.flagged).toBe(0);
    });

    it("should handle AI safety check errors gracefully", async () => {
      vi.mocked(aiSafety.checkContentSafetyWithAI).mockRejectedValueOnce(
        new Error("AI service error"),
      );

      const env = { AI: {}, KV: {} };
      const result = await bulkCheckSafety(env, ["ws-1", "ws-2"]);

      // First check fails due to AI error, second succeeds
      // The failed check is counted via Promise.allSettled error handling
      expect(result.summary.total).toBe(2);
      // Results will have one less than total (one failed)
      expect(result.results.length).toBeGreaterThanOrEqual(1);
    });

    it("should process worksheets in batches", async () => {
      const env = { AI: {}, KV: {} };
      const worksheetIds = ["ws-1", "ws-2", "ws-3"];

      await bulkCheckSafety(env, worksheetIds);

      // Verify all worksheets were processed
      expect(worksheetStorage.getWorksheet).toHaveBeenCalledTimes(3);
    });

    it("should use provided worksheet IDs when given", async () => {
      const env = { AI: {}, KV: {} };
      const result = await bulkCheckSafety(env, ["ws-1", "ws-3"]);

      expect(result.results).toHaveLength(2);
      expect(result.results.map((r) => r.worksheetId)).toEqual(
        expect.arrayContaining(["ws-1", "ws-3"]),
      );
    });

    it("should return well-formed BulkSafetyCheckResponse", async () => {
      const env = { AI: {}, KV: {} };
      const result = await bulkCheckSafety(env, ["ws-1", "ws-2"]);

      // Verify response structure
      expect(result).toHaveProperty("results");
      expect(result).toHaveProperty("summary");
      expect(Array.isArray(result.results)).toBe(true);
      expect(typeof result.summary.total).toBe("number");
      expect(typeof result.summary.safe).toBe("number");
      expect(typeof result.summary.flagged).toBe("number");
      expect(typeof result.summary.errors).toBe("number");
      expect(typeof result.summary.duration).toBe("number");
    });

    it("should track all result details", async () => {
      const env = { AI: {}, KV: {} };
      const result = await bulkCheckSafety(env, ["ws-1"]);

      const item = result.results[0];
      expect(item).toHaveProperty("safe");
      expect(item).toHaveProperty("confidence");
      expect(item).toHaveProperty("usingAI");
      expect(item).toHaveProperty("worksheetId");
      expect(item).toHaveProperty("timestamp");
      expect(item).toHaveProperty("classification");
    });

    it("should handle partial batch processing when some checks fail", async () => {
      // Create a scenario where some worksheets fail to load
      const worksheets: Record<string, SharedWorksheet> = {
        safe1: {
          id: "safe1",
          type: "explicit",
          title: "Safe 1",
          grades: ["K"],
          problems: [],
          createdAt: "2025-01-10T00:00:00.000Z",
          stats: {
            views: 0,
            downloads: 0,
            averageRating: 0,
            ratingCount: 0,
          },
        },
        safe2: {
          id: "safe2",
          type: "explicit",
          title: "Safe 2",
          grades: ["1"],
          problems: [],
          createdAt: "2025-01-10T00:00:00.000Z",
          stats: {
            views: 0,
            downloads: 0,
            averageRating: 0,
            ratingCount: 0,
          },
        },
      };

      vi.mocked(worksheetStorage.getWorksheet).mockImplementation(
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        async (ctx: any, id: string) => worksheets[id] || null,
      );

      vi.mocked(aiSafety.checkContentSafetyWithAI).mockResolvedValue({
        safe: true,
        classification: "safe",
        confidence: 0.95,
        usingAI: true,
      });

      const env = { AI: {}, KV: {} };
      // Request 3 worksheets but only 2 exist (safe1, safe2 exist, missing3 doesn't)
      const result = await bulkCheckSafety(env, ["safe1", "missing3", "safe2"]);

      // One failed (missing3), two succeeded
      expect(result.summary.total).toBe(3);
      expect(result.results.length).toBe(2); // Only two successful
      expect(result.summary.safe).toBe(2);
      expect(result.summary.flagged).toBe(0);
    });
  });

  describe("Safety check response types", () => {
    it("should have correct WorksheetSafetyCheckResult structure", async () => {
      const mockWorksheet: SharedWorksheet = {
        id: "test-ws",
        type: "explicit",
        title: "Test",
        grades: ["K"],
        problems: [],
        createdAt: "2025-01-10T00:00:00.000Z",
        stats: {
          views: 0,
          downloads: 0,
          averageRating: 0,
          ratingCount: 0,
        },
      };

      vi.mocked(aiSafety.checkContentSafetyWithAI).mockResolvedValue({
        safe: true,
        classification: "safe",
        confidence: 0.95,
        usingAI: true,
      });

      const result: WorksheetSafetyCheckResult =
        await import("@/lib/server/adminSafetyCheck").then((m) =>
          m.checkWorksheetSafety({}, mockWorksheet),
        );

      expect(result.worksheetId).toBeDefined();
      expect(result.timestamp).toBeDefined();
      expect(result.safe).toBeDefined();
      expect(result.classification).toBeDefined();
    });

    it("should have correct BulkSafetyCheckResponse structure", async () => {
      vi.mocked(worksheetStorage.getWorksheet).mockResolvedValue(null);

      const result: BulkSafetyCheckResponse = await import(
        "@/lib/server/adminSafetyCheck"
      ).then((m) =>
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        m.bulkCheckSafety({} as any, []),
      );

      expect(result.results).toBeDefined();
      expect(result.summary).toBeDefined();
      expect(Array.isArray(result.results)).toBe(true);
      expect(typeof result.summary.total).toBe("number");
      expect(typeof result.summary.safe).toBe("number");
      expect(typeof result.summary.flagged).toBe("number");
      expect(typeof result.summary.errors).toBe("number");
      expect(typeof result.summary.duration).toBe("number");
    });
  });

  describe("Edge cases and error scenarios", () => {
    it("should handle undefined AI binding", async () => {
      const mockWorksheet: SharedWorksheet = {
        id: "test-ws",
        type: "explicit",
        title: "Test",
        grades: ["K"],
        problems: [],
        createdAt: "2025-01-10T00:00:00.000Z",
        stats: {
          views: 0,
          downloads: 0,
          averageRating: 0,
          ratingCount: 0,
        },
      };

      vi.mocked(aiSafety.checkContentSafetyWithAI).mockResolvedValue({
        safe: true,
        classification: "safe",
        confidence: 0.75,
        usingAI: false,
        fallback: true,
      });

      const result = await checkWorksheetSafety({}, mockWorksheet);

      expect(result.safe).toBe(true);
      expect(result.fallback).toBe(true);
    });

    it("should maintain summary accuracy with mixed results", async () => {
      const worksheets: Record<string, SharedWorksheet> = {
        safe1: {
          id: "safe1",
          type: "explicit",
          title: "Safe 1",
          grades: ["K"],
          problems: [],
          createdAt: "2025-01-10T00:00:00.000Z",
          stats: {
            views: 0,
            downloads: 0,
            averageRating: 0,
            ratingCount: 0,
          },
        },
        flagged1: {
          id: "flagged1",
          type: "explicit",
          title: "Flagged 1",
          grades: ["1"],
          problems: [],
          createdAt: "2025-01-10T00:00:00.000Z",
          stats: {
            views: 0,
            downloads: 0,
            averageRating: 0,
            ratingCount: 0,
          },
        },
        safe2: {
          id: "safe2",
          type: "explicit",
          title: "Safe 2",
          grades: ["2"],
          problems: [],
          createdAt: "2025-01-10T00:00:00.000Z",
          stats: {
            views: 0,
            downloads: 0,
            averageRating: 0,
            ratingCount: 0,
          },
        },
      };

      vi.mocked(worksheetStorage.getWorksheet).mockImplementation(
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        async (ctx: any, id: string) => worksheets[id] || null,
      );

      vi.mocked(aiSafety.checkContentSafetyWithAI).mockImplementation(
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        async (env: any, content: any) => ({
          safe: !content.title.includes("Flagged"),
          classification: content.title.includes("Flagged")
            ? ("unsafe" as const)
            : ("safe" as const),
          confidence: 0.9,
          usingAI: true,
        }),
      );

      const env = { AI: {}, KV: {} };
      const result = await bulkCheckSafety(env, ["safe1", "flagged1", "safe2"]);

      expect(result.summary.total).toBe(3);
      expect(result.summary.safe).toBe(2);
      expect(result.summary.flagged).toBe(1);
      expect(result.summary.errors).toBe(0);
      expect(result.results.length).toBe(3);
    });
  });
});
