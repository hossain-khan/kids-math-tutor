/**
 * Unit tests for worksheet deletion functionality
 */
import { describe, it, expect, beforeEach } from "vitest";
import {
  saveWorksheet,
  getWorksheet,
  deleteWorksheet,
  type SharedWorksheet,
} from "@/lib/server/worksheetStorage";

describe("worksheetStorage - deletion", () => {
  let mockKV: Map<string, string>;
  let kvContext: { env: { KV: unknown } };

  beforeEach(() => {
    mockKV = new Map();

    // Mock KV methods
    kvContext = {
      env: {
        KV: {
          get: async (key: string, type?: string) => {
            const value = mockKV.get(key);
            if (!value) return null;
            return type === "json" ? JSON.parse(value) : value;
          },
          put: async (key: string, value: string) => {
            mockKV.set(key, value);
          },
          delete: async (key: string) => {
            mockKV.delete(key);
          },
          list: async (options?: { prefix?: string }) => {
            const keys = Array.from(mockKV.keys())
              .filter((key) =>
                options?.prefix ? key.startsWith(options.prefix) : true,
              )
              .map((name) => ({ name }));
            return { keys };
          },
        },
      },
    };
  });

  it("should delete a worksheet", async () => {
    const worksheet: SharedWorksheet = {
      id: "test-ws-1",
      type: "explicit",
      title: "Test Worksheet",
      subtitle: "Subtitle",
      description: "Description",
      grades: ["grade1"],
      problems: [{ operand1: 1, operand2: 2, operation: "addition" }],
      createdAt: new Date().toISOString(),
      creatorSessionId: "session-123",
      stats: { views: 0, downloads: 0, averageRating: 0, ratingCount: 0 },
    };

    await saveWorksheet(kvContext, worksheet);

    // Verify it exists
    let retrieved = await getWorksheet(kvContext, "test-ws-1");
    expect(retrieved).toBeTruthy();
    expect(retrieved?.title).toBe("Test Worksheet");

    // Delete it
    const success = await deleteWorksheet(kvContext, "test-ws-1");
    expect(success).toBe(true);

    // Verify it's gone
    retrieved = await getWorksheet(kvContext, "test-ws-1");
    expect(retrieved).toBeNull();
  });

  it("should delete associated ratings when deleting a worksheet", async () => {
    const worksheet: SharedWorksheet = {
      id: "test-ws-2",
      type: "explicit",
      title: "Test Worksheet 2",
      grades: ["grade1"],
      problems: [{ operand1: 1, operand2: 2, operation: "addition" }],
      createdAt: new Date().toISOString(),
      creatorSessionId: "session-123",
      stats: { views: 0, downloads: 0, averageRating: 0, ratingCount: 0 },
    };

    await saveWorksheet(kvContext, worksheet);

    // Add some ratings
    await kvContext.env.KV.put(
      "rating:test-ws-2:user1",
      JSON.stringify({ rating: 5, timestamp: new Date().toISOString() }),
    );
    await kvContext.env.KV.put(
      "rating:test-ws-2:user2",
      JSON.stringify({ rating: 4, timestamp: new Date().toISOString() }),
    );

    // Verify ratings exist
    expect(mockKV.has("rating:test-ws-2:user1")).toBe(true);
    expect(mockKV.has("rating:test-ws-2:user2")).toBe(true);

    // Delete worksheet
    await deleteWorksheet(kvContext, "test-ws-2");

    // Verify ratings are also deleted
    expect(mockKV.has("rating:test-ws-2:user1")).toBe(false);
    expect(mockKV.has("rating:test-ws-2:user2")).toBe(false);
  });

  it("should handle deleting non-existent worksheet gracefully", async () => {
    const success = await deleteWorksheet(kvContext, "non-existent-ws");
    expect(success).toBe(true); // Should still return true even if nothing to delete
  });

  it("should store and retrieve creatorSessionId", async () => {
    const worksheet: SharedWorksheet = {
      id: "test-ws-3",
      type: "explicit",
      title: "Test Worksheet 3",
      grades: ["grade1"],
      problems: [{ operand1: 1, operand2: 2, operation: "addition" }],
      createdAt: new Date().toISOString(),
      creatorSessionId: "session-456",
      stats: { views: 0, downloads: 0, averageRating: 0, ratingCount: 0 },
    };

    await saveWorksheet(kvContext, worksheet);

    const retrieved = await getWorksheet(kvContext, "test-ws-3");
    expect(retrieved).toBeTruthy();
    expect(retrieved?.creatorSessionId).toBe("session-456");
  });

  it("should allow worksheets without creatorSessionId", async () => {
    const worksheet: SharedWorksheet = {
      id: "test-ws-4",
      type: "explicit",
      title: "Test Worksheet 4",
      grades: ["grade1"],
      problems: [{ operand1: 1, operand2: 2, operation: "addition" }],
      createdAt: new Date().toISOString(),
      // No creatorSessionId - for backwards compatibility
      stats: { views: 0, downloads: 0, averageRating: 0, ratingCount: 0 },
    };

    await saveWorksheet(kvContext, worksheet);

    const retrieved = await getWorksheet(kvContext, "test-ws-4");
    expect(retrieved).toBeTruthy();
    expect(retrieved?.creatorSessionId).toBeUndefined();
  });
});
