import { useCallback } from "react";
import { getAdminAuthToken, isAdminAuthenticated } from "@/lib/adminAuth";
import type { AdminWorksheet } from "@/lib/reducers/adminReducer";

export interface FetchWorksheetsParams {
  limit?: number;
  offset?: number;
  search?: string;
}

export interface FetchWorksheetsResponse {
  worksheets: AdminWorksheet[];
  total: number;
}

export interface AuthResponse {
  token: string;
  expiry: number;
}

export interface SafetyCheckResult {
  worksheetId: string;
  safe: boolean;
  categories: string[];
  explanation: string;
  usingAI: boolean;
  confidence: number;
  timestamp: string;
}

export interface SafetyCheckResponse {
  results: SafetyCheckResult[];
  summary: {
    safe: number;
    flagged: number;
  };
}

/**
 * Custom hook for admin API operations
 * Handles authentication, worksheets, and safety checking
 */
export function useAdminAPI() {
  /**
   * Authenticate with admin password
   */
  const authenticate = useCallback(async (password: string) => {
    const response = await fetch("/api/v1/admin/auth", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ password }),
    });

    const data: AuthResponse | { error: string } = await response.json();

    if (!response.ok) {
      throw new Error("error" in data ? data.error : "Authentication failed");
    }

    return data as AuthResponse;
  }, []);

  /**
   * Fetch worksheets with pagination and optional search
   */
  const fetchWorksheets = useCallback(
    async (params: FetchWorksheetsParams = {}) => {
      const token = getAdminAuthToken();
      if (!token) {
        throw new Error("Session expired, please login again");
      }

      const searchParams = new URLSearchParams();
      // Always include limit and offset with defaults
      const limit = params.limit ?? 20;
      const offset = params.offset ?? 0;

      searchParams.append("limit", limit.toString());
      searchParams.append("offset", offset.toString());

      if (params.search) searchParams.append("search", params.search);

      const response = await fetch(
        `/api/v1/admin/worksheets?${searchParams.toString()}`,
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        },
      );

      if (!response.ok) {
        if (response.status === 401) {
          throw new Error("Unauthorized");
        }
        throw new Error("Failed to fetch worksheets");
      }

      const data: FetchWorksheetsResponse = await response.json();
      return data;
    },
    [],
  );

  /**
   * Delete a worksheet by ID
   */
  const deleteWorksheet = useCallback(async (id: string) => {
    const token = getAdminAuthToken();
    if (!token) {
      throw new Error("Session expired, please login again");
    }

    const response = await fetch(`/api/v1/admin/worksheets/${id}`, {
      method: "DELETE",
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });

    if (!response.ok) {
      throw new Error("Failed to delete worksheet");
    }
  }, []);

  /**
   * Check content safety of worksheets
   */
  const checkContentSafety = useCallback(async (worksheetIds: string[]) => {
    const token = getAdminAuthToken();
    if (!token) {
      throw new Error("Session expired, please login again");
    }

    const response = await fetch("/api/v1/admin/check-safety", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify({ worksheetIds }),
    });

    if (!response.ok) {
      throw new Error("Failed to check content safety");
    }

    const data: SafetyCheckResponse = await response.json();
    return data;
  }, []);

  /**
   * Check if user is authenticated
   */
  const isAuthenticated = useCallback(() => {
    return isAdminAuthenticated();
  }, []);

  return {
    authenticate,
    fetchWorksheets,
    deleteWorksheet,
    checkContentSafety,
    isAuthenticated,
  };
}
