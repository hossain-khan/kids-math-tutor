import { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import Button from '@/components/Button';
import Card from '@/components/Card';
import { generateDeeplink, isLikelyAndroidDevice } from '@/lib/deeplink';
import type { GradeLevel } from '@/lib/schemas/challenge-schema';

interface SharedWorksheet {
  id: string;
  type: 'explicit';
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
  };
}

interface WorksheetListItem {
  id: string;
  title: string;
  subtitle?: string;
  grades: GradeLevel[];
  problemCount: number;
  createdAt: string;
  stats: {
    views: number;
    downloads: number;
  };
}

const gradeLabels: Record<GradeLevel, string> = {
  kindergarten: 'K',
  grade1: '1st',
  grade2: '2nd',
};

export default function SharedWorksheets() {
  const { id } = useParams<{ id?: string }>();
  const [worksheet, setWorksheet] = useState<SharedWorksheet | null>(null);
  const [worksheets, setWorksheets] = useState<WorksheetListItem[]>([]);
  const [selectedGrades, setSelectedGrades] = useState<GradeLevel[]>([
    'kindergarten',
    'grade1',
    'grade2',
  ]);
  const [sortBy, setSortBy] = useState<'newest' | 'views' | 'downloads'>(
    'newest',
  );
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [isAndroid, setIsAndroid] = useState(false);
  const [usingWorksheet, setUsingWorksheet] = useState(false);

  // Check if running on Android
  useEffect(() => {
    setIsAndroid(isLikelyAndroidDevice());
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
            setError('Worksheet not found');
          } else {
            setError('Failed to load worksheet');
          }
          return;
        }

        const data = await response.json();
        setWorksheet(data);
      } catch (err) {
        console.error('Failed to fetch worksheet:', err);
        setError('Failed to load worksheet');
      } finally {
        setLoading(false);
      }
    };

    fetchWorksheet();
  }, [id]);

  // Load worksheets list
  useEffect(() => {
    if (id) return; // Don't load list if viewing single worksheet

    const fetchWorksheets = async () => {
      setLoading(true);
      setError(null);

      try {
        const params = new URLSearchParams();
        if (selectedGrades.length > 0) {
          params.append('grades', selectedGrades.join(','));
        }
        params.append('sort', sortBy);

        const response = await fetch(`/api/v1/worksheets?${params}`);

        if (!response.ok) {
          setError('Failed to load worksheets');
          return;
        }

        const data = await response.json();
        setWorksheets(data);
      } catch (err) {
        console.error('Failed to fetch worksheets:', err);
        setError('Failed to load worksheets');
      } finally {
        setLoading(false);
      }
    };

    fetchWorksheets();
  }, [id, selectedGrades, sortBy]);

  const handleUseWorksheet = async () => {
    if (!worksheet) return;

    setUsingWorksheet(true);

    try {
      // Track download
      await fetch(`/api/v1/worksheets/${worksheet.id}/download`, {
        method: 'POST',
      });

      // Generate deeplink with the worksheet data
      const deeplink = generateDeeplink({
        type: 'explicit',
        title: worksheet.title,
        subtitle: worksheet.subtitle,
        problems: worksheet.problems,
      });

      if (deeplink) {
        window.location.href = deeplink;
      }
    } catch (err) {
      console.error('Failed to use worksheet:', err);
    } finally {
      setUsingWorksheet(false);
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
        <div className="min-h-screen bg-gray-50">
          <header className="bg-white shadow-sm border-b border-gray-200">
            <div className="container mx-auto px-4 py-4">
              <Link to="/" className="inline-flex items-center gap-2 text-blue-600 hover:text-blue-700">
                <span>←</span>
                <span>Back to Home</span>
              </Link>
            </div>
          </header>
          <main className="container mx-auto px-4 py-8 max-w-2xl">
            <Card className="p-8 text-center">
              <div className="text-6xl mb-4">😕</div>
              <h1 className="text-2xl font-bold text-gray-900 mb-2">
                {error || 'Worksheet not found'}
              </h1>
              <p className="text-gray-600 mb-6">
                The worksheet you&apos;re looking for doesn&apos;t exist or has been
                removed.
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

    return (
      <div className="min-h-screen bg-gray-50">
        <header className="bg-white shadow-sm border-b border-gray-200">
          <div className="container mx-auto px-4 py-4">
            <Link to="/worksheets" className="inline-flex items-center gap-2 text-blue-600 hover:text-blue-700 mb-2">
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
          <Card className="mb-6 bg-gradient-to-br from-blue-50 to-indigo-50 border-2 border-blue-300 p-6">
            <div>
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
                    Grades:{' '}
                    <strong>
                      {worksheet.grades.map((g) => gradeLabels[g]).join(', ')}
                    </strong>
                  </span>
                </div>
              </div>
            </div>
          </Card>

          {/* Preview Problems */}
          <Card className="mb-6 p-6">
            <h3 className="font-display font-bold text-lg mb-4">Preview</h3>
            <div className="space-y-3">
              {previewProblems.map((problem, idx) => (
                <div
                  key={idx}
                  className="bg-gray-50 p-4 rounded border border-gray-200"
                >
                  <div className="text-lg font-mono">
                    {problem.operand1} {getOperationSymbol(problem.operation)}{' '}
                    {problem.operand2} = ?
                  </div>
                </div>
              ))}
              {problemCount > 3 && (
                <p className="text-sm text-gray-600 italic pt-2">
                  ... and {problemCount - 3} more problems
                </p>
              )}
            </div>
          </Card>

          {/* Action Buttons */}
          <div className="flex gap-3 flex-col sm:flex-row">
            {isAndroid && (
              <Button
                variant="primary"
                size="lg"
                onClick={handleUseWorksheet}
                disabled={usingWorksheet}
                className="flex-1 bg-gradient-to-r from-green-500 to-emerald-500 hover:from-green-600 hover:to-emerald-600"
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
            )}
            <Link to="/worksheets" className="flex-1">
              <Button variant="secondary" size="lg" className="w-full">
                <span aria-hidden="true" className="mr-2">
                  📚
                </span>
                Browse More
              </Button>
            </Link>
          </div>
        </main>
      </div>
    );
  }

  // Worksheets list view
  return (
    <div className="min-h-screen bg-gray-50">
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
        {/* Filters */}
        <Card className="mb-6 p-6">
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-bold text-gray-700 mb-3">
                Grade Levels
              </label>
              <div className="flex flex-wrap gap-2">
                {(['kindergarten', 'grade1', 'grade2'] as GradeLevel[]).map(
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
                          ? 'bg-blue-500 text-white'
                          : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
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
                  setSortBy(e.target.value as 'newest' | 'views' | 'downloads')
                }
                className="px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              >
                <option value="newest">Newest First</option>
                <option value="views">Most Viewed</option>
                <option value="downloads">Most Downloaded</option>
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
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {worksheets.map((ws) => (
              <Link key={ws.id} to={`/worksheets/${ws.id}`}>
                <Card className="p-4 h-full hover:shadow-lg transition-shadow hover:border-blue-300 cursor-pointer">
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
                        {ws.grades.map((g) => gradeLabels[g]).join(', ')}
                      </span>
                    </div>
                    <div className="flex items-center gap-2 text-gray-500">
                      <span>👁️</span>
                      <span>{ws.stats.views} views</span>
                    </div>
                  </div>

                  <div className="text-xs text-gray-500">
                    {new Date(ws.createdAt).toLocaleDateString()}
                  </div>
                </Card>
              </Link>
            ))}
          </div>
        )}
      </main>
    </div>
  );
}

function getOperationSymbol(operation: string): string {
  switch (operation) {
    case 'addition':
      return '+';
    case 'subtraction':
      return '-';
    case 'multiplication':
      return '×';
    case 'division':
      return '÷';
    default:
      return '?';
  }
}
