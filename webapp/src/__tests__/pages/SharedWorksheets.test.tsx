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
