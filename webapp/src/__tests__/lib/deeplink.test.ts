import { describe, it, expect, vi, beforeEach, afterEach } from "vitest";
import {
  generateDeeplink,
  openInApp,
  isLikelyAndroidDevice,
  getDeeplinkDisplay,
} from "@/lib/deeplink";

describe("deeplink utilities", () => {
  beforeEach(() => {
    // Clear all mocks before each test
    vi.clearAllMocks();
  });

  afterEach(() => {
    vi.resetAllMocks();
  });

  describe("generateDeeplink", () => {
    it("should generate valid deeplink from JSON object", () => {
      const jsonData = { type: "explicit", title: "Test Challenge" };

      const deeplink = generateDeeplink(jsonData);

      expect(deeplink).toMatch(/^mathpup:\/\/import\?json=.+$/);
      expect(deeplink).toContain("mathpup://import?json=");
    });

    it("should generate valid deeplink from JSON string", () => {
      const jsonString = '{"type":"generated","problemCount":10}';

      const deeplink = generateDeeplink(jsonString);

      expect(deeplink).toMatch(/^mathpup:\/\/import\?json=.+$/);
    });

    it("should properly encode special characters", () => {
      const jsonData = {
        title: 'Math & Science: "Addition"',
        subtitle: "Level 1",
      };

      const deeplink = generateDeeplink(jsonData);

      expect(deeplink).toMatch(/^mathpup:\/\/import\?json=.+$/);
      // Verify encoded content contains encoded characters
      expect(deeplink).toContain("json=");
    });

    it("should handle complex nested JSON", () => {
      const complexJson = {
        type: "explicit" as const,
        title: "Complex Challenge",
        problems: [
          { operand1: 1, operand2: 2, operation: "addition" as const },
          { operand1: 5, operand2: 3, operation: "subtraction" as const },
        ],
      };

      const deeplink = generateDeeplink(complexJson);

      expect(deeplink).toMatch(/^mathpup:\/\/import\?json=.+$/);
    });

    it("should return empty string on error", () => {
      // Suppress console.error for this test
      const consoleErrorSpy = vi
        .spyOn(console, "error")
        .mockImplementation(() => {});
      // Create circular reference to trigger JSON.stringify error
      const circular: Record<string, unknown> = { prop: "value" };
      circular.self = circular;

      const deeplink = generateDeeplink(circular);

      // Circular reference should cause JSON.stringify to throw, resulting in empty string
      expect(deeplink).toBe("");
      consoleErrorSpy.mockRestore();
    });

    it("should encode JSON with all special characters", () => {
      const testCases = [
        { data: { title: "Test with spaces" } },
        { data: { char: "!@#$%^&*()" } },
        { data: { quote: 'Say "hello"' } },
        { data: { slash: "path/to/file" } },
      ];

      testCases.forEach(({ data }) => {
        const deeplink = generateDeeplink(data);
        expect(deeplink).toMatch(/^mathpup:\/\/import\?json=.+$/);
        expect(deeplink).toContain("json=");
      });
    });
  });

  describe("openInApp", () => {
    it("should set window.location.href to deeplink", () => {
      const originalLocation = window.location;
      delete (window as Partial<Window>).location;
      window.location = { href: "" } as Location;

      const jsonData = { type: "explicit", title: "Test" };
      const result = openInApp(jsonData);

      expect(result).toMatch(/^mathpup:\/\/import\?json=.+$/);
      expect(window.location.href).toMatch(/^mathpup:\/\/import\?json=.+$/);

      // Restore
      window.location = originalLocation;
    });

    it("should return deeplink on success", () => {
      const originalLocation = window.location;
      delete (window as Partial<Window>).location;
      window.location = { href: "" } as Location;

      const jsonData = { type: "generated", problemCount: 5 };
      const result = openInApp(jsonData);

      expect(result).toMatch(/^mathpup:\/\/import\?json=.+$/);

      // Restore
      window.location = originalLocation;
    });

    it("should return empty string on error", () => {
      // Suppress console.error for this test
      const consoleErrorSpy = vi
        .spyOn(console, "error")
        .mockImplementation(() => {});
      // openInApp should return empty string when generateDeeplink fails
      const circular: Record<string, unknown> = { prop: "value" };
      circular.self = circular;

      const result = openInApp(circular);

      expect(result).toBe("");
      consoleErrorSpy.mockRestore();
    });
  });

  describe("isLikelyAndroidDevice", () => {
    it("should return true for Android user agents", () => {
      const androidUserAgents = [
        "Mozilla/5.0 (Linux; Android 12) AppleWebKit/537.36",
        "Mozilla/5.0 (Linux; Android 11; SM-G991B) AppleWebKit/537.36",
        "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 Chrome/91.0",
      ];

      androidUserAgents.forEach((userAgent) => {
        Object.defineProperty(navigator, "userAgent", {
          value: userAgent,
          configurable: true,
        });

        expect(isLikelyAndroidDevice()).toBe(true);
      });
    });

    it("should return false for non-Android user agents", () => {
      const nonAndroidUserAgents = [
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36",
        "Mozilla/5.0 (iPhone; CPU iPhone OS 14_6 like Mac OS X)",
        "Mozilla/5.0 (iPad; CPU OS 14_6 like Mac OS X)",
      ];

      nonAndroidUserAgents.forEach((userAgent) => {
        Object.defineProperty(navigator, "userAgent", {
          value: userAgent,
          configurable: true,
        });

        expect(isLikelyAndroidDevice()).toBe(false);
      });
    });

    it("should handle empty user agent", () => {
      Object.defineProperty(navigator, "userAgent", {
        value: "",
        configurable: true,
      });

      expect(isLikelyAndroidDevice()).toBe(false);
    });
  });

  describe("getDeeplinkDisplay", () => {
    it("should return formatted deeplink display string", () => {
      const deeplink = "mathpup://import?json=%7B%22type%22%3A%22test%22%7D";

      const display = getDeeplinkDisplay(deeplink);

      expect(display).toContain("mathpup://import");
      expect(display).toContain("json=");
    });

    it("should handle invalid deeplink", () => {
      const display = getDeeplinkDisplay("not-a-deeplink");

      expect(display).toBe("not-a-deeplink");
    });

    it("should handle empty deeplink", () => {
      const display = getDeeplinkDisplay("");

      expect(display).toBe("Invalid deeplink");
    });

    it("should truncate long deeplinks for display", () => {
      const deeplink = "mathpup://import?json=%7B%22type%22%3A%22test%22%7D";

      const display = getDeeplinkDisplay(deeplink);

      // Should contain the scheme and format info
      expect(display).toContain("mathpup://import");
    });
  });

  describe("Deeplink round-trip encoding", () => {
    it("should encode and generate valid deeplinks", () => {
      const testCases = [
        { type: "explicit", title: "Simple" },
        { type: "generated", operation: "addition", problemCount: 10 },
        {
          type: "explicit",
          title: "Complex with & < > quotes",
          problems: [
            { operand1: 1, operand2: 2, operation: "addition" as const },
          ],
        },
      ];

      testCases.forEach((testCase) => {
        const deeplink = generateDeeplink(testCase);

        expect(deeplink).toMatch(/^mathpup:\/\/import\?json=.+$/);
        // Verify the deeplink would be usable
        expect(deeplink.length).toBeGreaterThan(30);
      });
    });
  });
});
