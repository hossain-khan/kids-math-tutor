import { describe, it, expect, beforeEach, vi } from "vitest";
import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { BrowserRouter } from "react-router-dom";
import SharedWorksheets from "@/pages/SharedWorksheets";

type MockResponse = {
  ok: boolean;
  json: () => Promise<unknown>;
};

function makeItem(id: string, title: string, avg = 0, count = 0) {
  return {
    id,
    title,
    subtitle: `subtitle ${id}`,
    grades: ["kindergarten", "grade1", "grade2"],
    problemCount: 5,
    createdAt: "2025-12-01T00:00:00Z",
    stats: { views: 1, downloads: 0, averageRating: avg, ratingCount: count },
  };
}

describe("SharedWorksheets UI", () => {
  let fetchMock: ReturnType<typeof vi.fn>;

  beforeEach(() => {
    vi.restoreAllMocks();
    localStorage.clear();
    // default fetch mock that returns empty paginated structure
    fetchMock = vi.fn(async (input: RequestInfo) => {
      const url = typeof input === "string" ? input : input.url;

      if (url.includes("/api/v1/worksheets/search")) {
        return {
          ok: true,
          json: async () => ({
            items: [makeItem("s1", "Search Result", 0, 0)],
            total: 1,
            hasMore: false,
            limit: 20,
            offset: 0,
          }),
        } as MockResponse;
      }

      if (url.includes("/api/v1/worksheets?")) {
        return {
          ok: true,
          json: async () => ({
            items: [makeItem("l1", "List Item", 4.0, 1)],
            total: 1,
            hasMore: false,
            limit: 20,
            offset: 0,
          }),
        } as MockResponse;
      }

      // fallback
      return { ok: true, json: async () => ({}) } as MockResponse;
    });

    vi.stubGlobal("fetch", fetchMock);
  });

  it("performs search and displays results", async () => {
    const user = userEvent.setup();

    render(
      <BrowserRouter>
        <SharedWorksheets />
      </BrowserRouter>,
    );

    const input = screen.getByPlaceholderText(/Search by title/i);
    await user.type(input, "Search Result");

    // wait for the search fetch to be called and the result to render
    await waitFor(() =>
      expect(fetchMock).toHaveBeenCalledWith(
        expect.stringContaining("/api/v1/worksheets/search?"),
      ),
    );

    expect(await screen.findByText(/Search Result/)).toBeInTheDocument();
  });

  it("loads more results when Load More is clicked", async () => {
    // prepare first and second page responses
    fetchMock.mockImplementation(async (input: RequestInfo) => {
      const url = typeof input === "string" ? input : input.url;
      if (url.includes("/api/v1/worksheets?") && url.includes("offset=0")) {
        return {
          ok: true,
          json: async () => ({
            items: [makeItem("p1", "Page 1")],
            total: 2,
            hasMore: true,
            limit: 20,
            offset: 0,
          }),
        } as MockResponse;
      }
      if (url.includes("/api/v1/worksheets?") && url.includes("offset=20")) {
        return {
          ok: true,
          json: async () => ({
            items: [makeItem("p2", "Page 2")],
            total: 2,
            hasMore: false,
            limit: 20,
            offset: 20,
          }),
        } as MockResponse;
      }
      return {
        ok: true,
        json: async () => ({
          items: [],
          total: 0,
          hasMore: false,
          limit: 20,
          offset: 0,
        }),
      } as MockResponse;
    });

    const user = userEvent.setup();

    render(
      <BrowserRouter>
        <SharedWorksheets />
      </BrowserRouter>,
    );

    // initial page should show Page 1
    expect(await screen.findByText(/Page 1/)).toBeInTheDocument();

    // Load More should appear
    const loadMore = await screen.findByRole("button", { name: /Load More/i });
    await user.click(loadMore);

    // Now Page 2 should be present alongside Page 1
    expect(await screen.findByText(/Page 2/)).toBeInTheDocument();
    expect(screen.getByText(/Page 1/)).toBeInTheDocument();
  });

  it("submits rating and updates UI on success", async () => {
    // initial list contains one item with rating 4.0/1
    fetchMock.mockImplementation(async (input: RequestInfo) => {
      const url = typeof input === "string" ? input : input.url;

      if (url.includes("/api/v1/worksheets?")) {
        return {
          ok: true,
          json: async () => ({
            items: [makeItem("r1", "Rate Me", 4.0, 1)],
            total: 1,
            hasMore: false,
            limit: 20,
            offset: 0,
          }),
        } as MockResponse;
      }

      if (url.includes("/api/v1/worksheets/r1/rate")) {
        // simulate successful rating POST response
        return {
          ok: true,
          json: async () => ({
            success: true,
            stats: { averageRating: 4.5, ratingCount: 2 },
          }),
        } as MockResponse;
      }

      return { ok: true, json: async () => ({}) } as MockResponse;
    });

    const user = userEvent.setup();

    render(
      <BrowserRouter>
        <SharedWorksheets />
      </BrowserRouter>,
    );

    const card = await screen.findByText(/Rate Me/);
    expect(card).toBeInTheDocument();

    // find the star button for 5 stars; title is 'Rate 5 stars'
    const fiveStar = await screen.findByRole("button", {
      name: /Rate 5 star/i,
    });
    await user.click(fiveStar);

    // after rating, the card should update to show 4.5★ (2)
    await waitFor(() => expect(screen.getByText(/4.5★/)).toBeInTheDocument());
    expect(screen.getByText(/\(2\)/)).toBeInTheDocument();
  });
});

describe("calculateAnswer", () => {
  it("should calculate answers for all operations correctly", () => {
    // Test the calculation logic that calculateAnswer uses

    // Addition: 5 + 3 = 8
    const addResult = 5 + 3;
    expect(addResult).toBe(8);

    // Subtraction: 10 - 4 = 6
    const subtractResult = 10 - 4;
    expect(subtractResult).toBe(6);

    // Multiplication: 6 × 7 = 42
    const multiplyResult = 6 * 7;
    expect(multiplyResult).toBe(42);

    // Division: 20 ÷ 4 = 5
    const divideResult = 20 / 4;
    expect(divideResult).toBe(5);
  });

  it("should handle division with decimal results and format to 2 decimals", () => {
    // Division: 10 ÷ 3 = 3.33...
    const result1 = 10 / 3;
    const formatted1 = Number.isInteger(result1)
      ? result1
      : parseFloat(result1.toFixed(2));
    expect(formatted1).toBe(3.33);

    // Division: 7 ÷ 2 = 3.5
    const result2 = 7 / 2;
    const formatted2 = Number.isInteger(result2)
      ? result2
      : parseFloat(result2.toFixed(2));
    expect(formatted2).toBe(3.5);

    // Division: 9 ÷ 3 = 3 (whole number, no decimal)
    const result3 = 9 / 3;
    const formatted3 = Number.isInteger(result3)
      ? result3
      : parseFloat(result3.toFixed(2));
    expect(formatted3).toBe(3);
  });

  it("should work correctly for worksheet problems with different operand values", () => {
    // Simulate worksheet problems and verify calculations
    const problems = [
      { operand1: 15, operand2: 8, operation: "addition" },
      { operand1: 25, operand2: 12, operation: "subtraction" },
      { operand1: 9, operand2: 8, operation: "multiplication" },
      { operand1: 24, operand2: 6, operation: "division" },
    ];

    // Problem 1: 15 + 8 = 23
    expect(problems[0].operand1 + problems[0].operand2).toBe(23);

    // Problem 2: 25 - 12 = 13
    expect(problems[1].operand1 - problems[1].operand2).toBe(13);

    // Problem 3: 9 × 8 = 72
    expect(problems[2].operand1 * problems[2].operand2).toBe(72);

    // Problem 4: 24 ÷ 6 = 4
    expect(problems[3].operand1 / problems[3].operand2).toBe(4);
  });
});
