import { describe, it, expect, vi, afterEach } from "vitest";
import { render, screen, waitFor } from "@testing-library/react";
import Toast from "@/components/Toast";

describe("Toast Component", () => {
  afterEach(() => {
    vi.clearAllMocks();
  });

  it("should not be visible when isVisible is false", () => {
    render(
      <Toast message="Test message" isVisible={false} onClose={() => {}} />,
    );
    expect(screen.queryByText("Test message")).not.toBeInTheDocument();
  });

  it("should be visible when isVisible is true", () => {
    render(
      <Toast message="Test message" isVisible={true} onClose={() => {}} />,
    );
    expect(screen.getByText("Test message")).toBeInTheDocument();
  });

  it("should call onClose after the duration", async () => {
    const onClose = vi.fn();
    render(
      <Toast
        message="Test message"
        isVisible={true}
        onClose={onClose}
        duration={100}
      />,
    );
    await waitFor(() => {
      expect(onClose).toHaveBeenCalled();
    });
  });

  it("should render success variant correctly", () => {
    render(
      <Toast
        message="Success"
        isVisible={true}
        onClose={() => {}}
        variant="success"
      />,
    );
    expect(screen.getByText("✅")).toBeInTheDocument();
    expect(screen.getByText("Success").parentElement).toHaveClass(
      "bg-green-500",
    );
  });

  it("should render error variant correctly", () => {
    render(
      <Toast
        message="Error"
        isVisible={true}
        onClose={() => {}}
        variant="error"
      />,
    );
    expect(screen.getByText("❌")).toBeInTheDocument();
    expect(screen.getByText("Error").parentElement).toHaveClass("bg-red-500");
  });

  it("should render info variant correctly", () => {
    render(
      <Toast
        message="Info"
        isVisible={true}
        onClose={() => {}}
        variant="info"
      />,
    );
    expect(screen.getByText("ℹ️")).toBeInTheDocument();
    expect(screen.getByText("Info").parentElement).toHaveClass("bg-blue-500");
  });
});
