import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen, fireEvent } from "@testing-library/react";
import TemplateSection from "@/components/TemplateSection";
import { type GeneratedTemplate, type GradeLevel } from "@/lib/templates";
import * as deeplink from "@/lib/deeplink";

// Mock templates for testing
const mockTemplates: Record<GradeLevel, GeneratedTemplate[]> = {
  kindergarten: [
    {
      id: "kg-add-to-5",
      name: "Add to 5",
      description: "Practice adding numbers up to 5",
      icon: "🔢",
      config: {
        title: "Add to 5",
        subtitle: "Kindergarten - Adding numbers up to 5",
        operation: "addition",
        problemCount: 10,
        numberRange: { min: 0, max: 5 },
      },
    },
  ],
  grade1: [
    {
      id: "g1-add-to-10",
      name: "Add to 10",
      description: "Practice adding numbers up to 10",
      icon: "➕",
      config: {
        title: "Add to 10",
        subtitle: "Grade 1 - Adding numbers up to 10",
        operation: "addition",
        problemCount: 10,
        numberRange: { min: 0, max: 10 },
      },
    },
    {
      id: "g1-subtract-within-10",
      name: "Subtract Within 10",
      description: "Practice subtracting numbers within 10",
      icon: "➖",
      config: {
        title: "Subtract Within 10",
        subtitle: "Grade 1 - Subtracting within 10",
        operation: "subtraction",
        problemCount: 10,
        numberRange: { min: 0, max: 10 },
      },
    },
  ],
  grade2: [
    {
      id: "g2-add-to-20",
      name: "Add to 20",
      description: "Practice adding numbers up to 20",
      icon: "➕",
      config: {
        title: "Add to 20",
        subtitle: "Grade 2 - Adding numbers up to 20",
        operation: "addition",
        problemCount: 10,
        numberRange: { min: 0, max: 20 },
      },
    },
  ],
};

describe("TemplateSection Component", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("should render collapsed button when not expanded", () => {
    const mockOnSelect = vi.fn();
    const mockOnToggle = vi.fn();

    render(
      <TemplateSection
        templates={mockTemplates}
        onTemplateSelect={mockOnSelect}
        isExpanded={false}
        onToggle={mockOnToggle}
      />,
    );

    const button = screen.getByText(/Browse Fun Worksheet Templates/i);
    expect(button).toBeInTheDocument();
  });

  it("should toggle expanded state when button is clicked", () => {
    const mockOnSelect = vi.fn();
    const mockOnToggle = vi.fn();

    render(
      <TemplateSection
        templates={mockTemplates}
        onTemplateSelect={mockOnSelect}
        isExpanded={false}
        onToggle={mockOnToggle}
      />,
    );

    const button = screen.getByText(/Browse Fun Worksheet Templates/i);
    fireEvent.click(button);

    expect(mockOnToggle).toHaveBeenCalledWith(true);
  });

  it("should render templates section when expanded", () => {
    const mockOnSelect = vi.fn();
    const mockOnToggle = vi.fn();

    render(
      <TemplateSection
        templates={mockTemplates}
        onTemplateSelect={mockOnSelect}
        isExpanded={true}
        onToggle={mockOnToggle}
      />,
    );

    expect(screen.getByText(/Worksheet Templates/i)).toBeInTheDocument();
    expect(screen.getByText("Grade Level")).toBeInTheDocument();
  });

  it("should render Kindergarten templates by default", () => {
    const mockOnSelect = vi.fn();
    const mockOnToggle = vi.fn();

    render(
      <TemplateSection
        templates={mockTemplates}
        onTemplateSelect={mockOnSelect}
        isExpanded={true}
        onToggle={mockOnToggle}
      />,
    );

    expect(screen.getByText("Add to 5")).toBeInTheDocument();
    expect(
      screen.getByText("Practice adding numbers up to 5"),
    ).toBeInTheDocument();
  });

  it("should filter templates by grade level", () => {
    const mockOnSelect = vi.fn();
    const mockOnToggle = vi.fn();

    render(
      <TemplateSection
        templates={mockTemplates}
        onTemplateSelect={mockOnSelect}
        isExpanded={true}
        onToggle={mockOnToggle}
      />,
    );

    // Initially shows Kindergarten template
    expect(screen.getByText("Add to 5")).toBeInTheDocument();

    // Click on Grade 1 tab
    const grade1Button = screen.getByRole("button", { name: "Grade 1" });
    fireEvent.click(grade1Button);

    // Should show Grade 1 templates
    expect(screen.getByText("Add to 10")).toBeInTheDocument();
    expect(screen.getByText("Subtract Within 10")).toBeInTheDocument();
    expect(screen.queryByText("Add to 5")).not.toBeInTheDocument();
  });

  it("should call onTemplateSelect when template is clicked", () => {
    const mockOnSelect = vi.fn();
    const mockOnToggle = vi.fn();

    render(
      <TemplateSection
        templates={mockTemplates}
        onTemplateSelect={mockOnSelect}
        isExpanded={true}
        onToggle={mockOnToggle}
      />,
    );

    const templateButton = screen.getByRole("button", { name: /Add to 5/i });
    fireEvent.click(templateButton);

    expect(mockOnSelect).toHaveBeenCalledWith(mockTemplates.kindergarten[0]);
  });

  it("should close section after template selection", () => {
    const mockOnSelect = vi.fn();
    const mockOnToggle = vi.fn();

    render(
      <TemplateSection
        templates={mockTemplates}
        onTemplateSelect={mockOnSelect}
        isExpanded={true}
        onToggle={mockOnToggle}
      />,
    );

    const templateButton = screen.getByRole("button", { name: /Add to 5/i });
    fireEvent.click(templateButton);

    expect(mockOnToggle).toHaveBeenCalledWith(false);
  });

  it("should close section when close button is clicked", () => {
    const mockOnSelect = vi.fn();
    const mockOnToggle = vi.fn();

    render(
      <TemplateSection
        templates={mockTemplates}
        onTemplateSelect={mockOnSelect}
        isExpanded={true}
        onToggle={mockOnToggle}
      />,
    );

    const closeButton = screen.getByRole("button", { name: "✕" });
    fireEvent.click(closeButton);

    expect(mockOnToggle).toHaveBeenCalledWith(false);
  });

  it("should support primary color scheme (default)", () => {
    const mockOnSelect = vi.fn();
    const mockOnToggle = vi.fn();

    render(
      <TemplateSection
        templates={mockTemplates}
        onTemplateSelect={mockOnSelect}
        isExpanded={false}
        onToggle={mockOnToggle}
        colorScheme="primary"
      />,
    );

    const button = screen.getByText(/Browse Fun Worksheet Templates/i);
    expect(button.className).toContain("bg-primary-100");
    expect(button.className).toContain("text-primary-700");
  });

  it("should support secondary color scheme", () => {
    const mockOnSelect = vi.fn();
    const mockOnToggle = vi.fn();

    render(
      <TemplateSection
        templates={mockTemplates}
        onTemplateSelect={mockOnSelect}
        isExpanded={false}
        onToggle={mockOnToggle}
        colorScheme="secondary"
      />,
    );

    const button = screen.getByText(/Browse Fun Worksheet Templates/i);
    expect(button.className).toContain("bg-secondary-100");
    expect(button.className).toContain("text-secondary-700");
  });

  it("should display message when no templates available for grade level", () => {
    const emptyTemplates: Record<GradeLevel, GeneratedTemplate[]> = {
      kindergarten: [],
      grade1: [],
      grade2: [],
    };

    const mockOnSelect = vi.fn();
    const mockOnToggle = vi.fn();

    render(
      <TemplateSection
        templates={emptyTemplates}
        onTemplateSelect={mockOnSelect}
        isExpanded={true}
        onToggle={mockOnToggle}
      />,
    );

    expect(
      screen.getByText(/No templates available for this grade level/i),
    ).toBeInTheDocument();
  });

  it("should render all grade level options", () => {
    const mockOnSelect = vi.fn();
    const mockOnToggle = vi.fn();

    render(
      <TemplateSection
        templates={mockTemplates}
        onTemplateSelect={mockOnSelect}
        isExpanded={true}
        onToggle={mockOnToggle}
      />,
    );

    // Check for grade level tabs/buttons on desktop
    expect(
      screen.getByRole("button", { name: "Kindergarten" }),
    ).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Grade 1" })).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Grade 2" })).toBeInTheDocument();
  });

  it("should render template icons and descriptions", () => {
    const mockOnSelect = vi.fn();
    const mockOnToggle = vi.fn();

    render(
      <TemplateSection
        templates={mockTemplates}
        onTemplateSelect={mockOnSelect}
        isExpanded={true}
        onToggle={mockOnToggle}
      />,
    );

    expect(screen.getByText("🔢")).toBeInTheDocument();
    expect(screen.getByText("Add to 5")).toBeInTheDocument();
    expect(
      screen.getByText("Practice adding numbers up to 5"),
    ).toBeInTheDocument();
  });

  describe("Android deeplink functionality", () => {
    it("should not show 'Open in App' button on non-Android devices", () => {
      const mockOnSelect = vi.fn();
      const mockOnToggle = vi.fn();
      vi.spyOn(deeplink, "isLikelyAndroidDevice").mockReturnValue(false);

      render(
        <TemplateSection
          templates={mockTemplates}
          onTemplateSelect={mockOnSelect}
          isExpanded={true}
          onToggle={mockOnToggle}
        />,
      );

      expect(screen.queryByText(/Open in App/i)).not.toBeInTheDocument();
    });

    it("should show 'Open in App' button on Android devices", () => {
      const mockOnSelect = vi.fn();
      const mockOnToggle = vi.fn();
      vi.spyOn(deeplink, "isLikelyAndroidDevice").mockReturnValue(true);

      render(
        <TemplateSection
          templates={mockTemplates}
          onTemplateSelect={mockOnSelect}
          isExpanded={true}
          onToggle={mockOnToggle}
        />,
      );

      // Each template should have an "Open in App" button
      const openInAppButtons = screen.getAllByText(/Open in App/i);
      expect(openInAppButtons.length).toBeGreaterThan(0);
    });

    it("should generate deeplink when Open in App is clicked on Android", () => {
      const originalLocation = window.location;
      delete (window as Partial<Window>).location;
      window.location = { href: "" } as Location;

      const mockOnSelect = vi.fn();
      const mockOnToggle = vi.fn();
      const mockDeeplink =
        "mathpup://import?json=%7B%22type%22%3A%22generated%22%7D";

      vi.spyOn(deeplink, "isLikelyAndroidDevice").mockReturnValue(true);
      vi.spyOn(deeplink, "generateDeeplink").mockReturnValue(mockDeeplink);

      render(
        <TemplateSection
          templates={mockTemplates}
          onTemplateSelect={mockOnSelect}
          isExpanded={true}
          onToggle={mockOnToggle}
        />,
      );

      const openInAppButton = screen.getAllByText(/Open in App/i)[0];
      fireEvent.click(openInAppButton);

      expect(deeplink.generateDeeplink).toHaveBeenCalled();
      expect(window.location.href).toBe(mockDeeplink);

      // Restore
      window.location = originalLocation;
    });

    it("should still show 'Use Template' button alongside 'Open in App' on Android", () => {
      const mockOnSelect = vi.fn();
      const mockOnToggle = vi.fn();
      vi.spyOn(deeplink, "isLikelyAndroidDevice").mockReturnValue(true);

      render(
        <TemplateSection
          templates={mockTemplates}
          onTemplateSelect={mockOnSelect}
          isExpanded={true}
          onToggle={mockOnToggle}
        />,
      );

      // Should have both buttons
      expect(screen.getAllByText(/Use Template/i).length).toBeGreaterThan(0);
      expect(screen.getAllByText(/Open in App/i).length).toBeGreaterThan(0);
    });

    it("should pass template config to generateDeeplink", () => {
      const originalLocation = window.location;
      delete (window as Partial<Window>).location;
      window.location = { href: "" } as Location;

      const mockOnSelect = vi.fn();
      const mockOnToggle = vi.fn();
      const generateDeeplinkSpy = vi
        .spyOn(deeplink, "generateDeeplink")
        .mockReturnValue("mathpup://import?json=test");

      vi.spyOn(deeplink, "isLikelyAndroidDevice").mockReturnValue(true);

      render(
        <TemplateSection
          templates={mockTemplates}
          onTemplateSelect={mockOnSelect}
          isExpanded={true}
          onToggle={mockOnToggle}
        />,
      );

      const openInAppButton = screen.getAllByText(/Open in App/i)[0];
      fireEvent.click(openInAppButton);

      // Verify generateDeeplink was called with the template config
      expect(generateDeeplinkSpy).toHaveBeenCalledWith(
        mockTemplates.kindergarten[0].config,
      );

      // Restore
      window.location = originalLocation;
    });
  });
});
