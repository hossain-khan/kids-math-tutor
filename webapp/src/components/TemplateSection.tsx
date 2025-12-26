import { useState } from "react";
import Button from "./Button";
import {
  type GradeLevel,
  type GeneratedTemplate,
  type ExplicitTemplate,
} from "@/lib/templates";
import { generateDeeplink, isLikelyAndroidDevice } from "@/lib/deeplink";

type TemplateType = GeneratedTemplate | ExplicitTemplate;

type ColorScheme = "primary" | "secondary";

interface TemplateSectionProps {
  templates: Record<GradeLevel, TemplateType[]>;
  onTemplateSelect: (template: TemplateType) => void;
  isExpanded: boolean;
  onToggle: (expanded: boolean) => void;
  colorScheme?: ColorScheme;
}

export default function TemplateSection({
  templates,
  onTemplateSelect,
  isExpanded,
  onToggle,
  colorScheme = "primary",
}: TemplateSectionProps) {
  const [selectedGrade, setSelectedGrade] =
    useState<GradeLevel>("kindergarten");
  const [isAndroid] = useState(isLikelyAndroidDevice());

  const buttonBgClass =
    colorScheme === "secondary"
      ? "bg-secondary-100 text-secondary-700 border-secondary-200 hover:bg-secondary-200"
      : "bg-primary-100 text-primary-700 border-primary-200 hover:bg-primary-200";

  const gradeLevels: { value: GradeLevel; label: string }[] = [
    { value: "kindergarten", label: "Kindergarten" },
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
    <div>
      <style>{`
        @keyframes slideDown {
          from {
            opacity: 0;
            max-height: 0;
            overflow: hidden;
          }
          to {
            opacity: 1;
            max-height: 2000px;
            overflow: visible;
          }
        }
        
        @keyframes slideUp {
          from {
            opacity: 1;
            max-height: 2000px;
            overflow: visible;
          }
          to {
            opacity: 0;
            max-height: 0;
            overflow: hidden;
          }
        }
        
        .template-content {
          overflow: hidden;
        }
        
        .template-content.expanded {
          animation: slideDown 0.6s ease-out forwards;
          max-height: 2000px;
        }
        
        .template-content.collapsed {
          animation: slideUp 0.6s ease-out forwards;
          max-height: 0;
          display: none !important;
        }
      `}</style>

      {/* Collapsed Button */}
      {!isExpanded && (
        <Button
          type="button"
          variant="outline"
          size="md"
          onClick={() => onToggle(true)}
          className={`w-full text-left justify-start border ${buttonBgClass}`}
        >
          <span className="text-lg mr-2">📋</span>
          Browse Fun Worksheet Templates
        </Button>
      )}

      {/* Expanded Templates Section */}
      <div
        className={`template-content ${isExpanded ? "expanded" : "collapsed"}`}
      >
        {isExpanded && (
          <>
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
                <div
                  key={template.id}
                  className="flex flex-col text-left p-4 rounded-lg border-2 border-gray-200 bg-white hover:border-primary-400 hover:shadow-lg transition-all duration-200 h-full"
                >
                  <div>
                    <div className="text-3xl mb-2">{template.icon}</div>
                    <h4 className="font-bold text-gray-900 mb-1">
                      {template.name}
                    </h4>
                    <p className="text-xs text-gray-600 mb-3">
                      {template.description}
                    </p>
                  </div>

                  <div className="flex gap-2 mt-auto pt-3 border-t border-gray-100">
                    <button
                      type="button"
                      onClick={() => handleTemplateSelect(template)}
                      className="flex-1 px-3 py-2 text-sm font-medium text-primary-700 bg-primary-100 border border-primary-200 rounded hover:bg-primary-200 transition-colors"
                    >
                      Use Template
                    </button>
                    {isAndroid && (
                      <button
                        type="button"
                        onClick={() => {
                          const deeplink = generateDeeplink(template.config);
                          if (deeplink) {
                            window.location.href = deeplink;
                          }
                        }}
                        title="Open template directly in Math Pup app"
                        className="px-3 py-2 text-sm font-medium text-white bg-gradient-to-r from-purple-500 to-pink-500 rounded hover:from-purple-600 hover:to-pink-600 transition-colors"
                      >
                        📱 Open in App
                      </button>
                    )}
                  </div>
                </div>
              ))}
            </div>

            {currentTemplates.length === 0 && (
              <div className="text-center py-8 text-gray-500">
                No templates available for this grade level
              </div>
            )}
          </>
        )}
      </div>
    </div>
  );
}
