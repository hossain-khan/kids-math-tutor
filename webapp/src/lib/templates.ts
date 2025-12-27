import {
  type MathOperation,
  type ProblemSpec,
} from "./schemas/challenge-schema";

export type GradeLevel = "kindergarten" | "grade1" | "grade2";

export interface GeneratedTemplate {
  id: string;
  name: string;
  description: string;
  icon: string;
  config: {
    type: "generated";
    title: string;
    subtitle: string;
    operation: MathOperation;
    problemCount: number;
    numberRange: {
      min: number;
      max: number;
    };
  };
}

export interface ExplicitTemplate {
  id: string;
  name: string;
  description: string;
  icon: string;
  config: {
    type: "explicit";
    title: string;
    subtitle: string;
    problems: ProblemSpec[];
  };
}

// Generated (Quick Generator) Templates
export const generatedTemplates: Record<GradeLevel, GeneratedTemplate[]> = {
  kindergarten: [
    {
      id: "kg-add-to-5",
      name: "Add to 5",
      description: "Practice adding numbers up to 5",
      icon: "🔢",
      config: {
        type: "generated",
        title: "Add to 5",
        subtitle: "Kindergarten - Adding numbers up to 5",
        operation: "addition",
        problemCount: 10,
        numberRange: { min: 0, max: 5 },
      },
    },
    {
      id: "kg-subtract-within-5",
      name: "Subtract Within 5",
      description: "Practice subtracting numbers up to 5",
      icon: "➖",
      config: {
        type: "generated",
        title: "Subtract Within 5",
        subtitle: "Kindergarten - Subtracting numbers up to 5",
        operation: "subtraction",
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
        type: "generated",
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
        type: "generated",
        title: "Subtract Within 10",
        subtitle: "Grade 1 - Subtracting within 10",
        operation: "subtraction",
        problemCount: 10,
        numberRange: { min: 0, max: 10 },
      },
    },
    {
      id: "g1-multiply-basics",
      name: "Multiply Basics",
      description: "Practice basic multiplication (1-5)",
      icon: "✖️",
      config: {
        type: "generated",
        title: "Multiply Basics",
        subtitle: "Grade 1 - Basic multiplication with numbers 1-5",
        operation: "multiplication",
        problemCount: 10,
        numberRange: { min: 1, max: 5 },
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
        type: "generated",
        title: "Add to 20",
        subtitle: "Grade 2 - Adding numbers up to 20",
        operation: "addition",
        problemCount: 10,
        numberRange: { min: 0, max: 20 },
      },
    },
    {
      id: "g2-subtract-within-20",
      name: "Subtract Within 20",
      description: "Practice subtracting within 20",
      icon: "➖",
      config: {
        type: "generated",
        title: "Subtract Within 20",
        subtitle: "Grade 2 - Subtracting within 20",
        operation: "subtraction",
        problemCount: 10,
        numberRange: { min: 0, max: 20 },
      },
    },
    {
      id: "g2-multiply-extended",
      name: "Multiply (1-10)",
      description: "Practice multiplication with numbers 1-10",
      icon: "✖️",
      config: {
        type: "generated",
        title: "Multiply Extended",
        subtitle: "Grade 2 - Multiplying numbers 1-10",
        operation: "multiplication",
        problemCount: 10,
        numberRange: { min: 1, max: 10 },
      },
    },
    {
      id: "g2-divide-basics",
      name: "Divide Basics",
      description: "Practice basic division (numbers 2-10)",
      icon: "➗",
      config: {
        type: "generated",
        title: "Divide Basics",
        subtitle: "Grade 2 - Basic division with numbers 2-10",
        operation: "division",
        problemCount: 10,
        numberRange: { min: 2, max: 10 },
      },
    },
  ],
};

// Explicit (Custom Problems) Templates
export const explicitTemplates: Record<GradeLevel, ExplicitTemplate[]> = {
  kindergarten: [
    {
      id: "kg-exp-add-doubles",
      name: "Add Doubles",
      description: "Practice adding same numbers",
      icon: "👯",
      config: {
        type: "explicit",
        title: "Doubles",
        subtitle: "Kindergarten - Adding same numbers together",
        problems: [
          { operand1: 1, operand2: 1, operation: "addition" },
          { operand1: 2, operand2: 2, operation: "addition" },
          { operand1: 3, operand2: 3, operation: "addition" },
          { operand1: 4, operand2: 4, operation: "addition" },
          { operand1: 5, operand2: 5, operation: "addition" },
        ],
      },
    },
    {
      id: "kg-exp-number-bonds",
      name: "Number Bonds to 5",
      description: "Find pairs that make 5",
      icon: "🔗",
      config: {
        type: "explicit",
        title: "Number Bonds to 5",
        subtitle: "Kindergarten - Finding pairs that make 5",
        problems: [
          { operand1: 1, operand2: 4, operation: "addition" },
          { operand1: 2, operand2: 3, operation: "addition" },
          { operand1: 3, operand2: 2, operation: "addition" },
          { operand1: 4, operand2: 1, operation: "addition" },
          { operand1: 5, operand2: 0, operation: "addition" },
        ],
      },
    },
    {
      id: "kg-exp-speed-drills",
      name: "Speed Drills",
      description: "Quick addition facts within 5",
      icon: "⚡",
      config: {
        type: "explicit",
        title: "Speed Drills",
        subtitle: "Kindergarten - Build fluency with quick addition",
        problems: [
          { operand1: 1, operand2: 1, operation: "addition" },
          { operand1: 1, operand2: 2, operation: "addition" },
          { operand1: 2, operand2: 1, operation: "addition" },
          { operand1: 2, operand2: 2, operation: "addition" },
          { operand1: 1, operand2: 3, operation: "addition" },
        ],
      },
    },
    {
      id: "kg-exp-simple-facts",
      name: "Simple Addition",
      description: "Easy addition facts to build confidence",
      icon: "➕",
      config: {
        type: "explicit",
        title: "Simple Addition",
        subtitle: "Kindergarten - Building addition foundations",
        problems: [
          { operand1: 1, operand2: 0, operation: "addition" },
          { operand1: 2, operand2: 0, operation: "addition" },
          { operand1: 0, operand2: 3, operation: "addition" },
          { operand1: 1, operand2: 1, operation: "addition" },
          { operand1: 2, operand2: 2, operation: "addition" },
        ],
      },
    },
  ],
  grade1: [
    {
      id: "g1-exp-add-5s",
      name: "Add 5s",
      description: "Adding 5 to multiples of 5",
      icon: "5️⃣",
      config: {
        type: "explicit",
        title: "Add 5s Practice",
        subtitle: "Grade 1 - Adding to multiples of 5",
        problems: [
          { operand1: 5, operand2: 5, operation: "addition" },
          { operand1: 10, operand2: 5, operation: "addition" },
          { operand1: 3, operand2: 5, operation: "addition" },
          { operand1: 7, operand2: 5, operation: "addition" },
          { operand1: 2, operand2: 5, operation: "addition" },
          { operand1: 8, operand2: 5, operation: "addition" },
          { operand1: 4, operand2: 5, operation: "addition" },
          { operand1: 6, operand2: 5, operation: "addition" },
          { operand1: 1, operand2: 5, operation: "addition" },
          { operand1: 9, operand2: 5, operation: "addition" },
        ],
      },
    },
    {
      id: "g1-exp-doubles",
      name: "Add Doubles",
      description: "Practice adding same numbers",
      icon: "👯",
      config: {
        type: "explicit",
        title: "Doubles Practice",
        subtitle: "Grade 1 - Adding same numbers together",
        problems: [
          { operand1: 1, operand2: 1, operation: "addition" },
          { operand1: 2, operand2: 2, operation: "addition" },
          { operand1: 3, operand2: 3, operation: "addition" },
          { operand1: 4, operand2: 4, operation: "addition" },
          { operand1: 5, operand2: 5, operation: "addition" },
          { operand1: 6, operand2: 6, operation: "addition" },
        ],
      },
    },
    {
      id: "g1-exp-near-doubles",
      name: "Near Doubles",
      description: "Numbers close to doubles (5+4, 6+7)",
      icon: "📊",
      config: {
        type: "explicit",
        title: "Near Doubles",
        subtitle: "Grade 1 - Adding numbers near doubles",
        problems: [
          { operand1: 2, operand2: 3, operation: "addition" },
          { operand1: 3, operand2: 4, operation: "addition" },
          { operand1: 4, operand2: 5, operation: "addition" },
          { operand1: 5, operand2: 6, operation: "addition" },
          { operand1: 6, operand2: 7, operation: "addition" },
          { operand1: 7, operand2: 8, operation: "addition" },
          { operand1: 8, operand2: 9, operation: "addition" },
          { operand1: 1, operand2: 2, operation: "addition" },
          { operand1: 3, operand2: 2, operation: "addition" },
          { operand1: 4, operand2: 3, operation: "addition" },
        ],
      },
    },
    {
      id: "g1-exp-10-plus-x",
      name: "10 + X",
      description: "Add to 10, then add more",
      icon: "🔟",
      config: {
        type: "explicit",
        title: "10 Plus Numbers",
        subtitle: "Grade 1 - 10 plus other numbers",
        problems: [
          { operand1: 10, operand2: 1, operation: "addition" },
          { operand1: 10, operand2: 2, operation: "addition" },
          { operand1: 10, operand2: 3, operation: "addition" },
          { operand1: 10, operand2: 4, operation: "addition" },
          { operand1: 10, operand2: 5, operation: "addition" },
          { operand1: 10, operand2: 6, operation: "addition" },
          { operand1: 10, operand2: 7, operation: "addition" },
          { operand1: 10, operand2: 8, operation: "addition" },
          { operand1: 10, operand2: 9, operation: "addition" },
          { operand1: 10, operand2: 10, operation: "addition" },
        ],
      },
    },
    {
      id: "g1-exp-number-bonds",
      name: "Number Bonds to 10",
      description: "Find pairs that make 10",
      icon: "🔗",
      config: {
        type: "explicit",
        title: "Number Bonds to 10",
        subtitle: "Grade 1 - Finding pairs that make 10",
        problems: [
          { operand1: 4, operand2: 6, operation: "addition" },
          { operand1: 5, operand2: 5, operation: "addition" },
          { operand1: 3, operand2: 7, operation: "addition" },
          { operand1: 2, operand2: 8, operation: "addition" },
          { operand1: 1, operand2: 9, operation: "addition" },
        ],
      },
    },
    {
      id: "g1-exp-fact-families",
      name: "Fact Families",
      description: "Simple related addition & subtraction facts",
      icon: "👨‍👩‍👧‍👦",
      config: {
        type: "explicit",
        title: "Fact Families",
        subtitle: "Grade 1 - Related addition and subtraction facts",
        problems: [
          { operand1: 2, operand2: 3, operation: "addition" },
          { operand1: 3, operand2: 2, operation: "addition" },
          { operand1: 5, operand2: 2, operation: "subtraction" },
          { operand1: 5, operand2: 3, operation: "subtraction" },
          { operand1: 3, operand2: 4, operation: "addition" },
          { operand1: 4, operand2: 3, operation: "addition" },
          { operand1: 7, operand2: 3, operation: "subtraction" },
          { operand1: 7, operand2: 4, operation: "subtraction" },
        ],
      },
    },
    {
      id: "g1-exp-speed-drills",
      name: "Speed Drills",
      description: "Quick addition facts within 10",
      icon: "⚡",
      config: {
        type: "explicit",
        title: "Speed Drills",
        subtitle: "Grade 1 - Build fluency with quick addition",
        problems: [
          { operand1: 1, operand2: 1, operation: "addition" },
          { operand1: 2, operand2: 1, operation: "addition" },
          { operand1: 3, operand2: 1, operation: "addition" },
          { operand1: 4, operand2: 1, operation: "addition" },
          { operand1: 2, operand2: 2, operation: "addition" },
          { operand1: 3, operand2: 2, operation: "addition" },
          { operand1: 4, operand2: 2, operation: "addition" },
          { operand1: 5, operand2: 2, operation: "addition" },
        ],
      },
    },
    {
      id: "g1-exp-mixed-operations",
      name: "Mixed Operations",
      description: "Add and subtract for variety",
      icon: "🎲",
      config: {
        type: "explicit",
        title: "Mixed Operations Review",
        subtitle: "Grade 1 - Mix of addition and subtraction",
        problems: [
          { operand1: 3, operand2: 2, operation: "addition" },
          { operand1: 5, operand2: 2, operation: "subtraction" },
          { operand1: 4, operand2: 3, operation: "addition" },
          { operand1: 7, operand2: 1, operation: "subtraction" },
          { operand1: 6, operand2: 2, operation: "addition" },
          { operand1: 8, operand2: 3, operation: "subtraction" },
          { operand1: 5, operand2: 4, operation: "addition" },
          { operand1: 9, operand2: 2, operation: "subtraction" },
        ],
      },
    },
  ],
  grade2: [
    {
      id: "g2-exp-add-5s",
      name: "Add 5s",
      description: "Adding 5 to multiples of 5",
      icon: "5️⃣",
      config: {
        type: "explicit",
        title: "Add 5s Practice",
        subtitle: "Grade 2 - Adding to multiples of 5",
        problems: [
          { operand1: 5, operand2: 5, operation: "addition" },
          { operand1: 10, operand2: 5, operation: "addition" },
          { operand1: 15, operand2: 5, operation: "addition" },
          { operand1: 20, operand2: 5, operation: "addition" },
          { operand1: 8, operand2: 5, operation: "addition" },
          { operand1: 12, operand2: 5, operation: "addition" },
          { operand1: 3, operand2: 5, operation: "addition" },
          { operand1: 7, operand2: 5, operation: "addition" },
          { operand1: 18, operand2: 5, operation: "addition" },
          { operand1: 13, operand2: 5, operation: "addition" },
        ],
      },
    },
    {
      id: "g2-exp-doubles",
      name: "Add Doubles",
      description: "Practice adding same numbers",
      icon: "👯",
      config: {
        type: "explicit",
        title: "Doubles Practice",
        subtitle: "Grade 2 - Adding same numbers together",
        problems: [
          { operand1: 5, operand2: 5, operation: "addition" },
          { operand1: 6, operand2: 6, operation: "addition" },
          { operand1: 7, operand2: 7, operation: "addition" },
          { operand1: 8, operand2: 8, operation: "addition" },
          { operand1: 9, operand2: 9, operation: "addition" },
          { operand1: 10, operand2: 10, operation: "addition" },
          { operand1: 4, operand2: 4, operation: "addition" },
          { operand1: 3, operand2: 3, operation: "addition" },
          { operand1: 2, operand2: 2, operation: "addition" },
          { operand1: 1, operand2: 1, operation: "addition" },
        ],
      },
    },
    {
      id: "g2-exp-100-plus-x",
      name: "100 + X",
      description: "Add to 100, then add more",
      icon: "💯",
      config: {
        type: "explicit",
        title: "100 Plus Numbers",
        subtitle: "Grade 2 - 100 plus other numbers",
        problems: [
          { operand1: 100, operand2: 1, operation: "addition" },
          { operand1: 100, operand2: 5, operation: "addition" },
          { operand1: 100, operand2: 10, operation: "addition" },
          { operand1: 100, operand2: 15, operation: "addition" },
          { operand1: 100, operand2: 20, operation: "addition" },
          { operand1: 100, operand2: 2, operation: "addition" },
          { operand1: 100, operand2: 8, operation: "addition" },
          { operand1: 100, operand2: 12, operation: "addition" },
          { operand1: 100, operand2: 18, operation: "addition" },
          { operand1: 100, operand2: 3, operation: "addition" },
        ],
      },
    },
    {
      id: "g2-exp-multiply-10",
      name: "Multiply by 10",
      description: "Practice multiplying by 10",
      icon: "✖️",
      config: {
        type: "explicit",
        title: "Multiply by 10",
        subtitle: "Grade 2 - Multiplying numbers by 10",
        problems: [
          { operand1: 1, operand2: 10, operation: "multiplication" },
          { operand1: 2, operand2: 10, operation: "multiplication" },
          { operand1: 3, operand2: 10, operation: "multiplication" },
          { operand1: 4, operand2: 10, operation: "multiplication" },
          { operand1: 5, operand2: 10, operation: "multiplication" },
          { operand1: 6, operand2: 10, operation: "multiplication" },
          { operand1: 7, operand2: 10, operation: "multiplication" },
          { operand1: 8, operand2: 10, operation: "multiplication" },
          { operand1: 9, operand2: 10, operation: "multiplication" },
          { operand1: 10, operand2: 10, operation: "multiplication" },
        ],
      },
    },
    {
      id: "g2-exp-divide-2",
      name: "Divide by 2",
      description: "Practice dividing by 2",
      icon: "➗",
      config: {
        type: "explicit",
        title: "Divide by 2",
        subtitle: "Grade 2 - Dividing numbers by 2",
        problems: [
          { operand1: 2, operand2: 2, operation: "division" },
          { operand1: 4, operand2: 2, operation: "division" },
          { operand1: 6, operand2: 2, operation: "division" },
          { operand1: 8, operand2: 2, operation: "division" },
          { operand1: 10, operand2: 2, operation: "division" },
          { operand1: 12, operand2: 2, operation: "division" },
          { operand1: 14, operand2: 2, operation: "division" },
          { operand1: 16, operand2: 2, operation: "division" },
          { operand1: 18, operand2: 2, operation: "division" },
          { operand1: 20, operand2: 2, operation: "division" },
        ],
      },
    },
    {
      id: "g2-exp-tens-ones",
      name: "Tens & Ones",
      description: "Practice with 2-digit numbers",
      icon: "🔢",
      config: {
        type: "explicit",
        title: "Tens and Ones",
        subtitle: "Grade 2 - Adding tens and ones",
        problems: [
          { operand1: 10, operand2: 5, operation: "addition" },
          { operand1: 20, operand2: 3, operation: "addition" },
          { operand1: 15, operand2: 2, operation: "addition" },
          { operand1: 30, operand2: 7, operation: "addition" },
          { operand1: 25, operand2: 4, operation: "addition" },
          { operand1: 12, operand2: 6, operation: "addition" },
          { operand1: 18, operand2: 1, operation: "addition" },
          { operand1: 21, operand2: 8, operation: "addition" },
          { operand1: 14, operand2: 5, operation: "addition" },
          { operand1: 27, operand2: 2, operation: "addition" },
        ],
      },
    },
    {
      id: "g2-exp-fact-families",
      name: "Fact Families",
      description: "Related addition & subtraction facts",
      icon: "👨‍👩‍👧‍👦",
      config: {
        type: "explicit",
        title: "Fact Families",
        subtitle: "Grade 2 - Related addition and subtraction facts",
        problems: [
          { operand1: 3, operand2: 4, operation: "addition" },
          { operand1: 4, operand2: 3, operation: "addition" },
          { operand1: 7, operand2: 3, operation: "subtraction" },
          { operand1: 7, operand2: 4, operation: "subtraction" },
          { operand1: 5, operand2: 6, operation: "addition" },
          { operand1: 6, operand2: 5, operation: "addition" },
          { operand1: 11, operand2: 5, operation: "subtraction" },
          { operand1: 11, operand2: 6, operation: "subtraction" },
          { operand1: 7, operand2: 8, operation: "addition" },
          { operand1: 8, operand2: 7, operation: "addition" },
        ],
      },
    },
    {
      id: "g2-exp-skip-counting",
      name: "Skip Counting",
      description: "Count by 2s, 5s, and 10s (multiplication prep)",
      icon: "🔢",
      config: {
        type: "explicit",
        title: "Skip Counting",
        subtitle: "Grade 2 - Multiplication preparation with patterns",
        problems: [
          { operand1: 2, operand2: 1, operation: "multiplication" },
          { operand1: 2, operand2: 2, operation: "multiplication" },
          { operand1: 2, operand2: 3, operation: "multiplication" },
          { operand1: 5, operand2: 1, operation: "multiplication" },
          { operand1: 5, operand2: 2, operation: "multiplication" },
          { operand1: 5, operand2: 3, operation: "multiplication" },
          { operand1: 10, operand2: 1, operation: "multiplication" },
          { operand1: 10, operand2: 2, operation: "multiplication" },
          { operand1: 10, operand2: 3, operation: "multiplication" },
          { operand1: 2, operand2: 5, operation: "multiplication" },
        ],
      },
    },
    {
      id: "g2-exp-mixed-operations",
      name: "Mixed Operations",
      description: "Random mix of +, -, × for skill review",
      icon: "🎲",
      config: {
        type: "explicit",
        title: "Mixed Operations Review",
        subtitle: "Grade 2 - Mix of addition, subtraction, and multiplication",
        problems: [
          { operand1: 5, operand2: 3, operation: "addition" },
          { operand1: 8, operand2: 2, operation: "subtraction" },
          { operand1: 3, operand2: 4, operation: "multiplication" },
          { operand1: 12, operand2: 4, operation: "subtraction" },
          { operand1: 6, operand2: 6, operation: "addition" },
          { operand1: 4, operand2: 5, operation: "multiplication" },
          { operand1: 15, operand2: 7, operation: "subtraction" },
          { operand1: 9, operand2: 4, operation: "addition" },
          { operand1: 2, operand2: 7, operation: "multiplication" },
          { operand1: 20, operand2: 8, operation: "subtraction" },
        ],
      },
    },
    {
      id: "g2-exp-speed-drills",
      name: "Speed Drills",
      description: "Quick facts for building automaticity",
      icon: "⚡",
      config: {
        type: "explicit",
        title: "Speed Drills",
        subtitle: "Grade 2 - Build fluency with quick practice",
        problems: [
          { operand1: 1, operand2: 1, operation: "addition" },
          { operand1: 2, operand2: 1, operation: "addition" },
          { operand1: 3, operand2: 1, operation: "addition" },
          { operand1: 4, operand2: 1, operation: "addition" },
          { operand1: 5, operand2: 1, operation: "addition" },
          { operand1: 2, operand2: 2, operation: "addition" },
          { operand1: 3, operand2: 2, operation: "addition" },
          { operand1: 4, operand2: 2, operation: "addition" },
          { operand1: 5, operand2: 2, operation: "addition" },
          { operand1: 3, operand2: 3, operation: "addition" },
        ],
      },
    },
  ],
};
