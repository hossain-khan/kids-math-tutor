import Card from "@/components/Card";
import { AdminWorksheet } from "@/lib/reducers/adminReducer";

interface WorksheetCardProps {
  worksheet: AdminWorksheet;
  onDelete: (id: string) => void;
  onConfirmDelete: (id: string) => Promise<void>;
  deleteConfirm: string | null;
  deleting: string | null;
  expandedSafety: string | null;
  onToggleExpandedSafety: (id: string | null) => void;
}

export default function WorksheetCard({
  worksheet,
  onDelete,
  onConfirmDelete,
  deleteConfirm,
  deleting,
  expandedSafety,
  onToggleExpandedSafety,
}: WorksheetCardProps) {
  return (
    <div>
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
                  {worksheet.safety.isFlagged ? "⚠️ Flagged" : "✅ Safe"}
                </span>
              )}
            </div>
            {worksheet.subtitle && (
              <p className="text-gray-600 mb-2 break-words">
                {worksheet.subtitle}
              </p>
            )}
            <div className="flex flex-wrap gap-4 text-sm text-gray-600 mb-3">
              <span>📊 {worksheet.problemCount} problems</span>
              <span>👁️ {worksheet.stats.views} views</span>
              <span>💾 {worksheet.stats.downloads} downloads</span>
              <span>⭐ {worksheet.stats.averageRating.toFixed(1)} rating</span>
              <span>
                📅 {new Date(worksheet.createdAt).toLocaleDateString()}
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
                  onToggleExpandedSafety(
                    expandedSafety === worksheet.id ? null : worksheet.id,
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
                  onClick={() => onConfirmDelete(worksheet.id)}
                  disabled={deleting === worksheet.id}
                  className="px-3 py-1 text-xs font-medium bg-red-600 text-white rounded hover:bg-red-700 disabled:opacity-50"
                >
                  {deleting === worksheet.id ? "Deleting..." : "Confirm"}
                </button>
                <button
                  onClick={() => onDelete("")}
                  disabled={deleting === worksheet.id}
                  className="px-3 py-1 text-xs font-medium bg-gray-300 text-gray-900 rounded hover:bg-gray-400 disabled:opacity-50"
                >
                  Cancel
                </button>
              </div>
            ) : (
              <button
                onClick={() => onDelete(worksheet.id)}
                className="px-3 py-1 text-xs font-medium bg-red-50 text-red-600 rounded hover:bg-red-100 transition-colors"
              >
                Delete
              </button>
            )}
          </div>
        </div>
      </Card>

      {expandedSafety === worksheet.id && worksheet.safety?.isFlagged && (
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
                  {new Date(worksheet.safety.lastChecked).toLocaleString()}
                </p>
              )}
            </div>
          </div>
        </Card>
      )}
    </div>
  );
}
