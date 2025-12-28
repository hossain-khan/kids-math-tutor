/**
 * Content safety using Cloudflare Workers AI (Llama Guard 3)
 * Provides intelligent content classification for educational worksheets
 *
 * FREE TIER ONLY: 10,000 Neurons per day (no cost)
 * Falls back to bad-words if limit exceeded or AI unavailable
 */

import { Filter } from "bad-words";

// AI Model configuration
export const AI_SAFETY_CONFIG = {
  // Llama Guard 3 is specifically designed for content safety
  DEFAULT_MODEL: "@cf/meta/llama-guard-3-8b",
  // Alternative models for testing (in order of preference for safety)
  ALTERNATIVE_MODELS: [
    "@cf/meta/llama-3.3-70b-instruct-fp8-fast",
    "@cf/meta/llama-3.1-8b-instruct-fast",
    "@cf/meta/llama-4-scout-17b-16e-instruct",
  ],
} as const;

export interface SafetyCheckResult {
  safe: boolean;
  classification: "safe" | "unsafe";
  categories?: string[];
  explanation?: string;
  confidence?: number;
  usingAI?: boolean; // true = AI result, false = bad-words fallback
  fallback?: boolean; // true if fell back to bad-words
}

interface AIResponse {
  response?: string;
  result?: string;
  message?: string;
  [key: string]: unknown;
}

/**
 * Check content safety using Cloudflare Workers AI (Llama Guard 3)
 * Falls back to bad-words if AI is unavailable or quota exceeded
 *
 * @param env - Cloudflare environment with optional AI binding
 * @param content - Worksheet content to check (title, subtitle, description)
 * @param model - Optional AI model to use (defaults to Llama Guard 3)
 * @returns SafetyCheckResult with classification and confidence
 */
export async function checkContentSafetyWithAI(
  env: { AI?: unknown },
  content: {
    title: string;
    subtitle?: string;
    description?: string;
  },
  model: string = AI_SAFETY_CONFIG.DEFAULT_MODEL,
): Promise<SafetyCheckResult> {
  const textToCheck = [
    content.title,
    content.subtitle || "",
    content.description || "",
  ]
    .filter(Boolean)
    .join("\n");

  // Check if AI is available
  if (!env.AI) {
    console.warn("⚠️ Workers AI not available, using bad-words fallback");
    return fallbackToBadWords(textToCheck);
  }

  try {
    const prompt = `You are a strict content safety classifier for children's educational materials (K-2 grade level, ages 5-8).

STRICT RULES - ALL trigger UNSAFE classification:
1. PROFANITY & CURSE WORDS: "damn", "hell", "ass", "crap", "piss", "sucks", "butt", etc.
2. NEGATIVE SENTIMENT: "hate", "stupid", "dumb", "loser", "idiot", "worthless", "suck", "fail"
3. BULLYING & NAME-CALLING: "wimp", "nerd", "fat", "ugly", "mean", insults, mockery, teasing
4. SELF-HARM & MENTAL HEALTH: "kill myself", "cut myself", "depressed", "suicidal", "die"
5. BODY-SHAMING: Comments about appearance, weight, looks, physical attributes
6. DEATH & MORBID: "death", "dead", "kill", "murder", "die", "coffin", etc.
7. EXCLUSIONARY LANGUAGE: "everyone except", "people like you", "you don't belong", discrimination
8. CRUDE/SEXUAL LANGUAGE: Any sexual references, reproductive terms used inappropriately
9. VIOLENCE & WEAPONS: "hit", "punch", "gun", "knife", "shoot", weapons, fighting
10. DRUGS & ALCOHOL: References to drugs, alcohol, smoking, vaping
11. GAMBLING: "bet", "gamble", "money bet", betting language
12. ADULT THEMES: Romance, dating, flirting, mature relationships
13. SCARY CONTENT: Horror, nightmares, monsters, ghosts, scary stories
14. DISMISSIVE LANGUAGE: Comments that discourage learning ("too hard", "you can't do this", "give up")

Analyze this worksheet content:
TITLE: "${content.title}"
${content.subtitle ? `SUBTITLE: "${content.subtitle}"` : ""}
${content.description ? `DESCRIPTION: "${content.description}"` : ""}

Classification for K-2 children (ages 5-8):
- If ANY rule above is violated → classification = "unsafe"
- If content is clean and educational → classification = "safe"

Respond ONLY with valid JSON (no markdown, no code blocks):
{
  "classification": "safe" or "unsafe",
  "categories": ["category1", "category2"],
  "explanation": "brief explanation"
}`;

    const aiInstance = env.AI as {
      run: (model: string, options: unknown) => Promise<AIResponse | string>;
    };

    const response = await aiInstance.run(model, {
      messages: [
        {
          role: "user",
          content: prompt,
        },
      ],
    });

    // Log raw response for debugging safety issues
    console.log(
      "[AI] Raw response:",
      JSON.stringify(response).substring(0, 500),
    );

    // Parse response - handle various formats
    let result;
    if (typeof response === "string") {
      // Try parsing as JSON first
      try {
        result = JSON.parse(response);
      } catch {
        // If not JSON, treat as plain text response (e.g., just "safe" or "unsafe")
        const text = response.trim().toLowerCase();
        result = {
          classification: text.includes("unsafe") ? "unsafe" : "safe",
          categories: text.includes("unsafe") ? ["unknown"] : [],
          explanation: text,
        };
      }
    } else if (response && typeof response === "object") {
      // Extract text from response object
      const text =
        (response as AIResponse).response ||
        (response as AIResponse).result ||
        (response as AIResponse).message ||
        JSON.stringify(response);

      // Try parsing as JSON first
      try {
        result = JSON.parse(text);
      } catch {
        // If not JSON, treat as plain text response
        const cleanText = text.trim().toLowerCase();
        result = {
          classification: cleanText.includes("unsafe") ? "unsafe" : "safe",
          categories: cleanText.includes("unsafe") ? ["unknown"] : [],
          explanation: cleanText,
        };
      }
    } else {
      result = response;
    }

    // Validate result structure
    if (!result.classification) {
      console.warn("Invalid AI response structure, falling back to bad-words");
      return fallbackToBadWords(textToCheck);
    }

    return {
      safe: result.classification === "safe",
      classification: result.classification,
      categories: result.categories || [],
      explanation: result.explanation,
      confidence: 0.95, // Llama Guard 3 is highly confident
      usingAI: true,
      fallback: false,
    };
  } catch (error: unknown) {
    const errorMessage = error instanceof Error ? error.message : String(error);

    // Check if it's a rate limit or free tier exceeded error
    if (
      errorMessage.includes("rate limit") ||
      errorMessage.includes("429") ||
      errorMessage.includes("quota") ||
      errorMessage.includes("limit exceeded") ||
      errorMessage.includes("exceeded") ||
      errorMessage.includes("overloaded")
    ) {
      console.warn(
        "⚠️ Workers AI free tier quota exceeded, falling back to bad-words",
      );
    } else if (
      errorMessage.includes("unavailable") ||
      errorMessage.includes("timeout")
    ) {
      console.warn(
        "⚠️ Workers AI unavailable/timeout, falling back to bad-words",
      );
    } else {
      console.warn(
        `⚠️ AI safety check failed: ${errorMessage}, using bad-words fallback`,
      );
    }

    // Fallback to bad-words
    return fallbackToBadWords(textToCheck);
  }
}

/**
 * Fallback to bad-words library when AI is unavailable
 * @param text - Combined text to check for profanity
 * @returns SafetyCheckResult using bad-words classification
 */
export function fallbackToBadWords(text: string): SafetyCheckResult {
  try {
    const filter = new Filter();
    const hasProfanity = filter.isProfane(text.toLowerCase());

    return {
      safe: !hasProfanity,
      classification: hasProfanity ? "unsafe" : "safe",
      categories: hasProfanity ? ["profanity"] : [],
      explanation: hasProfanity
        ? "Content contains inappropriate language"
        : "Content is appropriate for children",
      confidence: 0.75, // Lower confidence than AI
      usingAI: false,
      fallback: true,
    };
  } catch (error) {
    console.error("Bad-words fallback failed:", error);
    // Last resort: assume safe if everything fails
    return {
      safe: true,
      classification: "safe",
      categories: [],
      explanation: "Safety check unavailable, content allowed by default",
      confidence: 0.0,
      usingAI: false,
      fallback: true,
    };
  }
}

/**
 * Check if content contains profanity using bad-words (lightweight check)
 * Used for quick pre-filtering before AI check
 * @param text - Text to check
 * @returns true if profanity detected, false otherwise
 */
export function containsProfanity(text: string): boolean {
  try {
    const filter = new Filter();
    return filter.isProfane(text.toLowerCase());
  } catch (error) {
    console.warn("Profanity check failed:", error);
    return false;
  }
}
