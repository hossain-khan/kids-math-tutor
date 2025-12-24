import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import Button from "@/components/Button";
import Input from "@/components/Input";
import Select from "@/components/Select";
import Card from "@/components/Card";
import TemplateSection from "@/components/TemplateSection";
import Toast from "@/components/Toast";
import {
  GeneratedChallengeSpecSchema,
  type MathOperation,
} from "@/lib/schemas/challenge-schema";
import {
  generatedTemplates,
  type GeneratedTemplate,
  type ExplicitTemplate,
} from "@/lib/templates";
import { z } from "zod";

export default function GeneratedBuilder() {
  const navigate = useNavigate();
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [showToast, setShowToast] = useState(false);
  const [toastMessage, setToastMessage] = useState("");
  const [isTemplateExpanded, setIsTemplateExpanded] = useState(false);
  const [formData, setFormData] = useState({
    title: "",
    subtitle: "",
    operation: "addition" as MathOperation,
    problemCount: 10,
    minNumber: 0,
    maxNumber: 20,
  });

  const operationOptions = [
    { value: "addition", label: "➕ Addition" },
    { value: "subtraction", label: "➖ Subtraction" },
    { value: "multiplication", label: "✖️ Multiplication" },
    { value: "division", label: "➗ Division" },
  ];

  const handleTemplateSelect = (
    template: GeneratedTemplate | ExplicitTemplate,
  ) => {
    // Type guard to ensure we have a GeneratedTemplate
    if (!("numberRange" in template.config)) {
      return;
    }
    const config = template.config;
    setFormData({
      title: config.title,
      subtitle: config.subtitle,
      operation: config.operation,
      problemCount: config.problemCount,
      minNumber: config.numberRange.min,
      maxNumber: config.numberRange.max,
    });

    // Show toast notification
    setToastMessage(`✨ "${template.name}" template applied!`);
    setShowToast(true);

    // Scroll to form
    setTimeout(() => {
      document
        .querySelector("form")
        ?.scrollIntoView({ behavior: "smooth", block: "start" });
    }, 100);
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setErrors({});

    try {
      // Validate the form data
      const challengeData = {
        type: "generated" as const,
        title: formData.title,
        subtitle: formData.subtitle || undefined,
        operation: formData.operation,
        problemCount: formData.problemCount,
        numberRange: {
          min: formData.minNumber,
          max: formData.maxNumber,
        },
      };

      GeneratedChallengeSpecSchema.parse(challengeData);

      // Store the data and navigate to result page
      sessionStorage.setItem("challengeData", JSON.stringify(challengeData));
      navigate("/result");
    } catch (error) {
      if (error instanceof z.ZodError) {
        const newErrors: Record<string, string> = {};
        error.errors.forEach((err) => {
          const path = err.path.join(".");
          newErrors[path] = err.message;
        });
        setErrors(newErrors);
      }
    }
  };

  return (
    <div className="min-h-screen">
      {/* Header */}
      <header className="bg-white shadow-sm border-b border-gray-200">
        <div className="container mx-auto px-4 py-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-3">
              <Link to="/" className="hover:scale-110 transition-transform">
                <img
                  src="/images/logo.webp"
                  alt="Math Pup Logo"
                  className="w-10 h-10 object-contain"
                />
              </Link>
              <h1 className="text-2xl font-display font-bold text-gray-900">
                Quick Generator
              </h1>
            </div>
            <Link
              to="/help"
              className="text-primary-600 hover:underline text-sm"
            >
              Need Help?
            </Link>
          </div>
        </div>
      </header>

      <main className="container mx-auto px-4 py-8 max-w-2xl">
        {/* Toast Notification */}
        <Toast
          message={toastMessage}
          isVisible={showToast}
          onClose={() => setShowToast(false)}
        />

        {/* Intro Card */}
        <Card className="mb-6 bg-gradient-to-br from-primary-50 to-blue-50 border-2 border-primary-200 p-6">
          <div className="flex flex-col gap-4">
            <div className="flex items-start gap-4">
              <div className="text-3xl flex-shrink-0">✨</div>
              <div className="flex-1">
                <h2 className="font-display font-bold text-lg mb-2">
                  Auto-Generate Problems
                </h2>
                <p className="text-sm text-gray-600">
                  Set your rules and we'll create random problems for you.
                  Perfect for quick practice!
                </p>
              </div>
            </div>

            {/* Templates inside Intro Card */}
            <TemplateSection
              templates={generatedTemplates}
              onTemplateSelect={handleTemplateSelect}
              isExpanded={isTemplateExpanded}
              onToggle={setIsTemplateExpanded}
            />
          </div>
        </Card>

        {/* Form */}
        <Card className="p-8">
          <form onSubmit={handleSubmit} className="space-y-6">
            {/* Title */}
            <Input
              label="Challenge Title"
              placeholder="e.g., Addition Practice 1-20"
              value={formData.title}
              onChange={(e) =>
                setFormData({ ...formData, title: e.target.value })
              }
              error={errors.title}
              required
            />

            {/* Subtitle */}
            <Input
              label="Subtitle (Optional)"
              placeholder="e.g., Master basic addition skills"
              value={formData.subtitle}
              onChange={(e) =>
                setFormData({ ...formData, subtitle: e.target.value })
              }
              error={errors.subtitle}
              helperText="Add extra description to help your child"
            />

            {/* Operation */}
            <Select
              label="Math Operation"
              options={operationOptions}
              value={formData.operation}
              onChange={(e) =>
                setFormData({
                  ...formData,
                  operation: e.target.value as MathOperation,
                })
              }
              error={errors.operation}
            />

            {/* Problem Count */}
            <div>
              <Input
                label="Number of Problems"
                type="number"
                min={1}
                max={50}
                value={formData.problemCount}
                onChange={(e) =>
                  setFormData({
                    ...formData,
                    problemCount: parseInt(e.target.value) || 0,
                  })
                }
                error={errors.problemCount}
                helperText="Between 1 and 50 problems"
                className="text-center text-xl font-bold"
              />
            </div>

            {/* Number Range */}
            <div className="space-y-4">
              <label className="block text-sm font-medium text-gray-700">
                Number Range
              </label>
              <div className="grid grid-cols-2 gap-4">
                <Input
                  label="Minimum"
                  type="number"
                  min={0}
                  max={9999}
                  value={formData.minNumber}
                  onChange={(e) => {
                    const newMin = parseInt(e.target.value) || 0;
                    setFormData({
                      ...formData,
                      minNumber: newMin,
                      // Auto-adjust max if min becomes >= max
                      maxNumber:
                        newMin >= formData.maxNumber
                          ? newMin + 1
                          : formData.maxNumber,
                    });
                  }}
                  error={errors["numberRange.min"]}
                  className="text-center text-xl font-bold"
                />
                <Input
                  label="Maximum"
                  type="number"
                  min={0}
                  max={9999}
                  value={formData.maxNumber}
                  onChange={(e) =>
                    setFormData({
                      ...formData,
                      maxNumber: parseInt(e.target.value) || 0,
                    })
                  }
                  error={errors["numberRange.max"]}
                  className="text-center text-xl font-bold"
                />
              </div>
              <p className="text-sm text-gray-500">
                Problems will use random numbers between {formData.minNumber}{" "}
                and {formData.maxNumber}
              </p>
            </div>

            {/* Action Buttons */}
            <div className="flex gap-4 pt-4">
              <Button
                type="submit"
                variant="primary"
                size="lg"
                className="flex-1"
              >
                Generate Worksheet 🎉
              </Button>
              <Button
                type="button"
                variant="outline"
                size="lg"
                onClick={() => navigate("/")}
              >
                Cancel
              </Button>
            </div>
          </form>
        </Card>

        {/* Tips Card */}
        <Card className="mt-6 bg-amber-50 border-amber-200 p-6">
          <div className="flex items-start gap-4">
            <div className="text-2xl flex-shrink-0">💡</div>
            <div className="flex-1">
              <h3 className="font-bold text-gray-900 mb-2">Quick Tips</h3>
              <ul className="text-sm text-gray-700 space-y-1">
                <li>• Start with smaller ranges (0-10) for younger children</li>
                <li>
                  • For division, the app ensures all answers are whole numbers
                </li>
                <li>• For subtraction, results will always be positive</li>
                <li>• Try 5-10 problems for quick practice sessions</li>
              </ul>
            </div>
          </div>
        </Card>
      </main>
    </div>
  );
}
