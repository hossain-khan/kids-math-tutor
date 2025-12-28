import { describe, it, expect, vi } from "vitest";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import WorksheetCard from "@/components/WorksheetCard";
import { AdminWorksheet } from "@/lib/reducers/adminReducer";

function createMockWorksheet(
  overrides?: Partial<AdminWorksheet>,
): AdminWorksheet {
  return {
    id: "test-1",
    type: "explicit",
    title: "Test Worksheet",
    subtitle: "Test Subtitle",
    description: "Test Description",
    problemCount: 10,
    createdAt: "2025-12-01T00:00:00Z",
    stats: {
      views: 100,
      downloads: 50,
      averageRating: 4.5,
      ratingCount: 20,
    },
    ...overrides,
  };
}

describe("WorksheetCard", () => {
  const mockOnDelete = vi.fn();
  const mockOnConfirmDelete = vi.fn();
  const mockOnToggleExpandedSafety = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("should render worksheet title", () => {
    const worksheet = createMockWorksheet({
      title: "Addition Practice",
    });

    render(
      <WorksheetCard
        worksheet={worksheet}
        onDelete={mockOnDelete}
        onConfirmDelete={mockOnConfirmDelete}
        deleteConfirm={null}
        deleting={null}
        expandedSafety={null}
        onToggleExpandedSafety={mockOnToggleExpandedSafety}
      />,
    );

    expect(screen.getByText("Addition Practice")).toBeInTheDocument();
  });

  it("should render subtitle if provided", () => {
    const worksheet = createMockWorksheet({
      subtitle: "Numbers 1-20",
    });

    render(
      <WorksheetCard
        worksheet={worksheet}
        onDelete={mockOnDelete}
        onConfirmDelete={mockOnConfirmDelete}
        deleteConfirm={null}
        deleting={null}
        expandedSafety={null}
        onToggleExpandedSafety={mockOnToggleExpandedSafety}
      />,
    );

    expect(screen.getByText("Numbers 1-20")).toBeInTheDocument();
  });

  it("should not render subtitle if not provided", () => {
    const worksheet = createMockWorksheet({ subtitle: undefined });

    const { container } = render(
      <WorksheetCard
        worksheet={worksheet}
        onDelete={mockOnDelete}
        onConfirmDelete={mockOnConfirmDelete}
        deleteConfirm={null}
        deleting={null}
        expandedSafety={null}
        onToggleExpandedSafety={mockOnToggleExpandedSafety}
      />,
    );

    const subtitleElements = container.querySelectorAll(".text-gray-600");
    const hasSubtitle = Array.from(subtitleElements).some((el) =>
      el.textContent?.includes("Test Worksheet"),
    );
    expect(hasSubtitle).toBe(false);
  });

  it("should display problem count", () => {
    const worksheet = createMockWorksheet({ problemCount: 15 });

    render(
      <WorksheetCard
        worksheet={worksheet}
        onDelete={mockOnDelete}
        onConfirmDelete={mockOnConfirmDelete}
        deleteConfirm={null}
        deleting={null}
        expandedSafety={null}
        onToggleExpandedSafety={mockOnToggleExpandedSafety}
      />,
    );

    expect(screen.getByText(/📊 15 problems/)).toBeInTheDocument();
  });

  it("should display stats correctly", () => {
    const worksheet = createMockWorksheet({
      stats: {
        views: 250,
        downloads: 120,
        averageRating: 4.8,
        ratingCount: 50,
      },
    });

    render(
      <WorksheetCard
        worksheet={worksheet}
        onDelete={mockOnDelete}
        onConfirmDelete={mockOnConfirmDelete}
        deleteConfirm={null}
        deleting={null}
        expandedSafety={null}
        onToggleExpandedSafety={mockOnToggleExpandedSafety}
      />,
    );

    expect(screen.getByText(/👁️ 250 views/)).toBeInTheDocument();
    expect(screen.getByText(/💾 120 downloads/)).toBeInTheDocument();
    expect(screen.getByText(/⭐ 4.8 rating/)).toBeInTheDocument();
  });

  it("should display formatted date", () => {
    const worksheet = createMockWorksheet({
      createdAt: "2025-12-25T00:00:00Z",
    });

    render(
      <WorksheetCard
        worksheet={worksheet}
        onDelete={mockOnDelete}
        onConfirmDelete={mockOnConfirmDelete}
        deleteConfirm={null}
        deleting={null}
        expandedSafety={null}
        onToggleExpandedSafety={mockOnToggleExpandedSafety}
      />,
    );

    expect(screen.getByText(/📅/)).toBeInTheDocument();
  });

  it("should display description if provided", () => {
    const worksheet = createMockWorksheet({
      description: "This is a custom worksheet",
    });

    render(
      <WorksheetCard
        worksheet={worksheet}
        onDelete={mockOnDelete}
        onConfirmDelete={mockOnConfirmDelete}
        deleteConfirm={null}
        deleting={null}
        expandedSafety={null}
        onToggleExpandedSafety={mockOnToggleExpandedSafety}
      />,
    );

    expect(screen.getByText("This is a custom worksheet")).toBeInTheDocument();
  });

  it("should display safe badge when safety status is ok", () => {
    const worksheet = createMockWorksheet({
      safety: { isFlagged: false },
    });

    render(
      <WorksheetCard
        worksheet={worksheet}
        onDelete={mockOnDelete}
        onConfirmDelete={mockOnConfirmDelete}
        deleteConfirm={null}
        deleting={null}
        expandedSafety={null}
        onToggleExpandedSafety={mockOnToggleExpandedSafety}
      />,
    );

    expect(screen.getByText(/✅ Safe/)).toBeInTheDocument();
  });

  it("should display flagged badge when safety status is flagged", () => {
    const worksheet = createMockWorksheet({
      safety: { isFlagged: true },
    });

    render(
      <WorksheetCard
        worksheet={worksheet}
        onDelete={mockOnDelete}
        onConfirmDelete={mockOnConfirmDelete}
        deleteConfirm={null}
        deleting={null}
        expandedSafety={null}
        onToggleExpandedSafety={mockOnToggleExpandedSafety}
      />,
    );

    expect(screen.getByText(/⚠️ Flagged/)).toBeInTheDocument();
  });

  it("should have view link to worksheet", () => {
    const worksheet = createMockWorksheet({ id: "ws-123" });

    render(
      <WorksheetCard
        worksheet={worksheet}
        onDelete={mockOnDelete}
        onConfirmDelete={mockOnConfirmDelete}
        deleteConfirm={null}
        deleting={null}
        expandedSafety={null}
        onToggleExpandedSafety={mockOnToggleExpandedSafety}
      />,
    );

    const viewLink = screen.getByRole("link", { name: /View/ });
    expect(viewLink).toHaveAttribute("href", "/worksheets/ws-123");
    expect(viewLink).toHaveAttribute("target", "_blank");
  });

  it("should show delete button initially", () => {
    const worksheet = createMockWorksheet();

    render(
      <WorksheetCard
        worksheet={worksheet}
        onDelete={mockOnDelete}
        onConfirmDelete={mockOnConfirmDelete}
        deleteConfirm={null}
        deleting={null}
        expandedSafety={null}
        onToggleExpandedSafety={mockOnToggleExpandedSafety}
      />,
    );

    const deleteButton = screen.getByRole("button", { name: /Delete/ });
    expect(deleteButton).toBeInTheDocument();
  });

  it("should call onDelete when delete button clicked", async () => {
    const user = userEvent.setup();
    const worksheet = createMockWorksheet({ id: "ws-123" });

    render(
      <WorksheetCard
        worksheet={worksheet}
        onDelete={mockOnDelete}
        onConfirmDelete={mockOnConfirmDelete}
        deleteConfirm={null}
        deleting={null}
        expandedSafety={null}
        onToggleExpandedSafety={mockOnToggleExpandedSafety}
      />,
    );

    const deleteButton = screen.getByRole("button", { name: /Delete/ });
    await user.click(deleteButton);

    expect(mockOnDelete).toHaveBeenCalledWith("ws-123");
  });

  it("should show confirm/cancel buttons when delete confirmed", () => {
    const worksheet = createMockWorksheet({ id: "ws-123" });

    render(
      <WorksheetCard
        worksheet={worksheet}
        onDelete={mockOnDelete}
        onConfirmDelete={mockOnConfirmDelete}
        deleteConfirm="ws-123"
        deleting={null}
        expandedSafety={null}
        onToggleExpandedSafety={mockOnToggleExpandedSafety}
      />,
    );

    expect(screen.getByRole("button", { name: /Confirm/ })).toBeInTheDocument();
    expect(screen.getByRole("button", { name: /Cancel/ })).toBeInTheDocument();
  });

  it("should call onConfirmDelete when confirm button clicked", async () => {
    const user = userEvent.setup();
    const worksheet = createMockWorksheet({ id: "ws-123" });

    render(
      <WorksheetCard
        worksheet={worksheet}
        onDelete={mockOnDelete}
        onConfirmDelete={mockOnConfirmDelete}
        deleteConfirm="ws-123"
        deleting={null}
        expandedSafety={null}
        onToggleExpandedSafety={mockOnToggleExpandedSafety}
      />,
    );

    const confirmButton = screen.getByRole("button", { name: /Confirm/ });
    await user.click(confirmButton);

    expect(mockOnConfirmDelete).toHaveBeenCalledWith("ws-123");
  });

  it("should call onDelete with empty string when cancel clicked", async () => {
    const user = userEvent.setup();
    const worksheet = createMockWorksheet({ id: "ws-123" });

    render(
      <WorksheetCard
        worksheet={worksheet}
        onDelete={mockOnDelete}
        onConfirmDelete={mockOnConfirmDelete}
        deleteConfirm="ws-123"
        deleting={null}
        expandedSafety={null}
        onToggleExpandedSafety={mockOnToggleExpandedSafety}
      />,
    );

    const cancelButton = screen.getByRole("button", { name: /Cancel/ });
    await user.click(cancelButton);

    expect(mockOnDelete).toHaveBeenCalledWith("");
  });

  it("should show details button when flagged", () => {
    const worksheet = createMockWorksheet({
      safety: { isFlagged: true },
    });

    render(
      <WorksheetCard
        worksheet={worksheet}
        onDelete={mockOnDelete}
        onConfirmDelete={mockOnConfirmDelete}
        deleteConfirm={null}
        deleting={null}
        expandedSafety={null}
        onToggleExpandedSafety={mockOnToggleExpandedSafety}
      />,
    );

    expect(
      screen.getByRole("button", { name: /Show Details/ }),
    ).toBeInTheDocument();
  });

  it("should call onToggleExpandedSafety when details button clicked", async () => {
    const user = userEvent.setup();
    const worksheet = createMockWorksheet({
      id: "ws-123",
      safety: { isFlagged: true },
    });

    render(
      <WorksheetCard
        worksheet={worksheet}
        onDelete={mockOnDelete}
        onConfirmDelete={mockOnConfirmDelete}
        deleteConfirm={null}
        deleting={null}
        expandedSafety={null}
        onToggleExpandedSafety={mockOnToggleExpandedSafety}
      />,
    );

    const detailsButton = screen.getByRole("button", { name: /Show Details/ });
    await user.click(detailsButton);

    expect(mockOnToggleExpandedSafety).toHaveBeenCalledWith("ws-123");
  });

  it("should show hide details when expanded", () => {
    const worksheet = createMockWorksheet({
      id: "ws-123",
      safety: { isFlagged: true },
    });

    render(
      <WorksheetCard
        worksheet={worksheet}
        onDelete={mockOnDelete}
        onConfirmDelete={mockOnConfirmDelete}
        deleteConfirm={null}
        deleting={null}
        expandedSafety="ws-123"
        onToggleExpandedSafety={mockOnToggleExpandedSafety}
      />,
    );

    expect(
      screen.getByRole("button", { name: /Hide Details/ }),
    ).toBeInTheDocument();
  });

  it("should display safety details when expanded", () => {
    const worksheet = createMockWorksheet({
      id: "ws-123",
      safety: {
        isFlagged: true,
        categories: ["violence", "profanity"],
        explanation: "Contains inappropriate language",
        method: "AI-based",
        confidence: 95,
        lastChecked: "2025-12-01T10:00:00Z",
      },
    });

    render(
      <WorksheetCard
        worksheet={worksheet}
        onDelete={mockOnDelete}
        onConfirmDelete={mockOnConfirmDelete}
        deleteConfirm={null}
        deleting={null}
        expandedSafety="ws-123"
        onToggleExpandedSafety={mockOnToggleExpandedSafety}
      />,
    );

    expect(screen.getByText(/⚠️ Safety Issues Found/)).toBeInTheDocument();
    expect(screen.getByText("violence")).toBeInTheDocument();
    expect(screen.getByText("profanity")).toBeInTheDocument();
    expect(
      screen.getByText(/Contains inappropriate language/),
    ).toBeInTheDocument();
    expect(screen.getByText(/AI-based/)).toBeInTheDocument();
  });

  it("should not display safety details when not expanded", () => {
    const worksheet = createMockWorksheet({
      id: "ws-123",
      safety: {
        isFlagged: true,
        explanation: "Contains inappropriate language",
      },
    });

    render(
      <WorksheetCard
        worksheet={worksheet}
        onDelete={mockOnDelete}
        onConfirmDelete={mockOnConfirmDelete}
        deleteConfirm={null}
        deleting={null}
        expandedSafety={null}
        onToggleExpandedSafety={mockOnToggleExpandedSafety}
      />,
    );

    expect(
      screen.queryByText(/⚠️ Safety Issues Found/),
    ).not.toBeInTheDocument();
    expect(
      screen.queryByText(/Contains inappropriate language/),
    ).not.toBeInTheDocument();
  });

  it("should disable buttons during delete operation", () => {
    const worksheet = createMockWorksheet({ id: "ws-123" });

    render(
      <WorksheetCard
        worksheet={worksheet}
        onDelete={mockOnDelete}
        onConfirmDelete={mockOnConfirmDelete}
        deleteConfirm="ws-123"
        deleting="ws-123"
        expandedSafety={null}
        onToggleExpandedSafety={mockOnToggleExpandedSafety}
      />,
    );

    const confirmButton = screen.getByRole("button", { name: /Deleting/ });
    const cancelButton = screen.getByRole("button", { name: /Cancel/ });

    expect(confirmButton).toBeDisabled();
    expect(cancelButton).toBeDisabled();
  });
});
