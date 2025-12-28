import { describe, it, expect, vi } from "vitest";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import DeleteConfirmationDialog from "@/components/DeleteConfirmationDialog";

describe("DeleteConfirmationDialog", () => {
  const defaultProps = {
    title: "Addition Practice",
    onConfirm: vi.fn(),
    onCancel: vi.fn(),
  };

  it("should render dialog with title", () => {
    render(<DeleteConfirmationDialog {...defaultProps} />);

    expect(screen.getByText("Delete Worksheet?")).toBeInTheDocument();
    expect(screen.getByText(defaultProps.title)).toBeInTheDocument();
  });

  it("should render default warning message", () => {
    render(<DeleteConfirmationDialog {...defaultProps} />);

    expect(
      screen.getByText("This action cannot be undone."),
    ).toBeInTheDocument();
  });

  it("should render custom warning message", () => {
    const customWarning = "Custom warning message";
    render(
      <DeleteConfirmationDialog
        {...defaultProps}
        warningMessage={customWarning}
      />,
    );

    expect(screen.getByText(customWarning)).toBeInTheDocument();
  });

  it("should render subtitle when provided", () => {
    const subtitle = "For ages 6-8";
    render(<DeleteConfirmationDialog {...defaultProps} subtitle={subtitle} />);

    expect(screen.getByText(subtitle)).toBeInTheDocument();
  });

  it("should not render subtitle when not provided", () => {
    const { container } = render(
      <DeleteConfirmationDialog {...defaultProps} />,
    );

    const subtitleSection = container.querySelector(
      ".border-t.border-gray-300",
    );
    expect(subtitleSection).not.toBeInTheDocument();
  });

  it("should call onCancel when cancel button is clicked", async () => {
    const onCancel = vi.fn();
    const user = userEvent.setup();

    render(<DeleteConfirmationDialog {...defaultProps} onCancel={onCancel} />);

    const cancelButton = screen.getByTestId("cancel-delete-button");
    await user.click(cancelButton);

    expect(onCancel).toHaveBeenCalledOnce();
  });

  it("should call onConfirm when delete button is clicked", async () => {
    const onConfirm = vi.fn();
    const user = userEvent.setup();

    render(
      <DeleteConfirmationDialog {...defaultProps} onConfirm={onConfirm} />,
    );

    const deleteButton = screen.getByTestId("confirm-delete-button");
    await user.click(deleteButton);

    expect(onConfirm).toHaveBeenCalledOnce();
  });

  it("should show deleting state", () => {
    render(<DeleteConfirmationDialog {...defaultProps} isDeleting={true} />);

    expect(screen.getByText("Deleting...")).toBeInTheDocument();
  });

  it("should show delete button text when not deleting", () => {
    render(<DeleteConfirmationDialog {...defaultProps} isDeleting={false} />);

    expect(screen.getByText("Delete")).toBeInTheDocument();
  });

  it("should disable buttons during deletion", () => {
    render(<DeleteConfirmationDialog {...defaultProps} isDeleting={true} />);

    const cancelButton = screen.getByTestId("cancel-delete-button");
    const deleteButton = screen.getByTestId("confirm-delete-button");

    expect(cancelButton).toBeDisabled();
    expect(deleteButton).toBeDisabled();
  });

  it("should not disable buttons when not deleting", () => {
    render(<DeleteConfirmationDialog {...defaultProps} isDeleting={false} />);

    const cancelButton = screen.getByTestId("cancel-delete-button");
    const deleteButton = screen.getByTestId("confirm-delete-button");

    expect(cancelButton).not.toBeDisabled();
    expect(deleteButton).not.toBeDisabled();
  });

  it("should display helper text", () => {
    render(<DeleteConfirmationDialog {...defaultProps} />);

    expect(
      screen.getByText("All associated data will be permanently removed"),
    ).toBeInTheDocument();
  });

  it("should display alarm/warning icon", () => {
    const { container } = render(
      <DeleteConfirmationDialog {...defaultProps} />,
    );

    // Check for SVG icon (AlertTriangle)
    const icon = container.querySelector("svg");
    expect(icon).toBeInTheDocument();
    expect(icon).toHaveClass("text-red-600");
  });

  it("should handle long worksheet titles", () => {
    const longTitle =
      "This is a very long worksheet title that might wrap to multiple lines in the dialog";

    render(<DeleteConfirmationDialog {...defaultProps} title={longTitle} />);

    expect(screen.getByText(longTitle)).toBeInTheDocument();
  });

  it("should be accessible with proper labels and structure", () => {
    const { container } = render(
      <DeleteConfirmationDialog {...defaultProps} />,
    );

    // Check for modal structure
    expect(container.querySelector('[aria-hidden="true"]')).toBeInTheDocument();

    // Check for buttons are accessible
    expect(screen.getByTestId("cancel-delete-button")).toBeInTheDocument();
    expect(screen.getByTestId("confirm-delete-button")).toBeInTheDocument();
  });

  it("should render all content in a card component", () => {
    const { container } = render(
      <DeleteConfirmationDialog {...defaultProps} />,
    );

    // Card should have max-w-md and be centered
    const card = container.querySelector(".max-w-md");
    expect(card).toBeInTheDocument();
  });
});
