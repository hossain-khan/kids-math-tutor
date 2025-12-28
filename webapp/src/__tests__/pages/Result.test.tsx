import { describe, it, expect, beforeEach, vi } from "vitest";
import { render, screen, waitFor } from "@testing-library/react";
import { BrowserRouter } from "react-router-dom";
import userEvent from "@testing-library/user-event";
import Result from "@/pages/Result";
import * as utils from "@/lib/utils";
import * as deeplink from "@/lib/deeplink";

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

  describe("Deeplink functionality", () => {
    it("should detect Android device", async () => {
      sessionStorage.setItem(
        "challengeData",
        JSON.stringify(generatedChallengeData),
      );
      vi.spyOn(deeplink, "isLikelyAndroidDevice").mockReturnValue(true);

      render(
        <BrowserRouter>
          <Result />
        </BrowserRouter>,
      );

      await waitFor(() => {
        expect(screen.getByText(/Open in Math Pup App/i)).toBeInTheDocument();
      });
    });

    it("should show 'Open in App' button only on Android", async () => {
      sessionStorage.setItem(
        "challengeData",
        JSON.stringify(generatedChallengeData),
      );
      vi.spyOn(deeplink, "isLikelyAndroidDevice").mockReturnValue(false);

      render(
        <BrowserRouter>
          <Result />
        </BrowserRouter>,
      );

      await waitFor(() => {
        expect(
          screen.queryByText(/Open in Math Pup App/i),
        ).not.toBeInTheDocument();
      });
    });

    it("should generate deeplink and set window.location.href on Android", async () => {
      const user = userEvent.setup();
      const originalLocation = window.location;
      delete (window as Partial<Window>).location;
      window.location = { href: "" } as Location;

      const generatedDeeplink =
        "mathpup://import?json=%7B%22type%22%3A%22generated%22%7D";
      vi.spyOn(deeplink, "isLikelyAndroidDevice").mockReturnValue(true);
      vi.spyOn(deeplink, "generateDeeplink").mockReturnValue(generatedDeeplink);

      sessionStorage.setItem(
        "challengeData",
        JSON.stringify(generatedChallengeData),
      );

      render(
        <BrowserRouter>
          <Result />
        </BrowserRouter>,
      );

      const openInAppButton = await screen.findByText(/Open in Math Pup App/i);
      await user.click(openInAppButton);

      expect(deeplink.generateDeeplink).toHaveBeenCalledWith(
        generatedChallengeData,
      );
      expect(window.location.href).toBe(generatedDeeplink);

      // Restore
      window.location = originalLocation;
    });

    it("should show loading state when opening app", async () => {
      const user = userEvent.setup();
      vi.spyOn(deeplink, "isLikelyAndroidDevice").mockReturnValue(true);
      vi.spyOn(deeplink, "generateDeeplink").mockReturnValue(
        "mathpup://import?json=test",
      );

      sessionStorage.setItem(
        "challengeData",
        JSON.stringify(generatedChallengeData),
      );

      render(
        <BrowserRouter>
          <Result />
        </BrowserRouter>,
      );

      const openInAppButton = await screen.findByText(/Open in Math Pup App/i);
      await user.click(openInAppButton);

      // Should show loading text temporarily
      await waitFor(() => {
        expect(screen.queryByText(/Opening Math Pup/i)).toBeInTheDocument();
      });
    });
  });

  describe("Share error feedback", () => {
    it("should display error message when share fails", async () => {
      const user = userEvent.setup();
      const explicitChallengeData = {
        type: "explicit" as const,
        title: "Test Challenge",
        subtitle: "Test",
        problems: [
          { operand1: 5, operand2: 3, operation: "addition" as const },
        ],
      };

      sessionStorage.setItem(
        "challengeData",
        JSON.stringify(explicitChallengeData),
      );

      global.fetch = vi.fn().mockResolvedValue({
        ok: false,
        json: async () => ({
          error:
            "Worksheet contains inappropriate content for children. Please review and try again.",
          categories: ["negative sentiment"],
          explanation: "Content contains discouraging language",
          suggestion: "Please use positive and encouraging language.",
          method: "AI-based safety check",
        }),
      });

      render(
        <BrowserRouter>
          <Result />
        </BrowserRouter>,
      );

      await waitFor(() => {
        expect(screen.getByText(/Worksheet Ready!/i)).toBeInTheDocument();
      });

      const shareButton = screen.getByRole("button", {
        name: /share to community/i,
      });
      await user.click(shareButton);

      await waitFor(() => {
        expect(
          screen.getByText(
            /Worksheet contains inappropriate content for children/i,
          ),
        ).toBeInTheDocument();
      });
    });

    it("should display violation categories", async () => {
      const user = userEvent.setup();
      const explicitChallengeData = {
        type: "explicit" as const,
        title: "Test Challenge",
        subtitle: "Test",
        problems: [
          { operand1: 5, operand2: 3, operation: "addition" as const },
        ],
      };

      sessionStorage.setItem(
        "challengeData",
        JSON.stringify(explicitChallengeData),
      );

      global.fetch = vi.fn().mockResolvedValue({
        ok: false,
        json: async () => ({
          error: "Worksheet contains inappropriate content",
          categories: ["negative sentiment", "bullying", "dismissive language"],
          explanation: "Content contains discouraging language",
          suggestion: "Please use positive and encouraging language.",
          method: "AI-based safety check",
        }),
      });

      render(
        <BrowserRouter>
          <Result />
        </BrowserRouter>,
      );

      await waitFor(() => {
        expect(screen.getByText(/Worksheet Ready!/i)).toBeInTheDocument();
      });

      const shareButton = screen.getByRole("button", {
        name: /share to community/i,
      });
      await user.click(shareButton);

      await waitFor(() => {
        expect(screen.getByText(/negative sentiment/i)).toBeInTheDocument();
        expect(screen.getByText(/bullying/i)).toBeInTheDocument();
        expect(screen.getByText(/dismissive language/i)).toBeInTheDocument();
      });
    });

    it("should display explanation text", async () => {
      const user = userEvent.setup();
      const explicitChallengeData = {
        type: "explicit" as const,
        title: "Test Challenge",
        subtitle: "Test",
        problems: [
          { operand1: 5, operand2: 3, operation: "addition" as const },
        ],
      };

      sessionStorage.setItem(
        "challengeData",
        JSON.stringify(explicitChallengeData),
      );

      const explanation =
        "The subtitle contains a discouraging phrase 'just give up' which is dismissive and negative";

      global.fetch = vi.fn().mockResolvedValue({
        ok: false,
        json: async () => ({
          error: "Worksheet contains inappropriate content",
          categories: ["dismissive language"],
          explanation,
          suggestion: "Please use positive and encouraging language.",
          method: "AI-based safety check",
        }),
      });

      render(
        <BrowserRouter>
          <Result />
        </BrowserRouter>,
      );

      await waitFor(() => {
        expect(screen.getByText(/Worksheet Ready!/i)).toBeInTheDocument();
      });

      const shareButton = screen.getByRole("button", {
        name: /share to community/i,
      });
      await user.click(shareButton);

      await waitFor(() => {
        expect(screen.getByText(/Why this happened/i)).toBeInTheDocument();
        expect(screen.getByText(explanation)).toBeInTheDocument();
      });
    });

    it("should display suggestion text", async () => {
      const user = userEvent.setup();
      const explicitChallengeData = {
        type: "explicit" as const,
        title: "Test Challenge",
        subtitle: "Test",
        problems: [
          { operand1: 5, operand2: 3, operation: "addition" as const },
        ],
      };

      sessionStorage.setItem(
        "challengeData",
        JSON.stringify(explicitChallengeData),
      );

      const suggestion =
        "Please ensure all content is age-appropriate for K-2 children.";

      global.fetch = vi.fn().mockResolvedValue({
        ok: false,
        json: async () => ({
          error: "Worksheet contains inappropriate content",
          categories: ["dismissive language"],
          explanation: "Content contains discouraging language",
          suggestion,
          method: "AI-based safety check",
        }),
      });

      render(
        <BrowserRouter>
          <Result />
        </BrowserRouter>,
      );

      await waitFor(() => {
        expect(screen.getByText(/Worksheet Ready!/i)).toBeInTheDocument();
      });

      const shareButton = screen.getByRole("button", {
        name: /share to community/i,
      });
      await user.click(shareButton);

      await waitFor(() => {
        expect(screen.getByText(/How to fix/i)).toBeInTheDocument();
        expect(screen.getByText(suggestion)).toBeInTheDocument();
      });
    });

    it("should display validation method", async () => {
      const user = userEvent.setup();
      const explicitChallengeData = {
        type: "explicit" as const,
        title: "Test Challenge",
        subtitle: "Test",
        problems: [
          { operand1: 5, operand2: 3, operation: "addition" as const },
        ],
      };

      sessionStorage.setItem(
        "challengeData",
        JSON.stringify(explicitChallengeData),
      );

      global.fetch = vi.fn().mockResolvedValue({
        ok: false,
        json: async () => ({
          error: "Worksheet contains inappropriate content",
          categories: ["dismissive language"],
          explanation: "Content contains discouraging language",
          suggestion: "Please use positive language.",
          method: "AI-based safety check",
        }),
      });

      render(
        <BrowserRouter>
          <Result />
        </BrowserRouter>,
      );

      await waitFor(() => {
        expect(screen.getByText(/Worksheet Ready!/i)).toBeInTheDocument();
      });

      const shareButton = screen.getByRole("button", {
        name: /share to community/i,
      });
      await user.click(shareButton);

      await waitFor(() => {
        expect(screen.getByText(/Validation method/i)).toBeInTheDocument();
        expect(screen.getByText(/AI-based safety check/i)).toBeInTheDocument();
      });
    });

    it("should handle error response without optional fields", async () => {
      const user = userEvent.setup();
      const explicitChallengeData = {
        type: "explicit" as const,
        title: "Test Challenge",
        subtitle: "Test",
        problems: [
          { operand1: 5, operand2: 3, operation: "addition" as const },
        ],
      };

      sessionStorage.setItem(
        "challengeData",
        JSON.stringify(explicitChallengeData),
      );

      global.fetch = vi.fn().mockResolvedValue({
        ok: false,
        json: async () => ({
          error: "Worksheet contains inappropriate content",
        }),
      });

      render(
        <BrowserRouter>
          <Result />
        </BrowserRouter>,
      );

      await waitFor(() => {
        expect(screen.getByText(/Worksheet Ready!/i)).toBeInTheDocument();
      });

      const shareButton = screen.getByRole("button", {
        name: /share to community/i,
      });
      await user.click(shareButton);

      await waitFor(() => {
        expect(
          screen.getByText(/Worksheet contains inappropriate content/i),
        ).toBeInTheDocument();
      });
    });
  });
});
