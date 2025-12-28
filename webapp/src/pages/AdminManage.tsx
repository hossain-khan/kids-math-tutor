import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import Button from "@/components/Button";
import Card from "@/components/Card";
import {
  getAdminAuthToken,
  clearAdminAuthToken,
  isAdminAuthenticated,
} from "@/lib/adminAuth";
import type { ProblemSpec } from "@/lib/schemas/challenge-schema";

interface SafetyStatus {
  isFlagged: boolean;
  categories?: string[];
  explanation?: string;
  method?: "AI-based" | "pattern-based";
  confidence?: number;
  lastChecked?: string;
}

interface AdminWorksheet {
  id: string;
  type: "explicit" | "generated";
  title: string;
  subtitle?: string;
  description?: string;
  problems?: ProblemSpec[];
  createdAt: string;
  stats: {
    views: number;
    downloads: number;
    averageRating: number;
    ratingCount: number;
  };
  safety?: SafetyStatus;
}

export default function AdminManage() {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [showAuthModal, setShowAuthModal] = useState(true);
  const [password, setPassword] = useState("");
  const [authError, setAuthError] = useState<string | null>(null);
  const [worksheets, setWorksheets] = useState<AdminWorksheet[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [deleteConfirm, setDeleteConfirm] = useState<string | null>(null);
  const [deleting, setDeleting] = useState<string | null>(null);
  const [checking, setChecking] = useState(false);
  const [checkProgress, setCheckProgress] = useState<{
    current: number;
    total: number;
  } | null>(null);
  const [expandedSafety, setExpandedSafety] = useState<string | null>(null);

  // Check authentication on mount
  useEffect(() => {
    const isAuth = isAdminAuthenticated();

    if (isAuth) {
      setIsAuthenticated(true);
      setShowAuthModal(false);
      fetchWorksheets();
    }
  }, []);

  const handleAuthSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setAuthError(null);

    try {
      const response = await fetch("/api/v1/admin/auth", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ password }),
      });

      const data = await response.json();

      if (!response.ok) {
        setAuthError(data.error || "Authentication failed");
        return;
      }

      // Store token
      localStorage.setItem("admin_token", data.token);
      localStorage.setItem("admin_token_expiry", data.expiry.toString());
      setIsAuthenticated(true);
      setShowAuthModal(false);
      setPassword("");
      fetchWorksheets();
    } catch {
      setAuthError("Connection error. Please try again.");
    }
  };

  const fetchWorksheets = async () => {
    setLoading(true);
    setError(null);

    try {
      const token = getAdminAuthToken();

      if (!token) {
        setError("Session expired, please login again");
        return;
      }

      const response = await fetch("/api/v1/admin/worksheets", {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      if (!response.ok) {
        if (response.status === 401) {
          console.error("❌ Unauthorized - token invalid");
          setIsAuthenticated(false);
          setShowAuthModal(true);
          return;
        }
        throw new Error("Failed to fetch worksheets");
      }

      const data = await response.json();
      console.log(
        "✅ Worksheets loaded:",
        data.worksheets?.length || 0,
        "items",
      );
      setWorksheets(data.worksheets || []);
    } catch (err) {
      console.error("❌ Fetch worksheets error:", err);
      setError(
        err instanceof Error ? err.message : "Failed to load worksheets",
      );
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id: string) => {
    setDeleting(id);

    try {
      const token = getAdminAuthToken();
      const response = await fetch(`/api/v1/admin/worksheets/${id}`, {
        method: "DELETE",
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      if (!response.ok) {
        throw new Error("Failed to delete worksheet");
      }

      setWorksheets(worksheets.filter((w) => w.id !== id));
      setDeleteConfirm(null);
    } catch (err) {
      setError(
        err instanceof Error ? err.message : "Failed to delete worksheet",
      );
    } finally {
      setDeleting(null);
    }
  };

  const handleCheckAllSafety = async () => {
    setChecking(true);
    setError(null);

    try {
      const token = getAdminAuthToken();

      if (!token) {
        setError("Session expired, please login again");
        return;
      }

      setCheckProgress({ current: 0, total: worksheets.length });

      const response = await fetch("/api/v1/admin/check-safety", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({
          worksheetIds: worksheets.map((w) => w.id),
        }),
      });

      if (!response.ok) {
        throw new Error("Failed to check content safety");
      }

      const data = await response.json();

      // Update worksheets with safety results
      const updatedWorksheets: AdminWorksheet[] = worksheets.map(
        (worksheet) => {
          const result = data.results.find(
            // eslint-disable-next-line @typescript-eslint/no-explicit-any
            (r: any) => r.worksheetId === worksheet.id,
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

      setWorksheets(updatedWorksheets);
      console.log(
        `✅ Safety check complete: ${data.summary.safe} safe, ${data.summary.flagged} flagged`,
      );
    } catch (err) {
      setError(
        err instanceof Error ? err.message : "Failed to check content safety",
      );
    } finally {
      setChecking(false);
      setCheckProgress(null);
    }
  };

  const handleLogout = () => {
    clearAdminAuthToken();
    setIsAuthenticated(false);
    setShowAuthModal(true);
  };

  // Render logic
  if (!showAuthModal && !isAuthenticated) {
    setShowAuthModal(true);
  }

  if (showAuthModal && !isAuthenticated) {
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
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                  placeholder="Enter admin password"
                  autoFocus
                />
              </div>

              {authError && (
                <div className="bg-red-50 border border-red-300 text-red-800 px-4 py-3 rounded text-sm">
                  {authError}
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
          <p className="text-gray-600">
            View and manage all community-shared worksheets
          </p>
        </div>

        {/* Error */}
        {error && (
          <Card className="mb-6 bg-red-50 border-red-300 p-4">
            <div className="flex items-start gap-3">
              <span className="text-2xl flex-shrink-0">⚠️</span>
              <div className="flex-1">
                <h3 className="font-bold text-red-900 mb-1">Error</h3>
                <p className="text-sm text-red-800">{error}</p>
              </div>
            </div>
          </Card>
        )}

        {/* Loading State */}
        {loading ? (
          <Card className="p-8 text-center">
            <div className="text-6xl mb-4 animate-spin">⏳</div>
            <p className="text-gray-600">Loading worksheets...</p>
          </Card>
        ) : worksheets.length === 0 ? (
          <Card className="p-8 text-center">
            <div className="text-6xl mb-4">📋</div>
            <p className="text-gray-600">No worksheets found</p>
          </Card>
        ) : (
          <div className="space-y-4">
            <div className="flex items-center justify-between mb-6">
              <div className="text-sm text-gray-600">
                Total worksheets:{" "}
                <span className="font-bold">{worksheets.length}</span>
              </div>
              <Button
                onClick={handleCheckAllSafety}
                disabled={checking || worksheets.length === 0}
                variant="primary"
                className="gap-2"
              >
                {checking ? (
                  <>
                    <span className="animate-spin">⏳</span>
                    Checking...{" "}
                    {checkProgress &&
                      `${checkProgress.current}/${checkProgress.total}`}
                  </>
                ) : (
                  <>🔍 Check All Content</>
                )}
              </Button>
            </div>

            {worksheets.map((worksheet) => (
              <div key={worksheet.id}>
                <Card className="p-6">
                  <div className="flex items-start justify-between gap-4">
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center gap-3 mb-2">
                        <h3 className="text-xl font-bold text-gray-900 break-words">
                          {worksheet.title}
                        </h3>
                        {worksheet.safety && (
                          <span
                            className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium whitespace-nowrap ${
                              worksheet.safety.isFlagged
                                ? "bg-orange-100 text-orange-800"
                                : "bg-green-100 text-green-800"
                            }`}
                          >
                            {worksheet.safety.isFlagged
                              ? "⚠️ Flagged"
                              : "✅ Safe"}
                          </span>
                        )}
                      </div>
                      {worksheet.subtitle && (
                        <p className="text-gray-600 mb-2 break-words">
                          {worksheet.subtitle}
                        </p>
                      )}
                      <div className="flex flex-wrap gap-4 text-sm text-gray-600 mb-3">
                        <span>
                          📊 {worksheet.problems?.length || 0} problems
                        </span>
                        <span>👁️ {worksheet.stats.views} views</span>
                        <span>💾 {worksheet.stats.downloads} downloads</span>
                        <span>
                          ⭐ {worksheet.stats.averageRating.toFixed(1)} rating
                        </span>
                        <span>
                          📅{" "}
                          {new Date(worksheet.createdAt).toLocaleDateString()}
                        </span>
                      </div>
                      {worksheet.description && (
                        <p className="text-sm text-gray-700 break-words">
                          {worksheet.description}
                        </p>
                      )}
                    </div>

                    <div className="flex flex-col gap-2 flex-shrink-0">
                      <a
                        href={`/worksheets/${worksheet.id}`}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="text-primary-600 hover:text-primary-700 text-sm font-medium"
                      >
                        View →
                      </a>

                      {worksheet.safety && worksheet.safety.isFlagged && (
                        <button
                          onClick={() =>
                            setExpandedSafety(
                              expandedSafety === worksheet.id
                                ? null
                                : worksheet.id,
                            )
                          }
                          className="text-orange-600 hover:text-orange-700 text-sm font-medium"
                        >
                          {expandedSafety === worksheet.id
                            ? "Hide Details"
                            : "Show Details"}
                        </button>
                      )}

                      {deleteConfirm === worksheet.id ? (
                        <div className="flex gap-2">
                          <button
                            onClick={() => handleDelete(worksheet.id)}
                            disabled={deleting === worksheet.id}
                            className="px-3 py-1 text-xs font-medium bg-red-600 text-white rounded hover:bg-red-700 disabled:opacity-50"
                          >
                            {deleting === worksheet.id
                              ? "Deleting..."
                              : "Confirm"}
                          </button>
                          <button
                            onClick={() => setDeleteConfirm(null)}
                            disabled={deleting === worksheet.id}
                            className="px-3 py-1 text-xs font-medium bg-gray-300 text-gray-900 rounded hover:bg-gray-400 disabled:opacity-50"
                          >
                            Cancel
                          </button>
                        </div>
                      ) : (
                        <button
                          onClick={() => setDeleteConfirm(worksheet.id)}
                          className="px-3 py-1 text-xs font-medium bg-red-50 text-red-600 rounded hover:bg-red-100 transition-colors"
                        >
                          Delete
                        </button>
                      )}
                    </div>
                  </div>
                </Card>

                {expandedSafety === worksheet.id &&
                  worksheet.safety?.isFlagged && (
                    <Card className="mt-2 p-4 bg-orange-50 border-orange-300">
                      <div className="space-y-2">
                        <h4 className="font-bold text-orange-900">
                          ⚠️ Safety Issues Found
                        </h4>
                        {worksheet.safety.categories &&
                          worksheet.safety.categories.length > 0 && (
                            <div>
                              <span className="text-sm font-medium text-orange-900">
                                Categories:
                              </span>
                              <div className="flex flex-wrap gap-2 mt-1">
                                {worksheet.safety.categories.map((cat) => (
                                  <span
                                    key={cat}
                                    className="inline-block bg-orange-200 text-orange-900 px-2 py-1 rounded text-xs"
                                  >
                                    {cat}
                                  </span>
                                ))}
                              </div>
                            </div>
                          )}
                        {worksheet.safety.explanation && (
                          <div>
                            <span className="text-sm font-medium text-orange-900">
                              Reason:
                            </span>
                            <p className="text-sm text-orange-800 mt-1">
                              {worksheet.safety.explanation}
                            </p>
                          </div>
                        )}
                        <div className="text-xs text-orange-700 pt-2 border-t border-orange-200">
                          <p>
                            Method: {worksheet.safety.method} | Confidence:{" "}
                            {(worksheet.safety.confidence || 0).toFixed(0)}%
                          </p>
                          {worksheet.safety.lastChecked && (
                            <p>
                              Last checked:{" "}
                              {new Date(
                                worksheet.safety.lastChecked,
                              ).toLocaleString()}
                            </p>
                          )}
                        </div>
                      </div>
                    </Card>
                  )}
              </div>
            ))}
          </div>
        )}
      </main>
    </div>
  );
}
