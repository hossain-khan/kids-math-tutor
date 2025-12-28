import { vi } from "vitest";
import "@testing-library/jest-dom";

// Mock navigator.clipboard
Object.assign(navigator, {
  clipboard: {
    writeText: vi.fn(() => Promise.resolve()),
  },
});

// Mock URL.createObjectURL and revokeObjectURL
if (typeof global !== "undefined") {
  global.URL.createObjectURL = vi.fn(() => "mock-url");
  global.URL.revokeObjectURL = vi.fn();
}

// Mock sessionStorage
const sessionStorageMock = (() => {
  let store: Record<string, string> = {};

  return {
    getItem: (key: string) => store[key] || null,
    setItem: (key: string, value: string) => {
      store[key] = value.toString();
    },
    removeItem: (key: string) => {
      delete store[key];
    },
    clear: () => {
      store = {};
    },
  };
})();

Object.defineProperty(window, "sessionStorage", {
  value: sessionStorageMock,
});

// Mock window.location
Object.defineProperty(window, "location", {
  value: {
    href: "",
  },
  writable: true,
});

// Mock alert
window.alert = vi.fn();
