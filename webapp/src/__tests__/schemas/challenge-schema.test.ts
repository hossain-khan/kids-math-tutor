import { describe, it, expect } from "vitest";
import {
  MathOperationSchema,
  NumberRangeSchema,
  ProblemSpecSchema,
  GeneratedChallengeSpecSchema,
  ExplicitChallengeSpecSchema,
} from "@/lib/schemas/challenge-schema";

describe("MathOperationSchema", () => {
  it("should accept valid math operations", () => {
    expect(MathOperationSchema.parse("addition")).toBe("addition");
    expect(MathOperationSchema.parse("subtraction")).toBe("subtraction");
    expect(MathOperationSchema.parse("multiplication")).toBe("multiplication");
    expect(MathOperationSchema.parse("division")).toBe("division");
  });

  it("should reject invalid operations", () => {
    expect(() => MathOperationSchema.parse("invalid")).toThrow();
    expect(() => MathOperationSchema.parse("add")).toThrow();
    expect(() => MathOperationSchema.parse("")).toThrow();
  });
});

describe("NumberRangeSchema", () => {
  it("should accept valid number ranges", () => {
    const result = NumberRangeSchema.parse({ min: 0, max: 10 });
    expect(result).toEqual({ min: 0, max: 10 });
  });

  it("should accept large valid ranges", () => {
    const result = NumberRangeSchema.parse({ min: 100, max: 9999 });
    expect(result).toEqual({ min: 100, max: 9999 });
  });

  it("should reject when min >= max", () => {
    expect(() => NumberRangeSchema.parse({ min: 10, max: 10 })).toThrow();
    expect(() => NumberRangeSchema.parse({ min: 20, max: 10 })).toThrow();
  });

  it("should reject negative numbers", () => {
    expect(() => NumberRangeSchema.parse({ min: -1, max: 10 })).toThrow();
    expect(() => NumberRangeSchema.parse({ min: 0, max: -5 })).toThrow();
  });

  it("should reject numbers above 9999", () => {
    expect(() => NumberRangeSchema.parse({ min: 0, max: 10000 })).toThrow();
    expect(() => NumberRangeSchema.parse({ min: 10000, max: 10001 })).toThrow();
  });
});

describe("ProblemSpecSchema", () => {
  describe("addition", () => {
    it("should accept valid addition problems", () => {
      const result = ProblemSpecSchema.parse({
        operand1: 5,
        operand2: 3,
        operation: "addition",
      });
      expect(result.operand1).toBe(5);
      expect(result.operand2).toBe(3);
    });

    it("should accept large addition problems", () => {
      const result = ProblemSpecSchema.parse({
        operand1: 9999,
        operand2: 1000,
        operation: "addition",
      });
      expect(result).toBeDefined();
    });
  });

  describe("subtraction", () => {
    it("should accept valid subtraction problems", () => {
      const result = ProblemSpecSchema.parse({
        operand1: 10,
        operand2: 3,
        operation: "subtraction",
      });
      expect(result.operand1).toBe(10);
    });

    it("should reject subtraction with negative result", () => {
      expect(() =>
        ProblemSpecSchema.parse({
          operand1: 3,
          operand2: 10,
          operation: "subtraction",
        }),
      ).toThrow(/negative|first number must be/);
    });

    it("should accept subtraction resulting in zero", () => {
      const result = ProblemSpecSchema.parse({
        operand1: 5,
        operand2: 5,
        operation: "subtraction",
      });
      expect(result).toBeDefined();
    });
  });

  describe("multiplication", () => {
    it("should accept valid multiplication problems", () => {
      const result = ProblemSpecSchema.parse({
        operand1: 12,
        operand2: 5,
        operation: "multiplication",
      });
      expect(result.operand1).toBe(12);
    });

    it("should accept multiplication with zero", () => {
      const result = ProblemSpecSchema.parse({
        operand1: 0,
        operand2: 10,
        operation: "multiplication",
      });
      expect(result).toBeDefined();
    });
  });

  describe("division", () => {
    it("should accept valid division problems", () => {
      const result = ProblemSpecSchema.parse({
        operand1: 12,
        operand2: 4,
        operation: "division",
      });
      expect(result.operand1).toBe(12);
      expect(result.operand2).toBe(4);
    });

    it("should reject division by zero", () => {
      expect(() =>
        ProblemSpecSchema.parse({
          operand1: 10,
          operand2: 0,
          operation: "division",
        }),
      ).toThrow(/divide by zero/);
    });

    it("should reject division with remainder", () => {
      expect(() =>
        ProblemSpecSchema.parse({
          operand1: 10,
          operand2: 3,
          operation: "division",
        }),
      ).toThrow(/whole number/);
    });

    it("should accept division resulting in 1", () => {
      const result = ProblemSpecSchema.parse({
        operand1: 5,
        operand2: 5,
        operation: "division",
      });
      expect(result).toBeDefined();
    });
  });

  it("should reject invalid operands", () => {
    expect(() =>
      ProblemSpecSchema.parse({
        operand1: -1,
        operand2: 5,
        operation: "addition",
      }),
    ).toThrow();

    expect(() =>
      ProblemSpecSchema.parse({
        operand1: 10000,
        operand2: 5,
        operation: "addition",
      }),
    ).toThrow();
  });
});

describe("GeneratedChallengeSpecSchema", () => {
  it("should accept valid generated challenge", () => {
    const result = GeneratedChallengeSpecSchema.parse({
      type: "generated",
      title: "Addition Practice",
      subtitle: "Numbers 1-20",
      operation: "addition",
      problemCount: 10,
      numberRange: { min: 1, max: 20 },
    });
    expect(result.type).toBe("generated");
    expect(result.problemCount).toBe(10);
  });

  it("should accept challenge without subtitle", () => {
    const result = GeneratedChallengeSpecSchema.parse({
      type: "generated",
      title: "Math Practice",
      operation: "multiplication",
      problemCount: 5,
      numberRange: { min: 0, max: 12 },
    });
    expect(result.subtitle).toBeUndefined();
  });

  it("should reject invalid problem count", () => {
    expect(() =>
      GeneratedChallengeSpecSchema.parse({
        type: "generated",
        title: "Test",
        operation: "addition",
        problemCount: 0,
        numberRange: { min: 1, max: 10 },
      }),
    ).toThrow();

    expect(() =>
      GeneratedChallengeSpecSchema.parse({
        type: "generated",
        title: "Test",
        operation: "addition",
        problemCount: 51,
        numberRange: { min: 1, max: 10 },
      }),
    ).toThrow();
  });

  it("should reject empty title", () => {
    expect(() =>
      GeneratedChallengeSpecSchema.parse({
        type: "generated",
        title: "",
        operation: "addition",
        problemCount: 10,
        numberRange: { min: 1, max: 20 },
      }),
    ).toThrow();
  });
});

describe("ExplicitChallengeSpecSchema", () => {
  it("should accept valid explicit challenge", () => {
    const result = ExplicitChallengeSpecSchema.parse({
      type: "explicit",
      title: "Custom Problems",
      subtitle: "Mixed practice",
      problems: [
        { operand1: 5, operand2: 3, operation: "addition" },
        { operand1: 10, operand2: 2, operation: "division" },
      ],
    });
    expect(result.type).toBe("explicit");
    expect(result.problems).toHaveLength(2);
  });

  it("should reject empty problems array", () => {
    expect(() =>
      ExplicitChallengeSpecSchema.parse({
        type: "explicit",
        title: "Test",
        problems: [],
      }),
    ).toThrow();
  });

  it("should reject too many problems", () => {
    const problems = Array(51).fill({
      operand1: 1,
      operand2: 1,
      operation: "addition",
    });
    expect(() =>
      ExplicitChallengeSpecSchema.parse({
        type: "explicit",
        title: "Test",
        problems,
      }),
    ).toThrow();
  });

  it("should accept maximum allowed problems", () => {
    const problems = Array(50).fill({
      operand1: 2,
      operand2: 2,
      operation: "multiplication",
    });
    const result = ExplicitChallengeSpecSchema.parse({
      type: "explicit",
      title: "Test",
      problems,
    });
    expect(result.problems).toHaveLength(50);
  });

  it("should validate individual problems", () => {
    expect(() =>
      ExplicitChallengeSpecSchema.parse({
        type: "explicit",
        title: "Test",
        problems: [
          { operand1: 10, operand2: 0, operation: "division" }, // Invalid: division by zero
        ],
      }),
    ).toThrow();
  });
});
