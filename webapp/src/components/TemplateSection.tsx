import { useState } from "react";
import Button from "./Button";
import Card from "./Card";
import {
  type GradeLevel,
  type GeneratedTemplate,
  type ExplicitTemplate,
} from "@/lib/templates";

type TemplateType = GeneratedTemplate | ExplicitTemplate;

interface TemplateSectionProps {
  templates: Record<GradeLevel, TemplateType[]>;
  onTemplateSelect: (template: TemplateType) => void;
  isExpanded: boolean;
  onToggle: (expanded: boolean) => void;
}

export default function TemplateSection({
  templates,
  onTemplateSelect,
  isExpanded,
  onToggle,
}: TemplateSectionProps) {
  const [selectedGrade, setSelectedGrade] =
    useState<GradeLevel>("kindergarten");

  const gradeLevels: { value: GradeLevel; label: string }[] = [
    { value: "kindergarten", label: "K" },
    { value: "grade1", label: "Grade 1" },
    { value: "grade2", label: "Grade 2" },
  ];

  const currentTemplates = templates[selectedGrade] || [];

  const handleTemplateSelect = (template: TemplateType) => {
    onTemplateSelect(template);
    // Close the template section after selection
    onToggle(false);
  };

  return (
    <div className="mb-6">
      {/* Collapsed Button */}
      {!isExpanded && (
        <Button
          type="button"
          variant="outline"
          size="md"
          onClick={() => onToggle(true)}
          className="w-full text-left justify-start"
        >
          <span className="text-lg mr-2">📋</span>
          Browse Fun Worksheet Templates
        </Button>
      )}

      {/* Expanded Templates Section */}
      {isExpanded && (
        <Card className="p-6 border-2 border-primary-200 bg-gradient-to-br from-primary-50 to-blue-50">
          {/* Close Button */}
          <div className="flex justify-between items-center mb-4">
            <h3 className="text-lg font-bold text-gray-900">
              📋 Worksheet Templates
            </h3>
            <Button
              type="button"
              variant="ghost"
              size="sm"
              onClick={() => onToggle(false)}
              className="text-gray-400 hover:text-gray-600"
            >
              ✕
            </Button>
          </div>

          {/* Grade Level Selector */}
          <div className="mb-4">
            <label className="text-sm font-medium text-gray-700 block mb-2">
              Grade Level
            </label>
            {/* Tabs for desktop, Dropdown for mobile */}
            <div className="hidden md:flex gap-2">
              {gradeLevels.map((grade) => (
                <button
                  key={grade.value}
                  type="button"
                  onClick={() => setSelectedGrade(grade.value)}
                  className={`px-4 py-2 rounded-lg font-medium transition-colors ${
                    selectedGrade === grade.value
                      ? "bg-primary-600 text-white"
                      : "bg-white text-gray-700 border border-gray-300 hover:bg-gray-50"
                  }`}
                >
                  {grade.label}
                </button>
              ))}
            </div>

            {/* Dropdown for mobile */}
            <select
              value={selectedGrade}
              onChange={(e) => setSelectedGrade(e.target.value as GradeLevel)}
              className="md:hidden w-full px-3 py-2 border border-gray-300 rounded-lg bg-white text-gray-900"
            >
              {gradeLevels.map((grade) => (
                <option key={grade.value} value={grade.value}>
                  {grade.label}
                </option>
              ))}
            </select>
          </div>

          {/* Templates Grid */}
          <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-3">
            {currentTemplates.map((template) => (
              <button
                key={template.id}
                type="button"
                onClick={() => handleTemplateSelect(template)}
                className="text-left p-4 rounded-lg border-2 border-gray-200 bg-white hover:border-primary-400 hover:shadow-lg transition-all duration-200"
              >
                <div className="text-3xl mb-2">{template.icon}</div>
                <h4 className="font-bold text-gray-900 mb-1">
                  {template.name}
                </h4>
                <p className="text-xs text-gray-600">{template.description}</p>
              </button>
            ))}
          </div>

          {currentTemplates.length === 0 && (
            <div className="text-center py-8 text-gray-500">
              No templates available for this grade level
            </div>
          )}
        </Card>
      )}
    </div>
  );
}
