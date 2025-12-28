import { useState } from "react";
import { AI_SAFETY_CONFIG } from "@/lib/server/aiSafety";

interface ValidationResult {
  safe: boolean;
  classification: "safe" | "unsafe";
  categories?: string[];
  explanation?: string;
  confidence?: number;
  usingAI?: boolean;
  fallback?: boolean;
}

export function SafetyValidator() {
  const [title, setTitle] = useState("");
  const [subtitle, setSubtitle] = useState("");
  const [selectedModel, setSelectedModel] = useState<string>(
    AI_SAFETY_CONFIG.DEFAULT_MODEL,
  );
  const [result, setResult] = useState<ValidationResult | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleValidate = async () => {
    if (!title.trim()) {
      setError("Title is required");
      return;
    }

    setLoading(true);
    setError(null);
    setResult(null);

    try {
      const response = await fetch("/api/v1/test/validate-content", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          title,
          subtitle,
          model: selectedModel,
        }),
      });

      if (!response.ok) {
        throw new Error(`HTTP ${response.status}`);
      }

      const data = await response.json();
      setResult(data);
    } catch (err) {
      setError(
        err instanceof Error
          ? err.message
          : "Failed to validate content. Please try again.",
      );
    } finally {
      setLoading(false);
    }
  };

  const allModels = [
    AI_SAFETY_CONFIG.DEFAULT_MODEL,
    ...AI_SAFETY_CONFIG.ALTERNATIVE_MODELS,
  ];

  return (
    <div className="min-h-screen bg-gradient-to-br from-orange-50 to-yellow-50 p-6">
      <div className="max-w-2xl mx-auto">
        {/* Header */}
        <div className="mb-8">
          <h1 className="text-4xl font-bold text-gray-800 mb-2">
            🛡️ Safety Content Validator
          </h1>
          <p className="text-gray-600">
            Test content filtering and choose the best AI model for safety
            classification
          </p>
        </div>

        {/* Input Section */}
        <div className="bg-white rounded-lg shadow-lg p-6 mb-6">
          <div className="space-y-4">
            {/* Title Input */}
            <div>
              <label className="block text-sm font-semibold text-gray-700 mb-2">
                Title <span className="text-red-500">*</span>
              </label>
              <input
                type="text"
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                placeholder="Enter worksheet title..."
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent outline-none transition"
                disabled={loading}
              />
            </div>

            {/* Subtitle Input */}
            <div>
              <label className="block text-sm font-semibold text-gray-700 mb-2">
                Subtitle{" "}
                <span className="text-gray-400 text-xs">(optional)</span>
              </label>
              <input
                type="text"
                value={subtitle}
                onChange={(e) => setSubtitle(e.target.value)}
                placeholder="Enter worksheet subtitle..."
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent outline-none transition"
                disabled={loading}
              />
            </div>

            {/* Model Selector */}
            <div>
              <label className="block text-sm font-semibold text-gray-700 mb-2">
                AI Model
              </label>
              <select
                value={selectedModel}
                onChange={(e) => setSelectedModel(e.target.value)}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent outline-none transition bg-white"
                disabled={loading}
              >
                {allModels.map((model) => (
                  <option key={model} value={model}>
                    {model === AI_SAFETY_CONFIG.DEFAULT_MODEL
                      ? `${model} (Recommended - Safety Specialized)`
                      : `${model} (Alternative)`}
                  </option>
                ))}
              </select>
              <p className="text-xs text-gray-500 mt-2">
                Llama Guard 3 is specifically designed for content safety. Other
                models available for testing/comparison.
              </p>
            </div>

            {/* Error Message */}
            {error && (
              <div className="bg-red-50 border border-red-200 rounded-lg p-4">
                <p className="text-red-700 text-sm">
                  <span className="font-semibold">Error:</span> {error}
                </p>
              </div>
            )}

            {/* Validate Button */}
            <button
              onClick={handleValidate}
              disabled={loading || !title.trim()}
              className="w-full bg-orange-500 hover:bg-orange-600 disabled:bg-gray-400 text-white font-semibold py-3 rounded-lg transition duration-200 transform hover:scale-105 disabled:hover:scale-100"
            >
              {loading ? "Validating..." : "Validate Content"}
            </button>
          </div>
        </div>

        {/* Result Section */}
        {result && (
          <div
            className={`rounded-lg shadow-lg p-6 ${
              result.safe
                ? "bg-green-50 border-2 border-green-500"
                : "bg-red-50 border-2 border-red-500"
            }`}
          >
            <div className="space-y-4">
              {/* Status Badge */}
              <div className="flex items-center gap-3">
                <div className="text-3xl">{result.safe ? "✅" : "⚠️"}</div>
                <div>
                  <h3 className="text-lg font-bold text-gray-800">
                    {result.safe ? "Content Approved" : "Content Flagged"}
                  </h3>
                  <p className="text-sm text-gray-600">
                    Classification:{" "}
                    <span className="font-semibold">
                      {result.classification}
                    </span>
                  </p>
                </div>
              </div>

              {/* Metadata */}
              <div className="grid grid-cols-2 gap-4">
                <div className="bg-white rounded p-3">
                  <p className="text-xs text-gray-600">Detection Method</p>
                  <p className="font-semibold text-gray-800">
                    {result.usingAI ? "🤖 AI-Based" : "📚 Pattern-Based"}
                  </p>
                </div>
                <div className="bg-white rounded p-3">
                  <p className="text-xs text-gray-600">Confidence</p>
                  <p className="font-semibold text-gray-800">
                    {(result.confidence || 0).toFixed(0)}%
                  </p>
                </div>
                {result.fallback && (
                  <div className="col-span-2 bg-yellow-100 rounded p-3">
                    <p className="text-xs text-yellow-800">
                      ℹ️ Using fallback pattern matching (AI unavailable)
                    </p>
                  </div>
                )}
              </div>

              {/* Categories */}
              {result.categories && result.categories.length > 0 && (
                <div className="bg-white rounded p-3">
                  <p className="text-xs text-gray-600 mb-2">Detected Issues</p>
                  <div className="flex flex-wrap gap-2">
                    {result.categories.map((category) => (
                      <span
                        key={category}
                        className="bg-red-200 text-red-800 text-xs px-3 py-1 rounded-full font-semibold"
                      >
                        {category}
                      </span>
                    ))}
                  </div>
                </div>
              )}

              {/* Explanation */}
              {result.explanation && (
                <div className="bg-white rounded p-3">
                  <p className="text-xs text-gray-600 mb-2">Explanation</p>
                  <p className="text-sm text-gray-700 italic">
                    &quot;{result.explanation}&quot;
                  </p>
                </div>
              )}

              {/* Test Input Echo */}
              <div className="bg-white rounded p-3 text-xs text-gray-600 space-y-1">
                <p>
                  <span className="font-semibold">Title:</span> {title}
                </p>
                {subtitle && (
                  <p>
                    <span className="font-semibold">Subtitle:</span> {subtitle}
                  </p>
                )}
                <p>
                  <span className="font-semibold">Model:</span>{" "}
                  {selectedModel.split("/").pop()}
                </p>
              </div>
            </div>
          </div>
        )}

        {/* Empty State */}
        {!result && !loading && (
          <div className="bg-white rounded-lg shadow-lg p-12 text-center">
            <p className="text-gray-500 text-lg">
              Enter content above and click &quot;Validate Content&quot; to test
              the safety classifier
            </p>
          </div>
        )}
      </div>
    </div>
  );
}
