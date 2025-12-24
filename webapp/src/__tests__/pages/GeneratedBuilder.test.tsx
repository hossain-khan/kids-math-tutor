import { describe, it, expect, beforeEach, vi } from "vitest";
import { render, screen, waitFor } from "@testing-library/react";
import { BrowserRouter } from "react-router-dom";
import userEvent from "@testing-library/user-event";
import GeneratedBuilder from "@/pages/GeneratedBuilder";

// Mock react-router-dom navigate
const mockNavigate = vi.fn();
vi.mock("react-router-dom", async () => {
  const actual = await vi.importActual("react-router-dom");
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

describe("GeneratedBuilder Component", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    sessionStorage.clear();
  });

  it("should render the component", () => {
    render(
      <BrowserRouter>
        <GeneratedBuilder />
      </BrowserRouter>,
    );

    expect(screen.getByText(/Quick Generator/i)).toBeInTheDocument();
  });

  it("should accept valid form and navigate to result", async () => {
    const user = userEvent.setup();

    render(
      <BrowserRouter>
        <GeneratedBuilder />
      </BrowserRouter>,
    );

    const titleInput = screen.getByPlaceholderText(/addition practice/i);
    await user.type(titleInput, "Math Worksheet");

    const submitButton = screen.getByRole("button", {
      name: /generate worksheet/i,
    });
    await user.click(submitButton);

    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalledWith("/result");
    });

    const storedData = sessionStorage.getItem("challengeData");
    expect(storedData).toBeTruthy();

    const parsed = JSON.parse(storedData!);
    expect(parsed.type).toBe("generated");
    expect(parsed.title).toBe("Math Worksheet");
    expect(parsed.operation).toBe("addition");
    expect(parsed.problemCount).toBe(10);
  });

  it("should store challenge data in sessionStorage", async () => {
    const user = userEvent.setup();

    render(
      <BrowserRouter>
        <GeneratedBuilder />
      </BrowserRouter>,
    );

    const titleInput = screen.getByPlaceholderText(/addition practice/i);
    await user.type(titleInput, "Test Challenge");

    const submitButton = screen.getByRole("button", {
      name: /generate worksheet/i,
    });
    await user.click(submitButton);

    await waitFor(() => {
      const data = sessionStorage.getItem("challengeData");
      expect(data).not.toBeNull();
      const parsed = JSON.parse(data!);
      expect(parsed.type).toBe("generated");
      expect(parsed.title).toBe("Test Challenge");
    });
  });
});
