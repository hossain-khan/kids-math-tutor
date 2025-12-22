import { z } from "zod";

// Math operation enum
export const MathOperationSchema = z.enum([
  "addition",
  "subtraction",
  "multiplication",
  "division",
]);

export type MathOperation = z.infer<typeof MathOperationSchema>;

// Number range schema
export const NumberRangeSchema = z
  .object({
    min: z.number().int().min(0).max(9999),
    max: z.number().int().min(0).max(9999),
  })
  .refine((data) => data.min < data.max, {
    message: "Minimum must be less than maximum",
    path: ["min"],
  });

export type NumberRange = z.infer<typeof NumberRangeSchema>;

// Problem specification schema
export const ProblemSpecSchema = z
  .object({
    operand1: z.number().int().min(0).max(9999),
    operand2: z.number().int().min(0).max(9999),
    operation: MathOperationSchema,
  })
  .refine(
    (data) => {
      // Division validation: operand2 must not be zero
      if (data.operation === "division" && data.operand2 === 0) {
        return false;
      }
      return true;
    },
    {
      message: "Cannot divide by zero",
      path: ["operand2"],
    },
  )
  .refine(
    (data) => {
      // Division validation: result must be a whole number
      if (
        data.operation === "division" &&
        data.operand1 % data.operand2 !== 0
      ) {
        return false;
      }
      return true;
    },
    {
      message: "Division result must be a whole number (no decimals)",
      path: ["operand1"],
    },
  )
  .refine(
    (data) => {
      // Overflow protection for addition
      if (
        data.operation === "addition" &&
        data.operand1 + data.operand2 > Number.MAX_SAFE_INTEGER
      ) {
        return false;
      }
      return true;
    },
    {
      message: "Result is too large (overflow)",
      path: ["operand1"],
    },
  )
  .refine(
    (data) => {
      // Overflow protection for multiplication
      if (
        data.operation === "multiplication" &&
        data.operand1 * data.operand2 > Number.MAX_SAFE_INTEGER
      ) {
        return false;
      }
      return true;
    },
    {
      message: "Result is too large (overflow)",
      path: ["operand1"],
    },
  )
  .refine(
    (data) => {
      // Subtraction validation: result must be non-negative
      if (data.operation === "subtraction" && data.operand1 < data.operand2) {
        return false;
      }
      return true;
    },
    {
      message:
        "Result cannot be negative (first number must be ≥ second number)",
      path: ["operand1"],
    },
  );

export type ProblemSpec = z.infer<typeof ProblemSpecSchema>;

// Generated challenge schema
export const GeneratedChallengeSpecSchema = z.object({
  type: z.literal("generated"),
  title: z
    .string()
    .min(1, "Title is required")
    .max(100, "Title must be 100 characters or less"),
  subtitle: z
    .string()
    .max(150, "Subtitle must be 150 characters or less")
    .optional(),
  operation: MathOperationSchema,
  problemCount: z
    .number()
    .int()
    .min(1, "Must create at least 1 problem")
    .max(50, "Maximum 50 problems allowed"),
  numberRange: NumberRangeSchema,
});

export type GeneratedChallengeSpec = z.infer<
  typeof GeneratedChallengeSpecSchema
>;

// Explicit challenge schema
export const ExplicitChallengeSpecSchema = z.object({
  type: z.literal("explicit"),
  title: z
    .string()
    .min(1, "Title is required")
    .max(100, "Title must be 100 characters or less"),
  subtitle: z
    .string()
    .max(150, "Subtitle must be 150 characters or less")
    .optional(),
  problems: z
    .array(ProblemSpecSchema)
    .min(1, "At least one problem is required")
    .max(50, "Maximum 50 problems allowed"),
});

export type ExplicitChallengeSpec = z.infer<typeof ExplicitChallengeSpecSchema>;

// Union of both types
export const ChallengeImportSpecSchema = z.discriminatedUnion("type", [
  GeneratedChallengeSpecSchema,
  ExplicitChallengeSpecSchema,
]);

export type ChallengeImportSpec = z.infer<typeof ChallengeImportSpecSchema>;
