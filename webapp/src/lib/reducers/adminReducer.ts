interface SafetyStatus {
  isFlagged: boolean;
  categories?: string[];
  explanation?: string;
  method?: "AI-based" | "pattern-based";
  confidence?: number;
  lastChecked?: string;
}

export interface AdminWorksheet {
  id: string;
  type: "explicit" | "generated";
  title: string;
  subtitle?: string;
  description?: string;
  problemCount: number;
  createdAt: string;
  stats: {
    views: number;
    downloads: number;
    averageRating: number;
    ratingCount: number;
  };
  safety?: SafetyStatus;
}

interface AuthState {
  isAuthenticated: boolean;
  showAuthModal: boolean;
  password: string;
  error: string | null;
}

interface WorksheetState {
  items: AdminWorksheet[];
  total: number;
  loading: boolean;
  error: string | null;
  limit: number;
  offset: number;
  hasMore: boolean;
}

interface UIState {
  deleteConfirm: string | null;
  deleting: string | null;
  expandedSafety: string | null;
  searchQuery: string;
}

interface SafetyCheckState {
  checking: boolean;
  progress: {
    current: number;
    total: number;
  } | null;
}

export interface AdminState {
  auth: AuthState;
  worksheets: WorksheetState;
  ui: UIState;
  safetyCheck: SafetyCheckState;
}

export type AdminAction =
  // Auth actions
  | {
      type: "SET_AUTH";
      payload: { isAuthenticated: boolean; showAuthModal: boolean };
    }
  | { type: "SET_PASSWORD"; payload: string }
  | { type: "SET_AUTH_ERROR"; payload: string | null }
  | { type: "CLEAR_AUTH_ERROR" }
  // Worksheet actions
  | { type: "FETCH_WORKSHEETS_START" }
  | {
      type: "FETCH_WORKSHEETS_SUCCESS";
      payload: { items: AdminWorksheet[]; total: number };
    }
  | { type: "FETCH_WORKSHEETS_ERROR"; payload: string }
  | { type: "SET_WORKSHEET_LIMIT"; payload: number }
  | { type: "SET_WORKSHEET_OFFSET"; payload: number }
  | { type: "LOAD_MORE_WORKSHEETS" }
  // Delete actions
  | { type: "SET_DELETE_CONFIRM"; payload: string | null }
  | { type: "SET_DELETING"; payload: string | null }
  | { type: "DELETE_WORKSHEET_SUCCESS"; payload: string }
  | { type: "DELETE_WORKSHEET_ERROR"; payload: string }
  // Safety check actions
  | { type: "START_SAFETY_CHECK"; payload: number }
  | {
      type: "UPDATE_SAFETY_PROGRESS";
      payload: { current: number; total: number };
    }
  | { type: "SAFETY_CHECK_SUCCESS"; payload: AdminWorksheet[] }
  | { type: "SAFETY_CHECK_ERROR"; payload: string }
  | { type: "STOP_SAFETY_CHECK" }
  // UI actions
  | { type: "TOGGLE_EXPANDED_SAFETY"; payload: string | null }
  | { type: "SET_SEARCH_QUERY"; payload: string }
  | { type: "RESET" };

export const initialAdminState: AdminState = {
  auth: {
    isAuthenticated: false,
    showAuthModal: true,
    password: "",
    error: null,
  },
  worksheets: {
    items: [],
    total: 0,
    loading: false,
    error: null,
    limit: 20,
    offset: 0,
    hasMore: false,
  },
  ui: {
    deleteConfirm: null,
    deleting: null,
    expandedSafety: null,
    searchQuery: "",
  },
  safetyCheck: {
    checking: false,
    progress: null,
  },
};

export function adminReducer(
  state: AdminState,
  action: AdminAction,
): AdminState {
  switch (action.type) {
    // Auth actions
    case "SET_AUTH":
      return {
        ...state,
        auth: {
          ...state.auth,
          isAuthenticated: action.payload.isAuthenticated,
          showAuthModal: action.payload.showAuthModal,
        },
      };
    case "SET_PASSWORD":
      return {
        ...state,
        auth: { ...state.auth, password: action.payload },
      };
    case "SET_AUTH_ERROR":
      return {
        ...state,
        auth: { ...state.auth, error: action.payload },
      };
    case "CLEAR_AUTH_ERROR":
      return {
        ...state,
        auth: { ...state.auth, error: null },
      };

    // Worksheet actions
    case "FETCH_WORKSHEETS_START":
      return {
        ...state,
        worksheets: {
          ...state.worksheets,
          loading: true,
          error: null,
        },
      };
    case "FETCH_WORKSHEETS_SUCCESS": {
      const { items, total } = action.payload;
      const hasMore = state.worksheets.offset + items.length < total;
      return {
        ...state,
        worksheets: {
          ...state.worksheets,
          items:
            state.worksheets.offset === 0
              ? items
              : [...state.worksheets.items, ...items],
          total,
          loading: false,
          error: null,
          hasMore,
        },
      };
    }
    case "FETCH_WORKSHEETS_ERROR":
      return {
        ...state,
        worksheets: {
          ...state.worksheets,
          loading: false,
          error: action.payload,
        },
      };
    case "SET_WORKSHEET_LIMIT":
      return {
        ...state,
        worksheets: {
          ...state.worksheets,
          limit: action.payload,
        },
      };
    case "SET_WORKSHEET_OFFSET":
      return {
        ...state,
        worksheets: {
          ...state.worksheets,
          offset: action.payload,
        },
      };
    case "LOAD_MORE_WORKSHEETS":
      return {
        ...state,
        worksheets: {
          ...state.worksheets,
          offset: state.worksheets.offset + state.worksheets.limit,
        },
      };

    // Delete actions
    case "SET_DELETE_CONFIRM":
      return {
        ...state,
        ui: { ...state.ui, deleteConfirm: action.payload },
      };
    case "SET_DELETING":
      return {
        ...state,
        ui: { ...state.ui, deleting: action.payload },
      };
    case "DELETE_WORKSHEET_SUCCESS": {
      const filteredItems = state.worksheets.items.filter(
        (w) => w.id !== action.payload,
      );
      return {
        ...state,
        worksheets: {
          ...state.worksheets,
          items: filteredItems,
          total: state.worksheets.total - 1,
        },
        ui: {
          ...state.ui,
          deleteConfirm: null,
          deleting: null,
        },
      };
    }
    case "DELETE_WORKSHEET_ERROR":
      return {
        ...state,
        worksheets: {
          ...state.worksheets,
          error: action.payload,
        },
        ui: {
          ...state.ui,
          deleting: null,
        },
      };

    // Safety check actions
    case "START_SAFETY_CHECK":
      return {
        ...state,
        safetyCheck: {
          checking: true,
          progress: { current: 0, total: action.payload },
        },
      };
    case "UPDATE_SAFETY_PROGRESS":
      return {
        ...state,
        safetyCheck: {
          ...state.safetyCheck,
          progress: action.payload,
        },
      };
    case "SAFETY_CHECK_SUCCESS":
      return {
        ...state,
        worksheets: {
          ...state.worksheets,
          items: action.payload,
        },
        safetyCheck: {
          checking: false,
          progress: null,
        },
      };
    case "SAFETY_CHECK_ERROR":
      return {
        ...state,
        worksheets: {
          ...state.worksheets,
          error: action.payload,
        },
        safetyCheck: {
          checking: false,
          progress: null,
        },
      };
    case "STOP_SAFETY_CHECK":
      return {
        ...state,
        safetyCheck: {
          checking: false,
          progress: null,
        },
      };

    // UI actions
    case "TOGGLE_EXPANDED_SAFETY":
      return {
        ...state,
        ui: {
          ...state.ui,
          expandedSafety: action.payload,
        },
      };
    case "SET_SEARCH_QUERY":
      return {
        ...state,
        ui: {
          ...state.ui,
          searchQuery: action.payload,
        },
        worksheets: {
          ...state.worksheets,
          offset: 0,
        },
      };

    case "RESET":
      return initialAdminState;

    default:
      return state;
  }
}
