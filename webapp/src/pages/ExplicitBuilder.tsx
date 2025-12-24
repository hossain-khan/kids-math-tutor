import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import Button from "@/components/Button";
import Input from "@/components/Input";
import Select from "@/components/Select";
import Card from "@/components/Card";
import {
  ExplicitChallengeSpecSchema,
  type MathOperation,
  type ProblemSpec,
} from "@/lib/schemas/challenge-schema";
import { z } from "zod";

export default function ExplicitBuilder() {
  const navigate = useNavigate();
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [formData, setFormData] = useState({
    title: "",
    subtitle: "",
  });
  const [defaultOperation, setDefaultOperation] =
    useState<MathOperation>("addition");
  const [problems, setProblems] = useState<ProblemSpec[]>([
    { operand1: 0, operand2: 0, operation: "addition" as MathOperation },
  ]);

  const operationOptions = [
    { value: "addition", label: "➕ Addition" },
    { value: "subtraction", label: "➖ Subtraction" },
    { value: "multiplication", label: "✖️ Multiplication" },
    { value: "division", label: "➗ Division" },
  ];

  const addProblem = () => {
    if (problems.length < 50) {
      setProblems([
        ...problems,
        { operand1: 0, operand2: 0, operation: defaultOperation },
      ]);
    }
  };

  const removeProblem = (index: number) => {
    if (problems.length > 1) {
      setProblems(problems.filter((_, i) => i !== index));
    }
  };

  const updateProblem = (
    index: number,
    field: keyof ProblemSpec,
    value: string | number,
  ) => {
    const newProblems = [...problems];
    newProblems[index] = { ...newProblems[index], [field]: value };
    setProblems(newProblems);
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setErrors({});

    try {
      // Validate the form data
      const challengeData = {
        type: "explicit" as const,
        title: formData.title,
        subtitle: formData.subtitle || undefined,
        problems: problems,
      };

      ExplicitChallengeSpecSchema.parse(challengeData);

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

        // Scroll to first error
        const firstErrorElement = document.querySelector('[data-error="true"]');
        if (firstErrorElement) {
          firstErrorElement.scrollIntoView({
            behavior: "smooth",
            block: "center",
          });
        }
      }
    }
  };

  const getOperationSymbol = (operation: MathOperation) => {
    const symbols = {
      addition: "+",
      subtraction: "-",
      multiplication: "×",
      division: "÷",
    };
    return symbols[operation];
  };

  const calculateResult = (problem: ProblemSpec): number | string => {
    const { operand1, operand2, operation } = problem;

    if (operand2 === 0 && operation === "division") return "Error";

    switch (operation) {
      case "addition":
        return operand1 + operand2;
      case "subtraction":
        return operand1 - operand2;
      case "multiplication":
        return operand1 * operand2;
      case "division":
        return operand1 % operand2 === 0
          ? operand1 / operand2
          : (operand1 / operand2).toFixed(2);
      default:
        return 0;
    }
  };

  const validateProblem = (problem: ProblemSpec): string | null => {
    const { operand1, operand2, operation } = problem;

    // Division by zero
    if (operation === "division" && operand2 === 0) {
      return "⚠️ Cannot divide by zero";
    }

    // Division must result in whole number
    if (operation === "division" && operand1 % operand2 !== 0) {
      return "⚠️ Division result must be a whole number (no decimals)";
    }

    // Subtraction must not result in negative
    if (operation === "subtraction" && operand1 < operand2) {
      return "⚠️ Result cannot be negative (first number must be ≥ second number)";
    }

    return null;
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
                Custom Problems
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

      <main className="container mx-auto px-4 py-8 max-w-3xl">
        {/* Intro Card */}
        <Card className="mb-6 bg-gradient-to-br from-secondary-50 to-purple-50 border-2 border-secondary-200 p-6">
          <div className="flex items-start gap-4">
            <div className="text-3xl flex-shrink-0">✏️</div>
            <div className="flex-1">
              <h2 className="font-display font-bold text-lg mb-2">
                Create Each Problem
              </h2>
              <p className="text-sm text-gray-600">
                Enter each math problem exactly how you want it. Perfect for
                targeting specific skills!
              </p>
            </div>
          </div>
        </Card>

        {/* Form */}
        <Card className="p-8">
          <form onSubmit={handleSubmit} className="space-y-6">
            {/* Title */}
            <Input
              label="Challenge Title"
              placeholder="e.g., Tricky Division Problems"
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
              placeholder="e.g., Focus on dividing by 5"
              value={formData.subtitle}
              onChange={(e) =>
                setFormData({ ...formData, subtitle: e.target.value })
              }
              error={errors.subtitle}
              helperText="Add extra description to help your child"
            />

            {/* Problems List */}
            <div className="space-y-4">
              <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3">
                <label className="block text-sm font-medium text-gray-700">
                  Math Problems ({problems.length}/50)
                </label>
                <div className="flex flex-col sm:flex-row items-stretch sm:items-center gap-2">
                  <Select
                    value={defaultOperation}
                    onChange={(e) =>
                      setDefaultOperation(e.target.value as MathOperation)
                    }
                    options={operationOptions}
                    className="text-sm"
                  />
                  <Button
                    type="button"
                    variant="primary"
                    size="md"
                    onClick={addProblem}
                    disabled={problems.length >= 50}
                    className="w-full sm:w-auto sm:min-w-[160px] font-semibold whitespace-nowrap"
                  >
                    ➕ Add Problem
                  </Button>
                </div>
              </div>

              {errors.problems && (
                <p className="text-sm text-red-600">{errors.problems}</p>
              )}

              <div className="space-y-3">
                {problems.map((problem, index) => {
                  const problemError = errors[`problems.${index}`];
                  const validationError = validateProblem(problem);
                  return (
                    <Card
                      key={index}
                      variant="outlined"
                      className="p-4"
                      data-error={!!problemError}
                    >
                      <div className="flex flex-col md:flex-row md:items-center gap-3">
                        <div className="flex-shrink-0 w-8 h-8 rounded-full bg-primary-100 text-primary-700 flex items-center justify-center font-bold text-sm">
                          {index + 1}
                        </div>

                        <div className="flex-1 flex flex-col md:grid md:grid-cols-9 gap-2 md:items-center">
                          {/* First Number */}
                          <div className="md:col-span-2">
                            <label className="text-xs font-medium text-gray-600 md:hidden block mb-1">
                              First Number
                            </label>
                            <Input
                              type="number"
                              min={0}
                              max={9999}
                              value={problem.operand1}
                              onChange={(e) =>
                                updateProblem(
                                  index,
                                  "operand1",
                                  parseInt(e.target.value) || 0,
                                )
                              }
                              className="text-center text-xl font-bold"
                            />
                          </div>

                          {/* Operation */}
                          <div className="md:col-span-3">
                            <label className="text-xs font-medium text-gray-600 md:hidden block mb-1">
                              Operation
                            </label>
                            <Select
                              options={operationOptions}
                              value={problem.operation}
                              onChange={(e) =>
                                updateProblem(
                                  index,
                                  "operation",
                                  e.target.value as MathOperation,
                                )
                              }
                              className="text-center text-lg font-semibold"
                            />
                          </div>

                          {/* Second Number */}
                          <div className="md:col-span-2">
                            <label className="text-xs font-medium text-gray-600 md:hidden block mb-1">
                              Second Number
                            </label>
                            <Input
                              type="number"
                              min={0}
                              max={9999}
                              value={problem.operand2}
                              onChange={(e) =>
                                updateProblem(
                                  index,
                                  "operand2",
                                  parseInt(e.target.value) || 0,
                                )
                              }
                              className="text-center text-xl font-bold"
                            />
                          </div>

                          {/* Result */}
                          <div className="md:col-span-2 text-center">
                            <label className="text-xs font-medium text-gray-600 md:hidden block mb-1">
                              Result
                            </label>
                            <div
                              className={`px-4 py-3 rounded-xl border-2 ${
                                validationError
                                  ? "bg-red-50 border-red-300"
                                  : "bg-gray-50 border-gray-200"
                              }`}
                            >
                              <span className="text-gray-400 text-lg font-semibold mr-2 hidden md:inline">
                                =
                              </span>
                              <span
                                className={`text-xl font-bold ${
                                  validationError
                                    ? "text-red-600"
                                    : "text-primary-600"
                                }`}
                              >
                                {calculateResult(problem)}
                              </span>
                            </div>
                          </div>
                        </div>

                        {/* Remove Button */}
                        {problems.length > 1 && (
                          <Button
                            type="button"
                            variant="ghost"
                            size="sm"
                            onClick={() => removeProblem(index)}
                            className="flex-shrink-0 text-red-600 hover:bg-red-50 self-start"
                          >
                            🗑️
                          </Button>
                        )}
                      </div>

                      {/* Validation Error Message */}
                      {validationError && (
                        <div className="mt-2 px-3 py-2 bg-red-50 border border-red-200 rounded-lg">
                          <p className="text-sm text-red-700 flex items-center gap-2">
                            <span className="text-base">⚠️</span>
                            <span>{validationError}</span>
                          </p>
                        </div>
                      )}

                      {/* Preview Equation */}
                      <div className="mt-2 text-center text-sm text-gray-600 font-mono">
                        {problem.operand1}{" "}
                        {getOperationSymbol(problem.operation)}{" "}
                        {problem.operand2} = ?
                      </div>

                      {/* Individual Problem Error */}
                      {problemError && (
                        <p className="text-sm text-red-600 mt-2">
                          {problemError}
                        </p>
                      )}
                    </Card>
                  );
                })}
              </div>
            </div>

            {/* Action Buttons */}
            <div className="flex gap-4 pt-4">
              <Button
                type="submit"
                variant="secondary"
                size="lg"
                className="flex-1"
              >
                Create Worksheet 🎉
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
              <h3 className="font-bold text-gray-900 mb-2">Problem Tips</h3>
              <ul className="text-sm text-gray-700 space-y-1">
                <li>
                  • <strong>Division:</strong> Make sure the first number
                  divides evenly (no decimals)
                </li>
                <li>
                  • <strong>Subtraction:</strong> First number must be ≥ second
                  number (no negatives)
                </li>
                <li>
                  • <strong>Mix it up:</strong> Combine different operations for
                  variety
                </li>
                <li>• You can create up to 50 problems per worksheet</li>
              </ul>
            </div>
          </div>
        </Card>
      </main>
    </div>
  );
}
