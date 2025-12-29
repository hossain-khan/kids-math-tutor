import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { Light as SyntaxHighlighter } from "react-syntax-highlighter";
import json from "react-syntax-highlighter/dist/esm/languages/hljs/json";
import { githubGist } from "react-syntax-highlighter/dist/esm/styles/hljs";
import Button from "@/components/Button";
import Card from "@/components/Card";
import { type ChallengeImportSpec } from "@/lib/schemas/challenge-schema";
import { copyToClipboard, downloadJson } from "@/lib/utils";
import { generateDeeplink, isLikelyAndroidDevice } from "@/lib/deeplink";
import { getOrCreateSessionId } from "@/lib/sessionId";

// Register JSON language
SyntaxHighlighter.registerLanguage("json", json);

interface ShareErrorData {
  error: string;
  categories?: string[];
  explanation?: string;
  suggestion?: string;
  method?: string;
}

export default function Result() {
  const navigate = useNavigate();
  const [challengeData, setChallengeData] =
    useState<ChallengeImportSpec | null>(null);
  const [copied, setCopied] = useState(false);
  const [downloadSuccess, setDownloadSuccess] = useState(false);
  const [isAndroid, setIsAndroid] = useState(false);
  const [deeplinkOpened, setDeeplinkOpened] = useState(false);
  const [shareLoading, setShareLoading] = useState(false);
  const [shareError, setShareError] = useState<ShareErrorData | null>(null);
  const [shareSuccess, setShareSuccess] = useState(false);
  const [shareLink, setShareLink] = useState<string | null>(null);
  const [sharedWorksheetId, setSharedWorksheetId] = useState<string | null>(
    null,
  );
  const [deleteLoading, setDeleteLoading] = useState(false);

  // Cleanup copied state after 3 seconds
  useEffect(() => {
    if (!copied) return;
    const timeout = setTimeout(() => setCopied(false), 3000);
    return () => clearTimeout(timeout);
  }, [copied]);

  // Cleanup download success state after 3 seconds
  useEffect(() => {
    if (!downloadSuccess) return;
    const timeout = setTimeout(() => setDownloadSuccess(false), 3000);
    return () => clearTimeout(timeout);
  }, [downloadSuccess]);

  // Cleanup deeplink opened state after 3 seconds
  useEffect(() => {
    if (!deeplinkOpened) return;
    const timeout = setTimeout(() => setDeeplinkOpened(false), 3000);
    return () => clearTimeout(timeout);
  }, [deeplinkOpened]);

  // Share states are no longer auto-cleaned - they persist until user navigates away

  useEffect(() => {
    if (!shareError) return;
    const timeout = setTimeout(() => setShareError(null), 5000);
    return () => clearTimeout(timeout);
  }, [shareError]);

  useEffect(() => {
    const data = sessionStorage.getItem("challengeData");
    if (!data) {
      navigate("/");
      return;
    }

    try {
      const parsed = JSON.parse(data);
      setChallengeData(parsed);
    } catch (error) {
      console.error("Failed to parse challenge data:", error);
      navigate("/");
    }

    // Check if running on Android
    setIsAndroid(isLikelyAndroidDevice());
  }, [navigate]);

  const handleCopy = async () => {
    if (!challengeData) return;

    const json = JSON.stringify(challengeData, null, 2);
    const success = await copyToClipboard(json);

    if (success) {
      setCopied(true);
    }
  };

  const handleOpenInApp = () => {
    if (!challengeData) return;

    const deeplink = generateDeeplink(challengeData);
    if (deeplink) {
      setDeeplinkOpened(true);
      // Open the deeplink - this will only work if Math Pup app is installed
      window.location.href = deeplink;
    }
  };

  const handleDownload = () => {
    if (!challengeData) return;

    const filename = `${challengeData.title.toLowerCase().replace(/\s+/g, "-")}-worksheet.json`;
    downloadJson(challengeData, filename);
    setDownloadSuccess(true);
  };

  const handleShareToCommunity = async () => {
    if (!challengeData || challengeData.type !== "explicit") return;

    setShareLoading(true);
    setShareError(null);

    try {
      const sessionId = getOrCreateSessionId();
      
      const response = await fetch("/api/v1/worksheets/share", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          ...challengeData,
          sessionId, // Include session ID for ownership tracking
        }),
      });

      const data = await response.json();

      if (!response.ok) {
        setShareError(data as ShareErrorData);
        return;
      }

      setShareSuccess(true);
      setShareLink(data.shareLink);
      setSharedWorksheetId(data.id); // Store the worksheet ID for potential deletion
    } catch (error) {
      console.error("Share error:", error);
      setShareError({
        error: "Network error. Please check your connection and try again.",
      });
    } finally {
      setShareLoading(false);
    }
  };

  const handleUndoShare = async () => {
    if (!sharedWorksheetId) return;

    // Show confirmation dialog
    const confirmed = window.confirm(
      "Are you sure you want to remove this worksheet from the community library? This action cannot be undone.",
    );

    if (!confirmed) return;

    setDeleteLoading(true);

    try {
      const sessionId = getOrCreateSessionId();

      const response = await fetch(`/api/v1/worksheets/${sharedWorksheetId}`, {
        method: "DELETE",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ sessionId }),
      });

      if (!response.ok) {
        const data = await response.json();
        alert(`Failed to delete: ${data.error}`);
        return;
      }

      // Reset share states
      setShareSuccess(false);
      setShareLink(null);
      setSharedWorksheetId(null);
      alert("Worksheet successfully removed from community library.");
    } catch (error) {
      console.error("Delete error:", error);
      alert("Network error. Please check your connection and try again.");
    } finally {
      setDeleteLoading(false);
    }
  };

  const handleCreateAnother = () => {
    sessionStorage.removeItem("challengeData");
    navigate("/");
  };

  if (!challengeData) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <div className="text-6xl mb-4 animate-spin">⏳</div>
          <p className="text-gray-600">Loading...</p>
        </div>
      </div>
    );
  }

  const json = JSON.stringify(challengeData, null, 2);
  const problemCount =
    challengeData.type === "generated"
      ? challengeData.problemCount
      : challengeData.problems.length;

  return (
    <div className="min-h-screen">
      {/* Header */}
      <header className="bg-white shadow-sm border-b border-gray-200">
        <div className="container mx-auto px-4 py-4">
          <div className="flex items-center gap-3">
            <Link to="/" className="hover:scale-110 transition-transform">
              <img
                src="/images/logo.webp"
                alt="Math Pup Logo"
                className="w-10 h-10 object-contain"
              />
            </Link>
            <h1 className="text-2xl font-display font-bold text-gray-900">
              Worksheet Ready!
            </h1>
          </div>
        </div>
      </header>

      <main className="container mx-auto px-4 py-8 max-w-4xl">
        {/* Success Card */}
        <Card className="mb-6 bg-gradient-to-br from-green-50 to-emerald-50 border-2 border-green-300 p-6">
          <div className="text-center">
            <div className="text-6xl mb-4 animate-bounce">🎉</div>
            <h2 className="text-3xl font-display font-bold text-gray-900 mb-2">
              {challengeData.title}
            </h2>
            {challengeData.subtitle && (
              <p className="text-lg text-gray-600 mb-4">
                {challengeData.subtitle}
              </p>
            )}
            <div className="flex items-center justify-center gap-6 text-sm text-gray-700">
              <div className="flex items-center gap-2">
                <span className="font-bold">{problemCount}</span>
                <span>problems</span>
              </div>
              <div className="w-1 h-1 rounded-full bg-gray-400" />
              <div className="flex items-center gap-2">
                <span className="font-bold capitalize">
                  {challengeData.type === "generated"
                    ? challengeData.operation
                    : "Mixed"}
                </span>
                <span>operation</span>
              </div>
            </div>
          </div>
        </Card>

        {/* How to Use Card */}
        <Card className="mb-6 bg-blue-50 border-blue-200 p-6">
          <h3 className="font-display font-bold text-lg mb-3 flex items-center gap-2">
            <span>📱</span>
            <span>How to Import to App</span>
          </h3>
          <ol className="space-y-2 text-sm text-gray-700">
            <li className="flex gap-3">
              <span className="flex-shrink-0 w-6 h-6 rounded-full bg-primary-500 text-white flex items-center justify-center text-xs font-bold">
                1
              </span>
              <span>Copy the code below (or download the JSON file)</span>
            </li>
            <li className="flex gap-3">
              <span className="flex-shrink-0 w-6 h-6 rounded-full bg-primary-500 text-white flex items-center justify-center text-xs font-bold">
                2
              </span>
              <span>
                Open <strong>Kids Math Pup Tutor</strong> app on your Android
                device
              </span>
            </li>
            <li className="flex gap-3">
              <span className="flex-shrink-0 w-6 h-6 rounded-full bg-primary-500 text-white flex items-center justify-center text-xs font-bold">
                3
              </span>
              <span>
                Go to <strong>Settings → Parent Challenges → Import</strong>
              </span>
            </li>
            <li className="flex gap-3">
              <span className="flex-shrink-0 w-6 h-6 rounded-full bg-primary-500 text-white flex items-center justify-center text-xs font-bold">
                4
              </span>
              <span>
                Paste the code and tap{" "}
                <strong>&quot;Save Challenge&quot;</strong>
              </span>
            </li>
          </ol>
        </Card>

        {/* Action Buttons */}
        <div className="flex gap-3 mb-6 flex-col sm:flex-row">
          {isAndroid && (
            <Button
              variant="primary"
              size="lg"
              onClick={handleOpenInApp}
              className="flex-1 bg-gradient-to-r from-purple-500 to-pink-500 hover:from-purple-600 hover:to-pink-600"
            >
              {deeplinkOpened ? (
                <>
                  <span aria-hidden="true" className="mr-2">
                    📱
                  </span>
                  Opening Math Pup...
                </>
              ) : (
                <>
                  <span aria-hidden="true" className="mr-2">
                    🚀
                  </span>
                  Open in Math Pup App
                </>
              )}
            </Button>
          )}
          {challengeData.type === "explicit" && (
            <Button
              variant="primary"
              size="lg"
              onClick={handleShareToCommunity}
              disabled={shareLoading || shareSuccess}
              className="flex-1 bg-gradient-to-r from-indigo-500 to-blue-500 hover:from-indigo-600 hover:to-blue-600"
            >
              {shareLoading ? (
                <>
                  <span aria-hidden="true" className="mr-2 animate-spin">
                    ⏳
                  </span>
                  Sharing...
                </>
              ) : shareSuccess ? (
                <>
                  <span aria-hidden="true" className="mr-2">
                    🎉
                  </span>
                  Shared!
                </>
              ) : (
                <>
                  <span aria-hidden="true" className="mr-2">
                    🌍
                  </span>
                  Share to Community
                </>
              )}
            </Button>
          )}
          <Button
            variant="primary"
            size="lg"
            onClick={handleCopy}
            className="flex-1"
          >
            {copied ? (
              <>
                <span aria-hidden="true" className="mr-2">
                  ✅
                </span>
                Copied!
              </>
            ) : (
              <>
                <span aria-hidden="true" className="mr-2">
                  📋
                </span>
                Copy Code
              </>
            )}
          </Button>
          <Button
            variant="secondary"
            size="lg"
            onClick={handleDownload}
            className="flex-1"
          >
            {downloadSuccess ? (
              <>
                <span className="mr-2">✅</span>
                Downloaded!
              </>
            ) : (
              <>
                <span className="mr-2">💾</span>
                Download JSON
              </>
            )}
          </Button>
        </div>

        {/* Share Success Message */}
        {shareSuccess && shareLink && (
          <Card className="mb-6 bg-green-50 border-green-300 p-4">
            <div className="flex items-start gap-3">
              <span className="text-2xl flex-shrink-0">✨</span>
              <div className="flex-1">
                <h3 className="font-bold text-green-900 mb-1">
                  Shared to Community!
                </h3>
                <p className="text-sm text-green-800 mb-3">
                  Your worksheet has been shared to the community library.
                </p>
                <div className="flex gap-2 items-center mb-3">
                  <div
                    className="flex-1 bg-white rounded p-3 text-xs font-mono text-gray-700 break-all cursor-pointer hover:bg-gray-50 transition-colors"
                    onClick={() => copyToClipboard(shareLink)}
                  >
                    {shareLink}
                  </div>
                  <button
                    onClick={() => copyToClipboard(shareLink)}
                    className="flex-shrink-0 bg-green-600 hover:bg-green-700 text-white px-3 py-2 rounded text-xs font-medium transition-colors whitespace-nowrap"
                  >
                    {copied ? "✅ Copied" : "📋 Copy"}
                  </button>
                </div>
                {/* Undo Share Button */}
                <button
                  onClick={handleUndoShare}
                  disabled={deleteLoading}
                  className="w-full bg-red-50 hover:bg-red-100 border-2 border-red-300 text-red-800 px-4 py-2 rounded-lg font-medium transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {deleteLoading ? (
                    <>
                      <span className="mr-2 animate-spin">⏳</span>
                      Removing...
                    </>
                  ) : (
                    <>
                      <span className="mr-2">🗑️</span>
                      Undo Share - Remove from Community
                    </>
                  )}
                </button>
              </div>
            </div>
          </Card>
        )}

        {/* Share Error Message */}
        {shareError && (
          <Card className="mb-6 bg-red-50 border-red-300 p-4">
            <div className="flex items-start gap-3">
              <span className="text-2xl flex-shrink-0">⚠️</span>
              <div className="flex-1">
                <h3 className="font-bold text-red-900 mb-2">
                  {shareError.error}
                </h3>

                {/* Show categories if available */}
                {shareError.categories && shareError.categories.length > 0 && (
                  <div className="mb-3">
                    <p className="text-xs font-semibold text-red-800 mb-1.5">
                      Issues found:
                    </p>
                    <div className="flex flex-wrap gap-1.5">
                      {shareError.categories.map((category, idx) => (
                        <span
                          key={idx}
                          className="inline-block bg-red-200 text-red-900 px-2 py-1 rounded text-xs font-medium capitalize"
                        >
                          {category}
                        </span>
                      ))}
                    </div>
                  </div>
                )}

                {/* Show explanation if available */}
                {shareError.explanation && (
                  <div className="mb-3 p-2 bg-red-100 rounded border border-red-200">
                    <p className="text-xs font-semibold text-red-800 mb-1">
                      Why this happened:
                    </p>
                    <p className="text-sm text-red-800">
                      {shareError.explanation}
                    </p>
                  </div>
                )}

                {/* Show suggestion if available */}
                {shareError.suggestion && (
                  <div className="mb-2 p-2 bg-blue-50 rounded border border-blue-200">
                    <p className="text-xs font-semibold text-blue-900 mb-1">
                      How to fix:
                    </p>
                    <p className="text-sm text-blue-900">
                      {shareError.suggestion}
                    </p>
                  </div>
                )}

                {/* Show validation method if available */}
                {shareError.method && (
                  <p className="text-xs text-red-700 mt-2">
                    <span className="font-semibold">Validation method:</span>{" "}
                    {shareError.method}
                  </p>
                )}
              </div>
            </div>
          </Card>
        )}

        {/* JSON Preview */}
        <Card className="p-6">
          <div className="flex items-center justify-between mb-4">
            <h3 className="font-display font-bold text-lg">Worksheet Code</h3>
            <span className="text-sm text-gray-500">JSON Format</span>
          </div>
          <div className="relative">
            <SyntaxHighlighter
              language="json"
              style={githubGist}
              customStyle={{
                borderRadius: "0.5rem",
                padding: "1rem",
                maxHeight: "24rem",
                fontSize: "0.875rem",
              }}
              showLineNumbers={false}
            >
              {json}
            </SyntaxHighlighter>
            <button
              onClick={handleCopy}
              className="absolute top-2 right-2 bg-gray-800 hover:bg-gray-700 text-white px-3 py-1 rounded text-xs transition-colors"
            >
              {copied ? "✅ Copied" : "📋 Copy"}
            </button>
          </div>
        </Card>

        {/* Footer Actions */}
        <div className="flex flex-col sm:flex-row gap-4 mt-8">
          <Button
            variant="primary"
            size="lg"
            onClick={handleCreateAnother}
            className="flex-1"
          >
            Create Another Worksheet
          </Button>
          <Link to="/" className="flex-1">
            <Button variant="outline" size="lg" className="w-full">
              Back to Home
            </Button>
          </Link>
        </div>
      </main>
    </div>
  );
}
