import { useEffect, useState } from "react";
import { useParams, Link } from "react-router-dom";
import Button from "@/components/Button";
import Card from "@/components/Card";
import StarRating from "@/components/StarRating";
import { generateDeeplink, isLikelyAndroidDevice } from "@/lib/deeplink";
import { getOrCreateSessionId } from "@/lib/sessionId";
import { copyToClipboard, downloadJson } from "@/lib/utils";
import type { GradeLevel } from "@/lib/schemas/challenge-schema";

interface SharedWorksheet {
  id: string;
  type: "explicit";
  title: string;
  subtitle?: string;
  description?: string;
  grades: GradeLevel[];
  problems: Array<{
    operand1: number;
    operand2: number;
    operation: string;
  }>;
  createdAt: string;
  stats: {
    views: number;
    downloads: number;
    averageRating: number;
    ratingCount: number;
  };
}

interface WorksheetListItem {
  id: string;
  title: string;
  subtitle?: string;
  grades: GradeLevel[];
  problemCount: number;
  singleOperation?: string;
  createdAt: string;
  stats: {
    views: number;
    downloads: number;
    averageRating: number;
    ratingCount: number;
  };
}

interface PaginatedResults<T> {
  items: T[];
  total: number;
  limit: number;
  offset: number;
  hasMore: boolean;
}

const gradeLabels: Record<GradeLevel, string> = {
  kindergarten: "K",
  grade1: "1st",
  grade2: "2nd",
};

export default function SharedWorksheets() {
  const { id } = useParams<{ id?: string }>();
  const [worksheet, setWorksheet] = useState<SharedWorksheet | null>(null);
  const [worksheets, setWorksheets] = useState<WorksheetListItem[]>([]);
  const [searchQuery, setSearchQuery] = useState("");
  const [selectedGrades, setSelectedGrades] = useState<GradeLevel[]>([
    "kindergarten",
    "grade1",
    "grade2",
  ]);
  const [sortBy, setSortBy] = useState<
    "newest" | "views" | "downloads" | "ratings"
  >("newest");
  const [offset, setOffset] = useState(0);
  const [total, setTotal] = useState(0);
  const [hasMore, setHasMore] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [isAndroid, setIsAndroid] = useState(false);
  const [usingWorksheet, setUsingWorksheet] = useState(false);
  const [sessionId, setSessionId] = useState("");
  const [showAllProblems, setShowAllProblems] = useState(false);
  const [copiedLink, setCopiedLink] = useState(false);
  const [copiedJson, setCopiedJson] = useState(false);
  // Initialize session ID and Android check
  useEffect(() => {
    setIsAndroid(isLikelyAndroidDevice());
    setSessionId(getOrCreateSessionId());
  }, []);

  // Load single worksheet if ID provided
  useEffect(() => {
    if (!id) return;

    const fetchWorksheet = async () => {
      setLoading(true);
      setError(null);

      try {
        const response = await fetch(`/api/v1/worksheets/${id}`);

        if (!response.ok) {
          if (response.status === 404) {
            setError("Worksheet not found");
          } else {
            setError("Failed to load worksheet");
          }
          return;
        }

        const data = await response.json();
        setWorksheet(data);
      } catch (err) {
        console.error("Failed to fetch worksheet:", err);
        setError("Failed to load worksheet");
      } finally {
        setLoading(false);
      }
    };

    fetchWorksheet();
  }, [id]);

  // Helper function to refetch worksheet (for updated stats)
  const refetchWorksheet = async () => {
    if (!id) return;
    try {
      const response = await fetch(`/api/v1/worksheets/${id}`);
      if (response.ok) {
        const data = await response.json();
        setWorksheet(data);
      }
    } catch (err) {
      console.error("Failed to refetch worksheet:", err);
    }
  };

  // Load worksheets list with pagination and search
  useEffect(() => {
    if (id) return; // Don't load list if viewing single worksheet

    const fetchWorksheets = async () => {
      setLoading(true);
      setError(null);

      try {
        const params = new URLSearchParams();

        if (searchQuery) {
          params.append("q", searchQuery);
        }

        if (selectedGrades.length > 0) {
          params.append("grades", selectedGrades.join(","));
        }

        params.append("sort", sortBy);
        params.append("limit", "20");
        params.append("offset", offset.toString());

        // Use search endpoint if query present, otherwise use list endpoint
        const endpoint = searchQuery
          ? `/api/v1/worksheets/search?${params}`
          : `/api/v1/worksheets?${params}`;

        const response = await fetch(endpoint);

        if (!response.ok) {
          setError("Failed to load worksheets");
          return;
        }

        const data: PaginatedResults<WorksheetListItem> = await response.json();

        if (offset === 0) {
          setWorksheets(data.items);
        } else {
          setWorksheets((prev) => [...prev, ...data.items]);
        }

        setTotal(data.total);
        setHasMore(data.hasMore);
      } catch (err) {
        console.error("Failed to fetch worksheets:", err);
        setError("Failed to load worksheets");
      } finally {
        setLoading(false);
      }
    };

    fetchWorksheets();
  }, [id, searchQuery, selectedGrades, sortBy, offset]);

  const handleUseWorksheet = async () => {
    if (!worksheet) return;

    setUsingWorksheet(true);

    try {
      // Track download
      await fetch(`/api/v1/worksheets/${worksheet.id}/download`, {
        method: "POST",
      });

      // Refetch to get updated stats
      await refetchWorksheet();

      // Generate deeplink with the worksheet data
      const deeplink = generateDeeplink({
        type: "explicit",
        title: worksheet.title,
        subtitle: worksheet.subtitle,
        problems: worksheet.problems,
      });

      if (deeplink) {
        window.location.href = deeplink;
      }
    } catch (err) {
      console.error("Failed to use worksheet:", err);
    } finally {
      setUsingWorksheet(false);
    }
  };

  const handleCopyLink = async () => {
    if (!id) return;
    const shareUrl = `${window.location.origin}/worksheets/${id}`;
    const success = await copyToClipboard(shareUrl);
    if (success) {
      setCopiedLink(true);
      setTimeout(() => setCopiedLink(false), 2000);
    }
  };

  const handleCopyJson = async () => {
    if (!worksheet) return;
    const json = JSON.stringify(
      {
        type: worksheet.type,
        title: worksheet.title,
        subtitle: worksheet.subtitle,
        description: worksheet.description,
        problems: worksheet.problems,
      },
      null,
      2,
    );
    const success = await copyToClipboard(json);
    if (success) {
      setCopiedJson(true);
      setTimeout(() => setCopiedJson(false), 2000);
    }
  };

  const handleDownloadJson = () => {
    if (!worksheet) return;
    const data = {
      type: worksheet.type,
      title: worksheet.title,
      subtitle: worksheet.subtitle,
      description: worksheet.description,
      problems: worksheet.problems,
    };
    downloadJson(
      data,
      `${worksheet.title.toLowerCase().replace(/\s+/g, "-")}-worksheet.json`,
    );
  };

  const handleShareToSocial = (platform: "twitter" | "facebook") => {
    if (!id) return;
    const shareUrl = `${window.location.origin}/worksheets/${id}`;
    const text = `Check out this math worksheet: "${worksheet?.title}" - perfect for K-2 students!`;

    let url = "";
    if (platform === "twitter") {
      url = `https://twitter.com/intent/tweet?text=${encodeURIComponent(text)}&url=${encodeURIComponent(shareUrl)}`;
    } else if (platform === "facebook") {
      url = `https://www.facebook.com/sharer/sharer.php?u=${encodeURIComponent(shareUrl)}`;
    }

    if (url) {
      window.open(url, "_blank", "width=600,height=400");
    }
  };

  // Single worksheet view
  if (id) {
    if (loading) {
      return (
        <div className="min-h-screen flex items-center justify-center">
          <div className="text-center">
            <div className="text-6xl mb-4 animate-spin">⏳</div>
            <p className="text-gray-600">Loading worksheet...</p>
          </div>
        </div>
      );
    }

    if (error || !worksheet) {
      return (
        <div className="min-h-screen">
          <header className="bg-white shadow-sm border-b border-gray-200">
            <div className="container mx-auto px-4 py-4">
              <Link
                to="/"
                className="inline-flex items-center gap-2 text-blue-600 hover:text-blue-700"
              >
                <span>←</span>
                <span>Back to Home</span>
              </Link>
            </div>
          </header>
          <main className="container mx-auto px-4 py-8 max-w-2xl">
            <Card className="p-8 text-center">
              <div className="text-6xl mb-4">😕</div>
              <h1 className="text-2xl font-bold text-gray-900 mb-2">
                {error || "Worksheet not found"}
              </h1>
              <p className="text-gray-600 mb-6">
                The worksheet you&apos;re looking for doesn&apos;t exist or has
                been removed.
              </p>
              <Link to="/">
                <Button variant="primary">Go Home</Button>
              </Link>
            </Card>
          </main>
        </div>
      );
    }

    // Single worksheet detail view
    const problemCount = worksheet.problems.length;
    const previewProblems = worksheet.problems.slice(0, 3);
    const singleOperation = getSingleOperationType(worksheet.problems);
    const operationColor = getOperationColor(singleOperation || "mixed");

    return (
      <div className="min-h-screen">
        <header className="bg-white shadow-sm border-b border-gray-200">
          <div className="container mx-auto px-4 py-4">
            <Link
              to="/worksheets"
              className="inline-flex items-center gap-2 text-blue-600 hover:text-blue-700 mb-2"
            >
              <span>←</span>
              <span>Back to Library</span>
            </Link>
            <h1 className="text-2xl font-bold text-gray-900">
              Shared Worksheet
            </h1>
          </div>
        </header>

        <main className="container mx-auto px-4 py-8 max-w-2xl">
          {/* Worksheet Header */}
          <Card
            className={`mb-6 border-2 p-6 relative overflow-hidden bg-gradient-to-br ${operationColor.bg} ${operationColor.border}`}
          >
            {/* Operation Background Pattern */}
            <div className="absolute -top-6 -right-0 opacity-15 pointer-events-none">
              <div className="text-[140px] font-bold text-gray-400 leading-none">
                {operationColor.symbol}
              </div>
            </div>

            <div className="relative z-10">
              <h2 className="text-3xl font-display font-bold text-gray-900 mb-2">
                {worksheet.title}
              </h2>
              {worksheet.subtitle && (
                <p className="text-lg text-gray-600 mb-4">
                  {worksheet.subtitle}
                </p>
              )}
              {worksheet.description && (
                <p className="text-gray-700 mb-4">{worksheet.description}</p>
              )}

              {/* Metadata */}
              <div className="flex flex-wrap gap-4 text-sm text-gray-700 pt-4 border-t border-blue-200">
                <div className="flex items-center gap-2">
                  <span>📚</span>
                  <span>
                    <strong>{problemCount}</strong> problems
                  </span>
                </div>
                <div className="flex items-center gap-2">
                  <span>📊</span>
                  <span>
                    <strong>{worksheet.stats.views}</strong> views
                  </span>
                </div>
                <div className="flex items-center gap-2">
                  <span>⬇️</span>
                  <span>
                    <strong>{worksheet.stats.downloads}</strong> downloads
                  </span>
                </div>
                <div className="flex items-center gap-2">
                  <span>🎓</span>
                  <span>
                    Grades:{" "}
                    <strong>
                      {worksheet.grades.map((g) => gradeLabels[g]).join(", ")}
                    </strong>
                  </span>
                </div>

                {/* Rating */}
                <div className="flex items-center gap-4 ml-auto">
                  <StarRating
                    rating={worksheet.stats.averageRating}
                    count={worksheet.stats.ratingCount}
                    onRate={async (stars) => {
                      try {
                        const res = await fetch(
                          `/api/v1/worksheets/${worksheet.id}/rate`,
                          {
                            method: "POST",
                            headers: { "Content-Type": "application/json" },
                            body: JSON.stringify({ rating: stars, sessionId }),
                          },
                        );
                        if (res.ok) {
                          const data = await res.json();
                          setWorksheet((prev) =>
                            prev
                              ? {
                                  ...prev,
                                  stats: {
                                    ...prev.stats,
                                    averageRating: data.stats.averageRating,
                                    ratingCount: data.stats.ratingCount,
                                  },
                                }
                              : prev,
                          );
                        }
                      } catch (err) {
                        console.error("Failed to submit rating:", err);
                      }
                    }}
                    size="md"
                  />
                </div>
              </div>
            </div>
          </Card>

          {/* Preview Problems */}
          <Card className="mb-6 p-6">
            <h3 className="font-display font-bold text-lg mb-4">
              Preview{" "}
              <span className="font-display font-normal text-base text-gray-600">
                (showing {Math.min(3, problemCount)} out of {problemCount}{" "}
                {problemCount === 1 ? "problem" : "problems"})
              </span>
            </h3>
            <div className="space-y-3">
              {previewProblems.map((problem, idx) => (
                <div
                  key={idx}
                  className="bg-gray-50 p-4 rounded border border-gray-200"
                >
                  <div className="text-lg font-mono">
                    {problem.operand1} {getOperationSymbol(problem.operation)}{" "}
                    {problem.operand2} = ?
                  </div>
                </div>
              ))}
              {problemCount > 3 && (
                <button
                  onClick={() => setShowAllProblems(true)}
                  className="w-full mt-4 px-4 py-2 bg-blue-50 hover:bg-blue-100 border border-blue-300 rounded-lg text-blue-700 font-medium transition-colors text-center"
                >
                  View All {problemCount} Problems
                </button>
              )}
            </div>
          </Card>

          {/* Share & Copy Options */}
          <Card className="mb-6 p-6 bg-gray-50 border-gray-200">
            <h3 className="font-display font-bold text-lg mb-4">
              Share & Copy
            </h3>
            <div className="grid grid-cols-2 sm:grid-cols-4 gap-2">
              <button
                onClick={handleCopyLink}
                className="flex items-center justify-center gap-2 px-4 py-3 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors text-sm font-medium"
                title="Copy shareable link to clipboard"
              >
                <span>{copiedLink ? "✓" : "🔗"}</span>
                {copiedLink ? "Copied!" : "Copy Link"}
              </button>
              <button
                onClick={handleCopyJson}
                className="flex items-center justify-center gap-2 px-4 py-3 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors text-sm font-medium"
                title="Copy worksheet JSON to clipboard"
              >
                <span>{copiedJson ? "✓" : "📋"}</span>
                {copiedJson ? "Copied!" : "Copy JSON"}
              </button>
              <button
                onClick={handleDownloadJson}
                className="flex items-center justify-center gap-2 px-4 py-3 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors text-sm font-medium"
                title="Download worksheet as JSON file"
              >
                <span>⬇️</span>
                Download
              </button>
              <div className="relative group">
                <button
                  className="w-full flex items-center justify-center gap-2 px-4 py-3 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors text-sm font-medium"
                  title="Share to social media"
                >
                  <span>📱</span>
                  Share
                </button>
                <div className="absolute right-0 mt-2 w-32 bg-white border border-gray-300 rounded-lg shadow-lg opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all z-10">
                  <button
                    onClick={() => handleShareToSocial("twitter")}
                    className="w-full text-left px-4 py-2 hover:bg-blue-50 text-sm font-medium border-b border-gray-200"
                  >
                    Twitter/X
                  </button>
                  <button
                    onClick={() => handleShareToSocial("facebook")}
                    className="w-full text-left px-4 py-2 hover:bg-blue-50 text-sm font-medium"
                  >
                    Facebook
                  </button>
                </div>
              </div>
            </div>
          </Card>

          {/* Action Buttons */}
          <div className="flex gap-3 flex-col sm:flex-row">
            <Button
              variant="primary"
              size="lg"
              onClick={handleUseWorksheet}
              disabled={usingWorksheet}
              className="flex-1 bg-gradient-to-r from-green-500 to-emerald-500 hover:from-green-600 hover:to-emerald-600"
              title={
                !isAndroid
                  ? "Works best on Android devices with Math Pup app installed"
                  : ""
              }
            >
              {usingWorksheet ? (
                <>
                  <span aria-hidden="true" className="mr-2">
                    ⏳
                  </span>
                  Opening...
                </>
              ) : (
                <>
                  <span aria-hidden="true" className="mr-2">
                    🚀
                  </span>
                  Use This Worksheet
                </>
              )}
            </Button>
            <Link to="/worksheets" className="flex-1">
              <Button variant="secondary" size="lg" className="w-full">
                <span aria-hidden="true" className="mr-2">
                  📚
                </span>
                Browse More
              </Button>
            </Link>
          </div>

          {/* Non-Android Note */}
          {!isAndroid && (
            <Card className="mt-6 bg-blue-50 border-blue-200 p-4">
              <div className="flex items-start gap-3">
                <span className="text-2xl flex-shrink-0">📱</span>
                <div className="text-sm text-blue-900">
                  <p className="font-bold mb-1">Android Only</p>
                  <p>
                    Click &quot;Use This Worksheet&quot; to open this worksheet
                    in the Kids Math Pup Tutor app on your Android device.
                  </p>
                </div>
              </div>
            </Card>
          )}

          {/* All Problems Modal */}
          {showAllProblems && (
            <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
              <Card className="w-full max-w-2xl max-h-[90vh] overflow-y-auto bg-white">
                <div className="sticky top-0 bg-white border-b border-gray-200 p-6 flex items-center justify-between">
                  <h2 className="font-display font-bold text-xl md:text-2xl">
                    All Problems ({problemCount})
                  </h2>
                  <button
                    onClick={() => setShowAllProblems(false)}
                    className="text-gray-500 hover:text-gray-700 text-2xl font-bold"
                  >
                    ✕
                  </button>
                </div>

                <div className="p-6 space-y-3">
                  {worksheet.problems.map((problem, idx) => (
                    <div
                      key={idx}
                      className="bg-gray-50 p-4 rounded border border-gray-200 hover:border-blue-300 hover:bg-blue-50 transition-colors"
                    >
                      <div className="flex items-center justify-between">
                        <span className="text-sm text-gray-500 font-medium">
                          Problem {idx + 1}
                        </span>
                      </div>
                      <div className="text-lg md:text-xl font-mono font-bold text-gray-900 mt-2">
                        {problem.operand1}{" "}
                        {getOperationSymbol(problem.operation)}{" "}
                        {problem.operand2} ={" "}
                        <span className="text-green-600">
                          {calculateAnswer(
                            problem.operand1,
                            problem.operand2,
                            problem.operation,
                          )}
                        </span>
                      </div>
                    </div>
                  ))}
                </div>

                <div className="sticky bottom-0 bg-white border-t border-gray-200 p-6">
                  <button
                    onClick={() => setShowAllProblems(false)}
                    className="w-full px-4 py-3 bg-blue-600 hover:bg-blue-700 text-white font-bold rounded-lg transition-colors"
                  >
                    Close
                  </button>
                </div>
              </Card>
            </div>
          )}
        </main>
      </div>
    );
  }

  // Worksheets list view
  return (
    <div className="min-h-screen">
      <header className="bg-white shadow-sm border-b border-gray-200">
        <div className="container mx-auto px-4 py-4">
          <div className="flex items-center gap-3 mb-4">
            <Link to="/" className="hover:scale-110 transition-transform">
              <img
                src="/images/logo.webp"
                alt="Math Pup Logo"
                className="w-10 h-10 object-contain"
              />
            </Link>
            <h1 className="text-2xl font-display font-bold text-gray-900">
              Community Library
            </h1>
          </div>
          <p className="text-gray-600">
            Browse worksheets shared by teachers and parents
          </p>
        </div>
      </header>

      <main className="container mx-auto px-4 py-8 max-w-6xl">
        {/* Search */}
        <div className="mb-6">
          <input
            type="text"
            placeholder="Search by title, subtitle, or description..."
            value={searchQuery}
            onChange={(e) => {
              setSearchQuery(e.target.value);
              setOffset(0); // Reset pagination on new search
            }}
            className="w-full px-4 py-3 rounded-lg border border-gray-300 focus:ring-2 focus:ring-blue-500 focus:border-transparent outline-none transition"
          />
          {searchQuery && (
            <button
              onClick={() => {
                setSearchQuery("");
                setOffset(0);
              }}
              className="mt-2 text-sm text-blue-600 hover:text-blue-700 font-medium"
            >
              Clear search
            </button>
          )}
        </div>

        {/* Filters */}
        <Card className="mb-6 p-6">
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-bold text-gray-700 mb-3">
                Grade Levels
              </label>
              <div className="flex flex-wrap gap-2">
                {(["kindergarten", "grade1", "grade2"] as GradeLevel[]).map(
                  (grade) => (
                    <button
                      key={grade}
                      onClick={() => {
                        setSelectedGrades((prev) =>
                          prev.includes(grade)
                            ? prev.filter((g) => g !== grade)
                            : [...prev, grade],
                        );
                      }}
                      className={`px-4 py-2 rounded-lg font-medium transition-colors ${
                        selectedGrades.includes(grade)
                          ? "bg-blue-500 text-white"
                          : "bg-gray-200 text-gray-700 hover:bg-gray-300"
                      }`}
                    >
                      Grade {gradeLabels[grade]}
                    </button>
                  ),
                )}
              </div>
            </div>

            <div>
              <label className="block text-sm font-bold text-gray-700 mb-3">
                Sort By
              </label>
              <select
                value={sortBy}
                onChange={(e) =>
                  setSortBy(
                    e.target.value as
                      | "newest"
                      | "views"
                      | "downloads"
                      | "ratings",
                  )
                }
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent outline-none"
              >
                <option value="newest">Newest First</option>
                <option value="views">Most Viewed</option>
                <option value="downloads">Most Downloaded</option>
                <option value="ratings">Highest Rated</option>
              </select>
            </div>
          </div>
        </Card>

        {/* Content */}
        {error ? (
          <Card className="p-6 text-center">
            <div className="text-4xl mb-2">⚠️</div>
            <p className="text-gray-600">{error}</p>
          </Card>
        ) : loading ? (
          <Card className="p-6 text-center">
            <div className="text-6xl mb-4 animate-spin">⏳</div>
            <p className="text-gray-600">Loading worksheets...</p>
          </Card>
        ) : worksheets.length === 0 ? (
          <Card className="p-8 text-center">
            <div className="text-6xl mb-4">📚</div>
            <h2 className="text-2xl font-bold text-gray-900 mb-2">
              No worksheets found
            </h2>
            <p className="text-gray-600">
              Be the first to share a worksheet to the community!
            </p>
          </Card>
        ) : (
          <div>
            <div className="flex items-center justify-between mb-4">
              <div className="text-sm text-gray-600">
                Showing {offset + 1} - {offset + worksheets.length} of {total}{" "}
                results
              </div>
              <div className="text-sm text-gray-500">Limit: 20</div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
              {worksheets.map((ws) => {
                const operationColor = getOperationColor(
                  ws.singleOperation || "mixed"
                );
                return (
                  <div key={ws.id}>
                    <Link to={`/worksheets/${ws.id}`}>
                      <Card
                        className={`group p-4 h-full hover:shadow-lg transition-shadow cursor-pointer relative overflow-hidden border-2 ${operationColor.border}`}
                      >
                        <div className="absolute -top-8 -right-0 opacity-10 group-hover:opacity-15 transition-opacity pointer-events-none">
                          <div className="text-[270px] font-bold text-gray-400 leading-none">
                            {operationColor.symbol}
                          </div>
                        </div>
                        <div className="relative z-10">
                          <h3 className="font-bold text-lg text-gray-900 mb-2 line-clamp-2">
                            {ws.title}
                          </h3>

                          {ws.subtitle && (
                            <p className="text-sm text-gray-600 mb-3 line-clamp-2">
                              {ws.subtitle}
                            </p>
                          )}

                          <div className="space-y-2 mb-4 text-sm text-gray-700">
                            <div className="flex items-center gap-2">
                              <span>📝</span>
                              <span>{ws.problemCount} problems</span>
                            </div>
                            <div className="flex items-center gap-2">
                              <span>🎓</span>
                              <span>
                                {ws.grades
                                  .map((g) => gradeLabels[g])
                                  .join(", ")}
                              </span>
                            </div>
                            <div className="flex items-center gap-2 text-gray-500">
                              <span>👁️</span>
                              <span>{ws.stats.views} views</span>
                            </div>
                            <div className="mt-2">
                              <StarRating
                                rating={ws.stats.averageRating}
                                count={ws.stats.ratingCount}
                                onRate={async (stars) => {
                                  // Submit rating and update local state
                                  try {
                                    const res = await fetch(
                                      `/api/v1/worksheets/${ws.id}/rate`,
                                      {
                                        method: "POST",
                                        headers: {
                                          "Content-Type": "application/json",
                                        },
                                        body: JSON.stringify({
                                          rating: stars,
                                          sessionId,
                                        }),
                                      },
                                    );
                                    if (res.ok) {
                                      const data = await res.json();
                                      setWorksheets((prev) =>
                                        prev.map((p) =>
                                          p.id === ws.id
                                            ? {
                                                ...p,
                                                stats: {
                                                  ...p.stats,
                                                  averageRating:
                                                    data.stats.averageRating,
                                                  ratingCount:
                                                    data.stats.ratingCount,
                                                },
                                              }
                                            : p,
                                        ),
                                      );
                                      // If viewing detail, update worksheet state too
                                      if (worksheet && worksheet.id === ws.id) {
                                        setWorksheet((prev) =>
                                          prev
                                            ? {
                                                ...prev,
                                                stats: {
                                                  ...prev.stats,
                                                  averageRating:
                                                    data.stats.averageRating,
                                                  ratingCount:
                                                    data.stats.ratingCount,
                                                },
                                              }
                                            : prev,
                                        );
                                      }
                                    }
                                  } catch (error) {
                                    console.error(
                                      "Failed to submit rating:",
                                      error,
                                    );
                                  }
                                }}
                                size="sm"
                              />
                            </div>
                          </div>

                          <div className="text-xs text-gray-500">
                            {new Date(ws.createdAt).toLocaleDateString()}
                          </div>
                        </div>
                      </Card>
                    </Link>
                  </div>
                );
              })}
            </div>

            {/* Load More */}
            {hasMore && (
              <div className="mt-6 text-center">
                <Button
                  variant="primary"
                  onClick={() => setOffset((o) => o + 20)}
                  className="px-6"
                >
                  Load More
                </Button>
              </div>
            )}
          </div>
        )}
      </main>
    </div>
  );
}

function getOperationColor(operation: string): {
  bg: string;
  symbol: string;
  text: string;
  border: string;
} {
  switch (operation) {
    case "addition":
      return {
        bg: "from-blue-50 to-blue-100",
        symbol: "+",
        text: "text-green-400",
        border: "border-green-300",
      };
    case "subtraction":
      return {
        bg: "from-pink-50 to-pink-100",
        symbol: "-",
        text: "text-pink-400",
        border: "border-pink-300",
      };
    case "multiplication":
      return {
        bg: "from-green-50 to-green-100",
        symbol: "×",
        text: "text-blue-400",
        border: "border-blue-300",
      };
    case "division":
      return {
        bg: "from-amber-50 to-amber-100",
        symbol: "÷",
        text: "text-amber-400",
        border: "border-amber-300",
      };
    case "mixed":
      return {
        bg: "from-purple-50 to-indigo-50",
        symbol: "±",
        text: "text-purple-400",
        border: "border-purple-300",
      };
    default:
      return {
        bg: "from-gray-50 to-gray-100",
        symbol: "?",
        text: "text-gray-400",
        border: "border-gray-300",
      };
  }
}

function getSingleOperationType(
  problems: Array<{ operation: string }>,
): string | null {
  if (problems.length === 0) return null;

  const firstOperation = problems[0].operation;
  const allSame = problems.every((p) => p.operation === firstOperation);

  return allSame ? firstOperation : null;
}

function getOperationSymbol(operation: string): string {
  switch (operation) {
    case "addition":
      return "+";
    case "subtraction":
      return "-";
    case "multiplication":
      return "×";
    case "division":
      return "÷";
    default:
      return "?";
  }
}

function calculateAnswer(
  operand1: number,
  operand2: number,
  operation: string,
): number | string {
  switch (operation) {
    case "addition":
      return operand1 + operand2;
    case "subtraction":
      return operand1 - operand2;
    case "multiplication":
      return operand1 * operand2;
    case "division": {
      // Return with 2 decimal places if not a whole number, otherwise whole number
      const result = operand1 / operand2;
      return Number.isInteger(result) ? result : result.toFixed(2);
    }
    default:
      return "?";
  }
}
