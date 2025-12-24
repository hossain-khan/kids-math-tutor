import { describe, it, expect, beforeEach, vi } from "vitest";
import { render, screen, waitFor } from "@testing-library/react";
import { BrowserRouter } from "react-router-dom";
import userEvent from "@testing-library/user-event";
import ExplicitBuilder from "@/pages/ExplicitBuilder";

// Mock react-router-dom navigate
const mockNavigate = vi.fn();
vi.mock("react-router-dom", async () => {
  const actual = await vi.importActual("react-router-dom");
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

describe("ExplicitBuilder Component", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    sessionStorage.clear();
  });

  it("should render the component", () => {
    render(
      <BrowserRouter>
        <ExplicitBuilder />
      </BrowserRouter>,
    );

    expect(screen.getByText(/Custom Problems/i)).toBeInTheDocument();
  });

  it("should navigate to result page on valid submission", async () => {
    const user = userEvent.setup();

    render(
      <BrowserRouter>
        <ExplicitBuilder />
      </BrowserRouter>,
    );

    const titleInput = screen.getByPlaceholderText(/tricky division/i);
    await user.type(titleInput, "Custom Math");

    const submitButton = screen.getByRole("button", {
      name: /create worksheet/i,
    });
    await user.click(submitButton);

    await waitFor(
      () => {
        expect(mockNavigate).toHaveBeenCalledWith("/result");
      },
      { timeout: 3000 },
    );
  });

  it("should store valid challenge data in sessionStorage", async () => {
    const user = userEvent.setup();

    render(
      <BrowserRouter>
        <ExplicitBuilder />
      </BrowserRouter>,
    );

    const titleInput = screen.getByPlaceholderText(/tricky division/i);
    await user.type(titleInput, "Test Problems");

    const submitButton = screen.getByRole("button", {
      name: /create worksheet/i,
    });
    await user.click(submitButton);

    await waitFor(
      () => {
        const data = sessionStorage.getItem("challengeData");
        expect(data).not.toBeNull();
        if (data) {
          const parsed = JSON.parse(data);
          expect(parsed.type).toBe("explicit");
          expect(parsed.title).toBe("Test Problems");
        }
      },
      { timeout: 3000 },
    );
  });
});
