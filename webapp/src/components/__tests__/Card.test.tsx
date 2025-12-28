import { describe, it, expect } from "vitest";
import { render, screen } from "@testing-library/react";
import Card, {
  CardHeader,
  CardTitle,
  CardDescription,
  CardContent,
  CardFooter,
} from "@/components/Card";

describe("Card Components", () => {
  it("should render Card with default variant", () => {
    render(<Card>Test</Card>);
    expect(screen.getByText("Test")).toHaveClass(
      "bg-white rounded-2xl shadow-sm",
    );
  });

  it("should render Card with elevated variant", () => {
    render(<Card variant="elevated">Test</Card>);
    expect(screen.getByText("Test")).toHaveClass(
      "bg-white rounded-2xl shadow-lg hover:shadow-xl transition-shadow",
    );
  });

  it("should render Card with outlined variant", () => {
    render(<Card variant="outlined">Test</Card>);
    expect(screen.getByText("Test")).toHaveClass(
      "bg-white rounded-2xl border-2 border-gray-200",
    );
  });

  it("should render CardHeader", () => {
    render(<CardHeader>Test</CardHeader>);
    expect(screen.getByText("Test")).toBeInTheDocument();
  });

  it("should render CardTitle", () => {
    render(<CardTitle>Test</CardTitle>);
    expect(screen.getByText("Test")).toBeInTheDocument();
  });

  it("should render CardDescription", () => {
    render(<CardDescription>Test</CardDescription>);
    expect(screen.getByText("Test")).toBeInTheDocument();
  });

  it("should render CardContent", () => {
    render(<CardContent>Test</CardContent>);
    expect(screen.getByText("Test")).toBeInTheDocument();
  });

  it("should render CardFooter", () => {
    render(<CardFooter>Test</CardFooter>);
    expect(screen.getByText("Test")).toBeInTheDocument();
  });
});
