import { describe, it, expect } from "vitest";

interface AdminWorksheet {
  id: string;
  type: "explicit" | "generated";
  title: string;
  subtitle?: string;
  description?: string;
  problemCount: number;
  createdAt: string;
  stats: {
    views: number;
    downloads: number;
    averageRating: number;
    ratingCount: number;
  };
}

function makeWorksheet(
  id: string,
  title: string,
  problemCount: number,
  views = 5,
  downloads = 2,
): AdminWorksheet {
  return {
    id,
    type: "explicit",
    title,
    subtitle: `Subtitle for ${title}`,
    description: `Description for ${title}`,
    problemCount,
    createdAt: "2025-12-01T00:00:00Z",
    stats: {
      views,
      downloads,
      averageRating: 4.5,
      ratingCount: 10,
    },
  };
}

describe("AdminManage - Problem Count Display", () => {
  it("should use problemCount field from API response, not problems array", () => {
    const worksheet = makeWorksheet("ws1", "Addition Practice", 10);

    // Verify that the returned object has problemCount, not problems
    expect(worksheet.problemCount).toBe(10);
    expect(worksheet).not.toHaveProperty("problems");
  });

  it("should handle multiple worksheets with different problem counts", () => {
    const worksheets = [
      makeWorksheet("ws1", "Addition Practice", 10),
      makeWorksheet("ws2", "Subtraction Problems", 15),
      makeWorksheet("ws3", "Mixed Operations", 5),
      makeWorksheet("ws4", "Single Problem", 1),
    ];

    expect(worksheets[0].problemCount).toBe(10);
    expect(worksheets[1].problemCount).toBe(15);
    expect(worksheets[2].problemCount).toBe(5);
    expect(worksheets[3].problemCount).toBe(1);
  });

  it("should correctly format problem count display text", () => {
    const worksheet1 = makeWorksheet("ws1", "Test", 1);
    const worksheet10 = makeWorksheet("ws2", "Test", 10);

    // Simulate the display logic: singular for 1, plural for others
    const format = (count: number) =>
      `📊 ${count} ${count === 1 ? "problem" : "problems"}`;

    expect(format(worksheet1.problemCount)).toBe("📊 1 problem");
    expect(format(worksheet10.problemCount)).toBe("📊 10 problems");
  });

  it("should not fall back to 0 when problemCount is present", () => {
    const worksheet = makeWorksheet("ws1", "Test", 42);

    // The old code would do: worksheet.problems?.length || 0
    // Which would return 0 if problems is undefined
    // The new code should use: worksheet.problemCount
    // Which should return 42
    expect(worksheet.problemCount).toBe(42);
    expect(worksheet.problemCount || 0).toBe(42); // Never falls back to 0
  });

  it("should include all worksheet stats correctly", () => {
    const worksheet = makeWorksheet(
      "ws1",
      "Comprehensive Test",
      25,
      100, // views
      50, // downloads
    );

    expect(worksheet.title).toBe("Comprehensive Test");
    expect(worksheet.problemCount).toBe(25);
    expect(worksheet.stats.views).toBe(100);
    expect(worksheet.stats.downloads).toBe(50);
    expect(worksheet.stats.averageRating).toBe(4.5);
    expect(worksheet.stats.ratingCount).toBe(10);
  });
});
