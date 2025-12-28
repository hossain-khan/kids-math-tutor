import { describe, it, expect, vi, beforeEach, afterEach } from "vitest";
import {
  checkContentSafetyWithAI,
  fallbackToBadWords,
} from "@/lib/server/aiSafety";

describe("aiSafety module", () => {
  beforeEach(() => {
    // Clear all mocks before each test
    vi.clearAllMocks();
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  describe("checkContentSafetyWithAI", () => {
    it("should fall back to bad-words when AI binding is not available", async () => {
      const env = {}; // No AI binding

      const result = await checkContentSafetyWithAI(env, {
        title: "Math Practice",
        subtitle: "Addition problems",
        description: "Learn basic addition",
      });

      expect(result.safe).toBe(true);
      expect(result.fallback).toBe(true);
      expect(result.usingAI).toBe(false);
      expect(result.confidence).toBe(0.75); // Fallback confidence
    });

    it("should detect profanity in title using fallback", async () => {
      const env = {}; // No AI binding

      const result = await checkContentSafetyWithAI(env, {
        title: "Math Practice with damn problems",
        subtitle: "Addition",
        description: "Learn math",
      });

      expect(result.safe).toBe(false);
      expect(result.fallback).toBe(true);
      expect(result.categories).toContain("profanity");
    });

    it("should detect profanity in subtitle using fallback", async () => {
      const env = {}; // No AI binding

      const result = await checkContentSafetyWithAI(env, {
        title: "Math Practice",
        subtitle: "Hell yeah problems",
        description: "Learn math",
      });

      expect(result.safe).toBe(false);
      expect(result.fallback).toBe(true);
    });

    it("should detect profanity in description using fallback", async () => {
      const env = {}; // No AI binding

      const result = await checkContentSafetyWithAI(env, {
        title: "Math Practice",
        subtitle: "Addition",
        description: "Learn to solve math with shit content",
      });

      expect(result.safe).toBe(false);
      expect(result.fallback).toBe(true);
    });

    it("should handle missing AI binding gracefully", async () => {
      const env = { AI: undefined };

      const result = await checkContentSafetyWithAI(env, {
        title: "Math Practice",
      });

      expect(result).toHaveProperty("safe");
      expect(result).toHaveProperty("fallback", true);
    });

    it("should handle AI error and fall back to bad-words", async () => {
      const mockAIError = new Error("AI service unavailable");
      const env = {
        AI: {
          run: vi.fn().mockRejectedValue(mockAIError),
        },
      };

      const result = await checkContentSafetyWithAI(env, {
        title: "Math Practice",
      });

      expect(result.fallback).toBe(true);
      expect(result.usingAI).toBe(false);
    });

    it("should handle AI timeout and fall back to bad-words", async () => {
      const timeoutError = new Error("Timeout");
      timeoutError.name = "AbortError";

      const env = {
        AI: {
          run: vi.fn().mockRejectedValue(timeoutError),
        },
      };

      const result = await checkContentSafetyWithAI(env, {
        title: "Math Practice",
      });

      expect(result.fallback).toBe(true);
      expect(result.usingAI).toBe(false);
    });

    it("should handle rate limit (429) and fall back to bad-words", async () => {
      const rateLimitError = new Error("Rate limit exceeded");
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      (rateLimitError as any).status = 429;

      const env = {
        AI: {
          run: vi.fn().mockRejectedValue(rateLimitError),
        },
      };

      const result = await checkContentSafetyWithAI(env, {
        title: "Math Practice",
      });

      expect(result.fallback).toBe(true);
      expect(result.usingAI).toBe(false);
    });

    it("should use AI when binding is available and working", async () => {
      const mockAIResponse = `{
        "classification": "safe",
        "categories": [],
        "explanation": ""
      }`;

      const env = {
        AI: {
          run: vi.fn().mockResolvedValue(mockAIResponse),
        },
      };

      const result = await checkContentSafetyWithAI(env, {
        title: "Math Practice",
        subtitle: "Addition problems",
      });

      expect(result.usingAI).toBe(true);
      expect(result.fallback).toBe(false);
      expect(result.confidence).toBe(0.95); // AI confidence
      expect(env.AI.run).toHaveBeenCalledWith(
        "@cf/meta/llama-guard-3-8b",
        expect.any(Object),
      );
    });

    it("should handle AI detecting unsafe content", async () => {
      const mockAIResponse = `{
        "classification": "unsafe",
        "categories": ["violence", "profanity"],
        "explanation": "Content contains references to violence"
      }`;

      const env = {
        AI: {
          run: vi.fn().mockResolvedValue(mockAIResponse),
        },
      };

      const result = await checkContentSafetyWithAI(env, {
        title: "Fight Club Math",
        subtitle: "Violent addition",
      });

      expect(result.safe).toBe(false);
      expect(result.usingAI).toBe(true);
      expect(result.categories).toContain("violence");
      expect(result.explanation).toBe(
        "Content contains references to violence",
      );
    });

    it("should include all content in AI prompt", async () => {
      const env = {
        AI: {
          run: vi.fn().mockResolvedValue(`{
              "classification": "safe",
              "categories": [],
              "explanation": ""
            }`),
        },
      };

      await checkContentSafetyWithAI(env, {
        title: "Test Title",
        subtitle: "Test Subtitle",
        description: "Test Description",
      });

      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      const callArgs = (env.AI.run as any).mock.calls[0];
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      const messages = (callArgs[1] as any).messages;

      expect(messages).toBeDefined();
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      expect((messages[0] as any).content).toContain("Test Title");
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      expect((messages[0] as any).content).toContain("Test Subtitle");
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      expect((messages[0] as any).content).toContain("Test Description");
    });

    it("should handle plain text 'safe' response", async () => {
      const env = {
        AI: {
          run: vi.fn().mockResolvedValue("\n\nsafe"), // Plain text safe response
        },
      };

      const result = await checkContentSafetyWithAI(env, {
        title: "Math Practice",
      });

      expect(result.safe).toBe(true);
      expect(result.usingAI).toBe(true);
      expect(result.fallback).toBe(false);
      expect(result.classification).toBe("safe");
      expect(result.confidence).toBe(0.95);
    });

    it("should handle plain text 'unsafe' response", async () => {
      const env = {
        AI: {
          run: vi.fn().mockResolvedValue("\n\nunsafe"), // Plain text unsafe response
        },
      };

      const result = await checkContentSafetyWithAI(env, {
        title: "Inappropriate content",
      });

      expect(result.safe).toBe(false);
      expect(result.usingAI).toBe(true);
      expect(result.fallback).toBe(false);
      expect(result.classification).toBe("unsafe");
      expect(result.confidence).toBe(0.95);
    });

    it("should handle AI response object with plain text response field", async () => {
      const env = {
        AI: {
          run: vi.fn().mockResolvedValue({
            response: "\n\nsafe",
            usage: {
              prompt_tokens: 100,
              completion_tokens: 5,
              total_tokens: 105,
            },
          }), // Response object with plain text
        },
      };

      const result = await checkContentSafetyWithAI(env, {
        title: "Math Practice",
      });

      expect(result.safe).toBe(true);
      expect(result.usingAI).toBe(true);
      expect(result.fallback).toBe(false);
      expect(result.classification).toBe("safe");
    });

    it("should handle JSON response from AI", async () => {
      const env = {
        AI: {
          run: vi.fn().mockResolvedValue(
            JSON.stringify({
              classification: "safe",
              categories: [],
              explanation: "Content appears to be educational",
            }),
          ),
        },
      };

      const result = await checkContentSafetyWithAI(env, {
        title: "Math Practice",
      });

      expect(result.safe).toBe(true);
      expect(result.usingAI).toBe(true);
      expect(result.classification).toBe("safe");
      expect(result.explanation).toBe("Content appears to be educational");
    });

    it("should handle mixed case plain text responses", async () => {
      const env = {
        AI: {
          run: vi.fn().mockResolvedValue("  SAFE  "), // Mixed case with whitespace
        },
      };

      const result = await checkContentSafetyWithAI(env, {
        title: "Math Practice",
      });

      expect(result.safe).toBe(true);
      expect(result.classification).toBe("safe");
      expect(result.usingAI).toBe(true);
    });

    it("should handle response with result field instead of response", async () => {
      const env = {
        AI: {
          run: vi.fn().mockResolvedValue({
            result: "\n\nunsafe",
            tokens: { prompt: 100, completion: 5 },
          }),
        },
      };

      const result = await checkContentSafetyWithAI(env, {
        title: "Inappropriate content",
      });

      expect(result.safe).toBe(false);
      expect(result.usingAI).toBe(true);
      expect(result.classification).toBe("unsafe");
    });
  });

  describe("fallbackToBadWords", () => {
    it("should return safe result for clean text", () => {
      const result = fallbackToBadWords("Math Practice with Addition Problems");

      expect(result.safe).toBe(true);
      expect(result.usingAI).toBe(false);
      expect(result.fallback).toBe(true);
      expect(result.confidence).toBe(0.75);
    });

    it("should detect profanity in text", () => {
      const result = fallbackToBadWords("This damn math problem is hell");

      expect(result.safe).toBe(false);
      expect(result.categories).toContain("profanity");
    });

    it("should handle mild profanity", () => {
      const result = fallbackToBadWords("This ass problem");

      expect(result.safe).toBe(false);
      expect(result.categories).toContain("profanity");
    });

    it("should be case-insensitive", () => {
      const result = fallbackToBadWords("DAMN AND HELL");

      expect(result.safe).toBe(false);
      expect(result.categories).toContain("profanity");
    });

    it("should return confidence of 0.75 for fallback", () => {
      const result = fallbackToBadWords("Test content");

      expect(result.confidence).toBe(0.75);
    });

    it("should handle empty text", () => {
      const result = fallbackToBadWords("");

      expect(result.safe).toBe(true);
      expect(result.fallback).toBe(true);
    });

    it("should handle text with only special characters", () => {
      const result = fallbackToBadWords('!@#$%^&*()_+-=[]{}|;:",.<>?');

      expect(result.safe).toBe(true);
    });
  });

  describe("Safety check result structure", () => {
    it("should always return required fields", async () => {
      const result = await checkContentSafetyWithAI(
        {},
        {
          title: "Test",
        },
      );

      expect(result).toHaveProperty("safe");
      expect(result).toHaveProperty("confidence");
      expect(result).toHaveProperty("usingAI");
      expect(result).toHaveProperty("fallback");
    });

    it("should return explanation only when content is unsafe", async () => {
      const env = {}; // Will use fallback

      // Safe content
      const safeResult = await checkContentSafetyWithAI(env, {
        title: "Math Practice",
      });
      expect(safeResult.explanation).toBe(
        "Content is appropriate for children",
      );

      // Unsafe content
      const unsafeResult = await checkContentSafetyWithAI(env, {
        title: "Math damn Practice",
      });
      expect(unsafeResult.explanation).toBeDefined();
    });

    it("should have correct confidence levels", async () => {
      const env = {}; // Fallback path

      const result = await checkContentSafetyWithAI(env, {
        title: "Test",
      });

      expect(result.confidence).toBe(0.75); // Fallback is 0.75
    });
  });

  describe("Performance and edge cases", () => {
    it("should handle very long content", async () => {
      const longText = "word ".repeat(1000);

      const result = await checkContentSafetyWithAI(
        {},
        {
          title: longText,
          subtitle: longText,
          description: longText,
        },
      );

      expect(result).toHaveProperty("safe");
      expect(result).toHaveProperty("fallback", true);
    });

    it("should handle special characters in content", async () => {
      const result = await checkContentSafetyWithAI(
        {},
        {
          title: "Math™ Practice™",
          subtitle: "©2024 Practice",
          description: "→ Learn Addition",
        },
      );

      expect(result.safe).toBe(true);
    });

    it("should handle unicode characters", async () => {
      const result = await checkContentSafetyWithAI(
        {},
        {
          title: "数学练习",
          subtitle: "Math Pratique",
          description: "Math Übung",
        },
      );

      expect(result).toHaveProperty("safe");
    });

    it("should handle null/undefined subtitle and description", async () => {
      const result = await checkContentSafetyWithAI(
        {},
        {
          title: "Test",
          subtitle: undefined,
          description: undefined,
        },
      );

      expect(result.safe).toBe(true);
    });
  });
});
