import { useEffect, useReducer, useCallback } from "react";
import { Link } from "react-router-dom";
import Button from "@/components/Button";
import Card from "@/components/Card";
import SearchBar from "@/components/SearchBar";
import DeleteConfirmationDialog from "@/components/DeleteConfirmationDialog";
import WorksheetCard from "@/components/WorksheetCard";
import { clearAdminAuthToken } from "@/lib/adminAuth";
import {
  adminReducer,
  initialAdminState,
  AdminWorksheet,
} from "@/lib/reducers/adminReducer";
import { useAdminAPI } from "@/lib/hooks/useAdminAPI";

export default function AdminManage() {
  const [state, dispatch] = useReducer(adminReducer, initialAdminState);
  const {
    authenticate,
    fetchWorksheets: fetchWorksheetsAPI,
    deleteWorksheet: deleteWorksheetAPI,
    checkContentSafety,
    isAuthenticated,
  } = useAdminAPI();

  const fetchWorksheets = useCallback(async () => {
    dispatch({ type: "FETCH_WORKSHEETS_START" });

    try {
      const data = await fetchWorksheetsAPI({
        limit: state.worksheets.limit,
        offset: state.worksheets.offset,
        search: state.ui.searchQuery || undefined,
      });

      console.log(
        "✅ Worksheets loaded:",
        data.worksheets?.length || 0,
        "items",
      );
      dispatch({
        type: "FETCH_WORKSHEETS_SUCCESS",
        payload: {
          items: data.worksheets || [],
          total: data.total || 0,
        },
      });
    } catch (err) {
      if (err instanceof Error && err.message === "Unauthorized") {
        dispatch({
          type: "SET_AUTH",
          payload: { isAuthenticated: false, showAuthModal: true },
        });
      } else {
        console.error("❌ Fetch worksheets error:", err);
        dispatch({
          type: "FETCH_WORKSHEETS_ERROR",
          payload:
            err instanceof Error ? err.message : "Failed to load worksheets",
        });
      }
    }
  }, [
    fetchWorksheetsAPI,
    state.ui.searchQuery,
    state.worksheets.limit,
    state.worksheets.offset,
  ]);

  // Check authentication on mount
  useEffect(() => {
    const isAuth = isAuthenticated();

    if (isAuth) {
      dispatch({
        type: "SET_AUTH",
        payload: { isAuthenticated: true, showAuthModal: false },
      });
      fetchWorksheets();
    }
  }, [isAuthenticated, fetchWorksheets]);

  const handleAuthSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    dispatch({ type: "CLEAR_AUTH_ERROR" });

    try {
      const data = await authenticate(state.auth.password);

      // Store token
      localStorage.setItem("admin_token", data.token);
      localStorage.setItem("admin_token_expiry", data.expiry.toString());
      dispatch({
        type: "SET_AUTH",
        payload: { isAuthenticated: true, showAuthModal: false },
      });
      await fetchWorksheets();
    } catch (err) {
      dispatch({
        type: "SET_AUTH_ERROR",
        payload: err instanceof Error ? err.message : "Authentication failed",
      });
    }
  };

  const handleDelete = async (id: string) => {
    dispatch({ type: "SET_DELETE_CONFIRM", payload: null });
    dispatch({ type: "SET_DELETING", payload: id });

    try {
      await deleteWorksheetAPI(id);
      dispatch({ type: "DELETE_WORKSHEET_SUCCESS", payload: id });
    } catch (err) {
      dispatch({
        type: "DELETE_WORKSHEET_ERROR",
        payload:
          err instanceof Error ? err.message : "Failed to delete worksheet",
      });
    }
  };

  const handleCheckAllSafety = async () => {
    dispatch({
      type: "START_SAFETY_CHECK",
      payload: state.worksheets.items.length,
    });

    try {
      const data = await checkContentSafety(
        state.worksheets.items.map((w) => w.id),
      );

      // Update worksheets with safety results
      const updatedWorksheets: AdminWorksheet[] = state.worksheets.items.map(
        (worksheet) => {
          const result = data.results.find(
            (r) => r.worksheetId === worksheet.id,
          );

          if (result) {
            const method: "AI-based" | "pattern-based" = result.usingAI
              ? "AI-based"
              : "pattern-based";
            return {
              ...worksheet,
              safety: {
                isFlagged: !result.safe,
                categories: result.categories,
                explanation: result.explanation,
                method,
                confidence: result.confidence,
                lastChecked: result.timestamp,
              },
            };
          }

          return worksheet;
        },
      );

      dispatch({
        type: "SAFETY_CHECK_SUCCESS",
        payload: updatedWorksheets,
      });
      console.log(
        `✅ Safety check complete: ${data.summary.safe} safe, ${data.summary.flagged} flagged`,
      );
    } catch (err) {
      dispatch({
        type: "SAFETY_CHECK_ERROR",
        payload:
          err instanceof Error ? err.message : "Failed to check content safety",
      });
    }
  };

  const handleLogout = () => {
    clearAdminAuthToken();
    dispatch({ type: "RESET" });
  };

  const handleLoadMore = () => {
    dispatch({ type: "LOAD_MORE_WORKSHEETS" });
    fetchWorksheets();
  };

  // Render logic - early return for auth modal
  if (state.auth.showAuthModal && !state.auth.isAuthenticated) {
    return (
      <div className="min-h-screen bg-gray-50">
        <header className="bg-white shadow-sm border-b border-gray-200">
          <div className="container mx-auto px-4 py-4">
            <Link to="/" className="hover:scale-110 transition-transform">
              <img
                src="/images/logo.webp"
                alt="Math Pup Logo"
                className="w-10 h-10 object-contain"
              />
            </Link>
          </div>
        </header>

        <main className="container mx-auto px-4 py-16 max-w-md">
          <Card className="p-8">
            <div className="text-center mb-6">
              <h1 className="text-3xl font-display font-bold text-gray-900 mb-2">
                Admin Portal
              </h1>
              <p className="text-gray-600">Enter password to access</p>
            </div>

            <form onSubmit={handleAuthSubmit} className="space-y-4">
              <div>
                <label
                  htmlFor="password"
                  className="block text-sm font-medium text-gray-700 mb-2"
                >
                  Password
                </label>
                <input
                  id="password"
                  type="password"
                  value={state.auth.password}
                  onChange={(e) =>
                    dispatch({ type: "SET_PASSWORD", payload: e.target.value })
                  }
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                  placeholder="Enter admin password"
                  autoFocus
                />
              </div>

              {state.auth.error && (
                <div className="bg-red-50 border border-red-300 text-red-800 px-4 py-3 rounded text-sm">
                  {state.auth.error}
                </div>
              )}

              <Button type="submit" variant="primary" className="w-full">
                Access Admin Panel
              </Button>
            </form>
          </Card>
        </main>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-white shadow-sm border-b border-gray-200">
        <div className="container mx-auto px-4 py-4 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <Link to="/" className="hover:scale-110 transition-transform">
              <img
                src="/images/logo.webp"
                alt="Math Pup Logo"
                className="w-10 h-10 object-contain"
              />
            </Link>
            <h1 className="text-2xl font-display font-bold text-gray-900">
              Admin Panel
            </h1>
          </div>
          <Button variant="secondary" onClick={handleLogout}>
            Logout
          </Button>
        </div>
      </header>

      <main className="container mx-auto px-4 py-8 max-w-6xl">
        {/* Title */}
        <div className="mb-8">
          <h2 className="text-3xl font-display font-bold text-gray-900 mb-2">
            Manage Shared Worksheets
          </h2>
          <p className="text-gray-600 mb-6">
            View and manage all community-shared worksheets
          </p>
          <SearchBar
            value={state.ui.searchQuery}
            onChange={(value) =>
              dispatch({ type: "SET_SEARCH_QUERY", payload: value })
            }
            onSearch={fetchWorksheets}
            disabled={state.worksheets.loading}
          />
        </div>

        {/* Error */}
        {state.worksheets.error && (
          <Card className="mb-6 bg-red-50 border-red-300 p-4">
            <div className="flex items-start gap-3">
              <span className="text-2xl flex-shrink-0">⚠️</span>
              <div className="flex-1">
                <h3 className="font-bold text-red-900 mb-1">Error</h3>
                <p className="text-sm text-red-800">{state.worksheets.error}</p>
              </div>
            </div>
          </Card>
        )}

        {/* Loading State */}
        {state.worksheets.loading ? (
          <Card className="p-8 text-center">
            <div className="text-6xl mb-4 animate-spin">⏳</div>
            <p className="text-gray-600">Loading worksheets...</p>
          </Card>
        ) : state.worksheets.items.length === 0 ? (
          <Card className="p-8 text-center">
            <div className="text-6xl mb-4">📋</div>
            <p className="text-gray-600">No worksheets found</p>
          </Card>
        ) : (
          <div className="space-y-4">
            <div className="flex items-center justify-between mb-6">
              <div className="text-sm text-gray-600">
                Total worksheets:{" "}
                <span className="font-bold">{state.worksheets.total}</span>
              </div>
              <Button
                onClick={handleCheckAllSafety}
                disabled={
                  state.safetyCheck.checking ||
                  state.worksheets.items.length === 0
                }
                variant="primary"
                className="gap-2"
              >
                {state.safetyCheck.checking ? (
                  <>
                    <span className="animate-spin">⏳</span>
                    Checking...{" "}
                    {state.safetyCheck.progress &&
                      `${state.safetyCheck.progress.current}/${state.safetyCheck.progress.total}`}
                  </>
                ) : (
                  <>🔍 Check All Content</>
                )}
              </Button>
            </div>

            {state.worksheets.items.map((worksheet) => (
              <WorksheetCard
                key={worksheet.id}
                worksheet={worksheet}
                onDelete={(id) =>
                  dispatch({ type: "SET_DELETE_CONFIRM", payload: id })
                }
                onConfirmDelete={handleDelete}
                deleteConfirm={state.ui.deleteConfirm}
                deleting={state.ui.deleting}
                expandedSafety={state.ui.expandedSafety}
                onToggleExpandedSafety={(id) =>
                  dispatch({ type: "TOGGLE_EXPANDED_SAFETY", payload: id })
                }
              />
            ))}

            {state.worksheets.hasMore && (
              <div className="text-center mt-8">
                <Button
                  onClick={handleLoadMore}
                  variant="secondary"
                  disabled={state.worksheets.loading}
                >
                  {state.worksheets.loading ? "Loading..." : "Load More"}
                </Button>
              </div>
            )}
          </div>
        )}

        {/* Delete Confirmation Dialog */}
        {state.ui.deleteConfirm && (
          <DeleteConfirmationDialog
            title={
              state.worksheets.items.find(
                (w) => w.id === state.ui.deleteConfirm,
              )?.title || "Worksheet"
            }
            subtitle={
              state.worksheets.items.find(
                (w) => w.id === state.ui.deleteConfirm,
              )?.subtitle
            }
            isDeleting={Boolean(state.ui.deleting)}
            onConfirm={() => handleDelete(state.ui.deleteConfirm || "")}
            onCancel={() =>
              dispatch({ type: "SET_DELETE_CONFIRM", payload: null })
            }
          />
        )}
      </main>
    </div>
  );
}
