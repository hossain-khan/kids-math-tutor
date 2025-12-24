import { describe, it, expect, beforeEach, vi } from "vitest";
import { render, screen, waitFor } from "@testing-library/react";
import { BrowserRouter } from "react-router-dom";
import userEvent from "@testing-library/user-event";
import Result from "@/pages/Result";
import * as utils from "@/lib/utils";

// Mock react-router-dom navigate
const mockNavigate = vi.fn();
vi.mock("react-router-dom", async () => {
  const actual = await vi.importActual("react-router-dom");
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

describe("Result Component", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    sessionStorage.clear();
  });

  const generatedChallengeData = {
    type: "generated" as const,
    title: "Addition Practice",
    subtitle: "Numbers 1-20",
    operation: "addition" as const,
    problemCount: 10,
    numberRange: { min: 1, max: 20 },
  };

  it("should redirect to home if no challenge data exists", async () => {
    render(
      <BrowserRouter>
        <Result />
      </BrowserRouter>,
    );

    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalledWith("/");
    });
  });

  it("should display challenge result page", async () => {
    sessionStorage.setItem(
      "challengeData",
      JSON.stringify(generatedChallengeData),
    );

    render(
      <BrowserRouter>
        <Result />
      </BrowserRouter>,
    );

    await waitFor(() => {
      expect(screen.getByText(/Worksheet Ready!/i)).toBeInTheDocument();
    });
  });

  it("should copy JSON to clipboard", async () => {
    const user = userEvent.setup();
    const copyToClipboardSpy = vi
      .spyOn(utils, "copyToClipboard")
      .mockResolvedValue(true);

    sessionStorage.setItem(
      "challengeData",
      JSON.stringify(generatedChallengeData),
    );

    render(
      <BrowserRouter>
        <Result />
      </BrowserRouter>,
    );

    await waitFor(() => {
      expect(screen.getByText(/Worksheet Ready!/i)).toBeInTheDocument();
    });

    const copyButtons = screen.getAllByRole("button", { name: /copy/i });
    await user.click(copyButtons[0]);

    expect(copyToClipboardSpy).toHaveBeenCalled();
  });

  it("should download JSON file", async () => {
    const user = userEvent.setup();
    const downloadJsonSpy = vi
      .spyOn(utils, "downloadJson")
      .mockImplementation(() => {});

    sessionStorage.setItem(
      "challengeData",
      JSON.stringify(generatedChallengeData),
    );

    render(
      <BrowserRouter>
        <Result />
      </BrowserRouter>,
    );

    await waitFor(() => {
      expect(screen.getByText(/Worksheet Ready!/i)).toBeInTheDocument();
    });

    const downloadButton = screen.getByRole("button", { name: /download/i });
    await user.click(downloadButton);

    expect(downloadJsonSpy).toHaveBeenCalled();
  });

  it("should navigate to home when create another is clicked", async () => {
    const user = userEvent.setup();
    sessionStorage.setItem(
      "challengeData",
      JSON.stringify(generatedChallengeData),
    );

    render(
      <BrowserRouter>
        <Result />
      </BrowserRouter>,
    );

    await waitFor(() => {
      expect(screen.getByText(/Worksheet Ready!/i)).toBeInTheDocument();
    });

    const createAnotherButton = screen.getByRole("button", {
      name: /create another/i,
    });
    await user.click(createAnotherButton);

    expect(sessionStorage.getItem("challengeData")).toBeNull();
    expect(mockNavigate).toHaveBeenCalledWith("/");
  });

  it("should redirect to home if challenge data is invalid JSON", async () => {
    sessionStorage.setItem("challengeData", "invalid json");
    const consoleErrorSpy = vi
      .spyOn(console, "error")
      .mockImplementation(() => {});

    render(
      <BrowserRouter>
        <Result />
      </BrowserRouter>,
    );

    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalledWith("/");
      expect(consoleErrorSpy).toHaveBeenCalled();
    });

    consoleErrorSpy.mockRestore();
  });
});
