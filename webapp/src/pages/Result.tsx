import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { Light as SyntaxHighlighter } from "react-syntax-highlighter";
import json from "react-syntax-highlighter/dist/esm/languages/hljs/json";
import { githubGist } from "react-syntax-highlighter/dist/esm/styles/hljs";
import Button from "@/components/Button";
import Card from "@/components/Card";
import { type ChallengeImportSpec } from "@/lib/schemas/challenge-schema";
import { copyToClipboard, downloadJson } from "@/lib/utils";

// Register JSON language
SyntaxHighlighter.registerLanguage("json", json);

export default function Result() {
  const navigate = useNavigate();
  const [challengeData, setChallengeData] =
    useState<ChallengeImportSpec | null>(null);
  const [copied, setCopied] = useState(false);
  const [downloadSuccess, setDownloadSuccess] = useState(false);

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
  }, [navigate]);

  const handleCopy = async () => {
    if (!challengeData) return;

    const json = JSON.stringify(challengeData, null, 2);
    const success = await copyToClipboard(json);

    if (success) {
      setCopied(true);
      setTimeout(() => setCopied(false), 3000);
    }
  };

  const handleDownload = () => {
    if (!challengeData) return;

    const filename = `${challengeData.title.toLowerCase().replace(/\s+/g, "-")}-worksheet.json`;
    downloadJson(challengeData, filename);
    setDownloadSuccess(true);
    setTimeout(() => setDownloadSuccess(false), 3000);
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
                Paste the code and tap <strong>&quot;Save Challenge&quot;</strong>
              </span>
            </li>
          </ol>
        </Card>

        {/* Action Buttons */}
        <div className="flex gap-3 mb-6">
          <Button
            variant="primary"
            size="lg"
            onClick={handleCopy}
            className="flex-1"
          >
            {copied ? (
              <>
                <span className="mr-2">✅</span>
                Copied!
              </>
            ) : (
              <>
                <span className="mr-2">📋</span>
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
