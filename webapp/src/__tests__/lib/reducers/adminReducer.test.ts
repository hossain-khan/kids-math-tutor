import { describe, it, expect } from "vitest";
import {
  adminReducer,
  initialAdminState,
  AdminState,
  AdminAction,
  AdminWorksheet,
} from "@/lib/reducers/adminReducer";

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

describe("adminReducer", () => {
  describe("Auth actions", () => {
    it("should handle SET_AUTH action", () => {
      const action: AdminAction = {
        type: "SET_AUTH",
        payload: { isAuthenticated: true, showAuthModal: false },
      };
      const result = adminReducer(initialAdminState, action);

      expect(result.auth.isAuthenticated).toBe(true);
      expect(result.auth.showAuthModal).toBe(false);
    });

    it("should handle SET_PASSWORD action", () => {
      const action: AdminAction = { type: "SET_PASSWORD", payload: "test123" };
      const result = adminReducer(initialAdminState, action);

      expect(result.auth.password).toBe("test123");
    });

    it("should handle SET_AUTH_ERROR action", () => {
      const action: AdminAction = {
        type: "SET_AUTH_ERROR",
        payload: "Invalid password",
      };
      const result = adminReducer(initialAdminState, action);

      expect(result.auth.error).toBe("Invalid password");
    });

    it("should handle CLEAR_AUTH_ERROR action", () => {
      const stateWithError = {
        ...initialAdminState,
        auth: { ...initialAdminState.auth, error: "Some error" },
      };
      const action: AdminAction = { type: "CLEAR_AUTH_ERROR" };
      const result = adminReducer(stateWithError, action);

      expect(result.auth.error).toBeNull();
    });
  });

  describe("Worksheet actions", () => {
    it("should handle FETCH_WORKSHEETS_START action", () => {
      const action: AdminAction = { type: "FETCH_WORKSHEETS_START" };
      const result = adminReducer(initialAdminState, action);

      expect(result.worksheets.loading).toBe(true);
      expect(result.worksheets.error).toBeNull();
    });

    it("should handle FETCH_WORKSHEETS_SUCCESS action with first batch", () => {
      const worksheets = [
        createMockWorksheet({ id: "ws-1" }),
        createMockWorksheet({ id: "ws-2" }),
      ];
      const action: AdminAction = {
        type: "FETCH_WORKSHEETS_SUCCESS",
        payload: { items: worksheets, total: 50 },
      };
      const result = adminReducer(initialAdminState, action);

      expect(result.worksheets.items).toEqual(worksheets);
      expect(result.worksheets.total).toBe(50);
      expect(result.worksheets.loading).toBe(false);
      expect(result.worksheets.hasMore).toBe(true); // 2 < 50
    });

    it("should handle FETCH_WORKSHEETS_SUCCESS with load more (append items)", () => {
      const initialWorksheets = [createMockWorksheet({ id: "ws-1" })];
      const stateWithWorksheets: AdminState = {
        ...initialAdminState,
        worksheets: {
          ...initialAdminState.worksheets,
          items: initialWorksheets,
          offset: 20,
          total: 50,
        },
      };

      const moreWorksheets = [createMockWorksheet({ id: "ws-2" })];
      const action: AdminAction = {
        type: "FETCH_WORKSHEETS_SUCCESS",
        payload: { items: moreWorksheets, total: 50 },
      };
      const result = adminReducer(stateWithWorksheets, action);

      expect(result.worksheets.items).toHaveLength(2);
      expect(result.worksheets.items[0].id).toBe("ws-1");
      expect(result.worksheets.items[1].id).toBe("ws-2");
    });

    it("should calculate hasMore correctly", () => {
      const worksheets = [createMockWorksheet({ id: "ws-1" })];
      const action: AdminAction = {
        type: "FETCH_WORKSHEETS_SUCCESS",
        payload: { items: worksheets, total: 1 },
      };
      const result = adminReducer(initialAdminState, action);

      expect(result.worksheets.hasMore).toBe(false); // 1 >= 1
    });

    it("should handle FETCH_WORKSHEETS_ERROR action", () => {
      const action: AdminAction = {
        type: "FETCH_WORKSHEETS_ERROR",
        payload: "Network error",
      };
      const result = adminReducer(initialAdminState, action);

      expect(result.worksheets.loading).toBe(false);
      expect(result.worksheets.error).toBe("Network error");
    });

    it("should handle SET_WORKSHEET_LIMIT action", () => {
      const action: AdminAction = { type: "SET_WORKSHEET_LIMIT", payload: 50 };
      const result = adminReducer(initialAdminState, action);

      expect(result.worksheets.limit).toBe(50);
    });

    it("should handle SET_WORKSHEET_OFFSET action", () => {
      const action: AdminAction = { type: "SET_WORKSHEET_OFFSET", payload: 20 };
      const result = adminReducer(initialAdminState, action);

      expect(result.worksheets.offset).toBe(20);
    });

    it("should handle LOAD_MORE_WORKSHEETS action", () => {
      const stateWithOffset: AdminState = {
        ...initialAdminState,
        worksheets: {
          ...initialAdminState.worksheets,
          offset: 0,
          limit: 20,
        },
      };
      const action: AdminAction = { type: "LOAD_MORE_WORKSHEETS" };
      const result = adminReducer(stateWithOffset, action);

      expect(result.worksheets.offset).toBe(20);
    });
  });

  describe("Delete actions", () => {
    it("should handle SET_DELETE_CONFIRM action", () => {
      const action: AdminAction = {
        type: "SET_DELETE_CONFIRM",
        payload: "ws-123",
      };
      const result = adminReducer(initialAdminState, action);

      expect(result.ui.deleteConfirm).toBe("ws-123");
    });

    it("should handle SET_DELETING action", () => {
      const action: AdminAction = { type: "SET_DELETING", payload: "ws-123" };
      const result = adminReducer(initialAdminState, action);

      expect(result.ui.deleting).toBe("ws-123");
    });

    it("should handle DELETE_WORKSHEET_SUCCESS action", () => {
      const worksheets = [
        createMockWorksheet({ id: "ws-1" }),
        createMockWorksheet({ id: "ws-2" }),
      ];
      const stateWithWorksheets: AdminState = {
        ...initialAdminState,
        worksheets: {
          ...initialAdminState.worksheets,
          items: worksheets,
          total: 2,
        },
        ui: {
          ...initialAdminState.ui,
          deleteConfirm: "ws-1",
          deleting: "ws-1",
        },
      };

      const action: AdminAction = {
        type: "DELETE_WORKSHEET_SUCCESS",
        payload: "ws-1",
      };
      const result = adminReducer(stateWithWorksheets, action);

      expect(result.worksheets.items).toHaveLength(1);
      expect(result.worksheets.items[0].id).toBe("ws-2");
      expect(result.worksheets.total).toBe(1);
      expect(result.ui.deleteConfirm).toBeNull();
      expect(result.ui.deleting).toBeNull();
    });

    it("should handle DELETE_WORKSHEET_ERROR action", () => {
      const action: AdminAction = {
        type: "DELETE_WORKSHEET_ERROR",
        payload: "Failed to delete",
      };
      const stateWithDeleting: AdminState = {
        ...initialAdminState,
        ui: { ...initialAdminState.ui, deleting: "ws-1" },
      };

      const result = adminReducer(stateWithDeleting, action);

      expect(result.worksheets.error).toBe("Failed to delete");
      expect(result.ui.deleting).toBeNull();
    });
  });

  describe("Safety check actions", () => {
    it("should handle START_SAFETY_CHECK action", () => {
      const action: AdminAction = {
        type: "START_SAFETY_CHECK",
        payload: 5,
      };
      const result = adminReducer(initialAdminState, action);

      expect(result.safetyCheck.checking).toBe(true);
      expect(result.safetyCheck.progress?.total).toBe(5);
      expect(result.safetyCheck.progress?.current).toBe(0);
    });

    it("should handle UPDATE_SAFETY_PROGRESS action", () => {
      const stateWithChecking: AdminState = {
        ...initialAdminState,
        safetyCheck: {
          checking: true,
          progress: { current: 0, total: 5 },
        },
      };
      const action: AdminAction = {
        type: "UPDATE_SAFETY_PROGRESS",
        payload: { current: 3, total: 5 },
      };
      const result = adminReducer(stateWithChecking, action);

      expect(result.safetyCheck.progress?.current).toBe(3);
      expect(result.safetyCheck.progress?.total).toBe(5);
    });

    it("should handle SAFETY_CHECK_SUCCESS action", () => {
      const worksheets = [
        createMockWorksheet({
          id: "ws-1",
          safety: { isFlagged: false },
        }),
        createMockWorksheet({
          id: "ws-2",
          safety: { isFlagged: true, categories: ["violence"] },
        }),
      ];
      const action: AdminAction = {
        type: "SAFETY_CHECK_SUCCESS",
        payload: worksheets,
      };
      const result = adminReducer(initialAdminState, action);

      expect(result.worksheets.items).toEqual(worksheets);
      expect(result.safetyCheck.checking).toBe(false);
      expect(result.safetyCheck.progress).toBeNull();
    });

    it("should handle SAFETY_CHECK_ERROR action", () => {
      const action: AdminAction = {
        type: "SAFETY_CHECK_ERROR",
        payload: "AI service unavailable",
      };
      const result = adminReducer(initialAdminState, action);

      expect(result.worksheets.error).toBe("AI service unavailable");
      expect(result.safetyCheck.checking).toBe(false);
      expect(result.safetyCheck.progress).toBeNull();
    });

    it("should handle STOP_SAFETY_CHECK action", () => {
      const stateWithChecking: AdminState = {
        ...initialAdminState,
        safetyCheck: {
          checking: true,
          progress: { current: 2, total: 5 },
        },
      };
      const action: AdminAction = { type: "STOP_SAFETY_CHECK" };
      const result = adminReducer(stateWithChecking, action);

      expect(result.safetyCheck.checking).toBe(false);
      expect(result.safetyCheck.progress).toBeNull();
    });
  });

  describe("UI actions", () => {
    it("should handle TOGGLE_EXPANDED_SAFETY action", () => {
      const action: AdminAction = {
        type: "TOGGLE_EXPANDED_SAFETY",
        payload: "ws-123",
      };
      const result = adminReducer(initialAdminState, action);

      expect(result.ui.expandedSafety).toBe("ws-123");
    });

    it("should clear expanded safety when toggling same ID", () => {
      const stateWithExpanded: AdminState = {
        ...initialAdminState,
        ui: { ...initialAdminState.ui, expandedSafety: "ws-123" },
      };
      const action: AdminAction = {
        type: "TOGGLE_EXPANDED_SAFETY",
        payload: null,
      };
      const result = adminReducer(stateWithExpanded, action);

      expect(result.ui.expandedSafety).toBeNull();
    });
  });

  describe("Reset action", () => {
    it("should handle RESET action", () => {
      const messyState: AdminState = {
        auth: {
          isAuthenticated: true,
          showAuthModal: false,
          password: "secret",
          error: "some error",
        },
        worksheets: {
          items: [createMockWorksheet()],
          total: 100,
          loading: true,
          error: "fetch error",
          limit: 50,
          offset: 20,
          hasMore: true,
        },
        ui: {
          deleteConfirm: "ws-1",
          deleting: "ws-1",
          expandedSafety: "ws-2",
        },
        safetyCheck: {
          checking: true,
          progress: { current: 5, total: 10 },
        },
      };

      const action: AdminAction = { type: "RESET" };
      const result = adminReducer(messyState, action);

      expect(result).toEqual(initialAdminState);
    });
  });

  describe("State immutability", () => {
    it("should not mutate original state", () => {
      const originalState = { ...initialAdminState };
      const action: AdminAction = {
        type: "SET_PASSWORD",
        payload: "new-password",
      };

      adminReducer(initialAdminState, action);

      expect(initialAdminState).toEqual(originalState);
    });

    it("should not mutate nested objects", () => {
      const originalAuth = { ...initialAdminState.auth };
      const action: AdminAction = {
        type: "SET_AUTH_ERROR",
        payload: "Error message",
      };

      adminReducer(initialAdminState, action);

      expect(initialAdminState.auth).toEqual(originalAuth);
    });
  });

  describe("Complex workflows", () => {
    it("should handle complete auth flow", () => {
      let state = initialAdminState;

      // User enters password
      state = adminReducer(state, {
        type: "SET_PASSWORD",
        payload: "correct-password",
      });
      expect(state.auth.password).toBe("correct-password");

      // Clear any auth error
      state = adminReducer(state, { type: "CLEAR_AUTH_ERROR" });
      expect(state.auth.error).toBeNull();

      // Set authenticated
      state = adminReducer(state, {
        type: "SET_AUTH",
        payload: { isAuthenticated: true, showAuthModal: false },
      });
      expect(state.auth.isAuthenticated).toBe(true);
      expect(state.auth.showAuthModal).toBe(false);
    });

    it("should handle complete worksheet deletion flow", () => {
      const worksheets = [
        createMockWorksheet({ id: "ws-1" }),
        createMockWorksheet({ id: "ws-2" }),
      ];
      let state: AdminState = {
        ...initialAdminState,
        worksheets: {
          ...initialAdminState.worksheets,
          items: worksheets,
          total: 2,
        },
      };

      // User clicks delete
      state = adminReducer(state, {
        type: "SET_DELETE_CONFIRM",
        payload: "ws-1",
      });
      expect(state.ui.deleteConfirm).toBe("ws-1");

      // Start deleting
      state = adminReducer(state, {
        type: "SET_DELETING",
        payload: "ws-1",
      });
      expect(state.ui.deleting).toBe("ws-1");

      // Delete succeeds
      state = adminReducer(state, {
        type: "DELETE_WORKSHEET_SUCCESS",
        payload: "ws-1",
      });
      expect(state.worksheets.items).toHaveLength(1);
      expect(state.worksheets.items[0].id).toBe("ws-2");
      expect(state.ui.deleteConfirm).toBeNull();
      expect(state.ui.deleting).toBeNull();
    });

    it("should handle pagination workflow", () => {
      let state = initialAdminState;

      // First fetch
      state = adminReducer(state, { type: "FETCH_WORKSHEETS_START" });
      state = adminReducer(state, {
        type: "FETCH_WORKSHEETS_SUCCESS",
        payload: {
          items: [
            createMockWorksheet({ id: "ws-1" }),
            createMockWorksheet({ id: "ws-2" }),
          ],
          total: 100,
        },
      });
      expect(state.worksheets.items).toHaveLength(2);
      expect(state.worksheets.hasMore).toBe(true);

      // Load more
      state = adminReducer(state, { type: "LOAD_MORE_WORKSHEETS" });
      expect(state.worksheets.offset).toBe(20); // limit is 20

      state = adminReducer(state, { type: "FETCH_WORKSHEETS_START" });
      state = adminReducer(state, {
        type: "FETCH_WORKSHEETS_SUCCESS",
        payload: {
          items: [
            createMockWorksheet({ id: "ws-3" }),
            createMockWorksheet({ id: "ws-4" }),
          ],
          total: 100,
        },
      });
      expect(state.worksheets.items).toHaveLength(4); // Previous + new
      expect(state.worksheets.offset).toBe(20);
    });
  });
});
