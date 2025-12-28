# Content Safety with Cloudflare Workers AI

This document explains how content safety is implemented in the Worksheet Generator using Cloudflare Workers AI (Llama 3.1 8B) with automatic fallback to pattern-based filtering and a dedicated testing tool.

## Overview

The app uses a **layered safety approach** to protect K-2 children from inappropriate content:

1. **Primary**: AI-based content classification using Llama 3.1 8B (fast, reliable JSON responses)
2. **Fallback**: Fast pattern-based profanity filtering using bad-words library
3. **Testing**: Dedicated Safety Content Validator tool for testing and iterating on safety rules
4. **Result**: Age-appropriate content guaranteed with zero cost (within free tier)

## Free Tier Constraints

**Cloudflare Workers AI Free Tier Limits:**
- **Daily Budget**: 10,000 Neurons per day (reset daily at midnight UTC)
- **Cost**: $0 (completely free)
- **Per Check**: ~9-12 neurons (depending on content length)
- **Daily Capacity**: ~850-1,000 worksheet safety checks per day

**Example Cost Breakdown:**
- 100 worksheets/day = ~1,000 neurons/day = 10% of daily budget
- 850 worksheets/day = ~8,500 neurons/day = 85% of daily budget
- 1,000+ worksheets/day = automatic fallback to bad-words

## Implementation Details

### Safety Check Flow

```
User submits worksheet content
                ↓
Is AI binding available?
  ├─ YES → Call Llama Guard 3 model
  │        ├─ Content unsafe? → REJECT with categories
  │        ├─ Content safe? → ACCEPT
  │        └─ Error/Timeout/RateLimit? → Fall back to bad-words
  │
  └─ NO → Use bad-words pattern-based filter
           ├─ Profanity detected? → REJECT
           └─ Clean? → ACCEPT
```

### AI Model Configuration

The app uses a configurable AI model system with Llama 3.1 8B as default:

```typescript
export const AI_SAFETY_CONFIG = {
  // Primary model: Llama 3.1 8B (fast, reliable JSON responses)
  DEFAULT_MODEL: "@cf/meta/llama-3.1-8b-instruct-fast",
  // Alternative models for testing
  ALTERNATIVE_MODELS: [
    "@cf/meta/llama-guard-3-8b",
    "@cf/meta/llama-3.3-70b-instruct-fp8-fast",
    "@cf/meta/llama-4-scout-17b-16e-instruct",
  ],
} as const;
```

### Content Safety Rules (14 Categories)

The AI model strictly evaluates content against 14 safety categories for K-2 children:

1. **Profanity & Curse Words**: "damn", "hell", "ass", "crap", "piss", "sucks", "butt"
2. **Negative Sentiment**: "hate", "stupid", "dumb", "loser", "idiot", "worthless", "fail"
3. **Bullying & Name-Calling**: "wimp", "nerd", "fat", "ugly", "mean", insults, mockery
4. **Self-Harm & Mental Health**: "kill myself", "cut myself", "depressed", "suicidal"
5. **Body-Shaming**: Comments about appearance, weight, looks, physical attributes
6. **Death & Morbid**: "death", "dead", "kill", "murder", "die", "coffin"
7. **Exclusionary Language**: "everyone except", "you don't belong", discrimination
8. **Crude/Sexual Language**: Sexual references, reproductive terms used inappropriately
9. **Violence & Weapons**: "hit", "punch", "gun", "knife", "shoot", weapons, fighting
10. **Drugs & Alcohol**: References to drugs, alcohol, smoking, vaping
11. **Gambling**: "bet", "gamble", "money bet", betting language
12. **Adult Themes**: Romance, dating, flirting, mature relationships
13. **Scary Content**: Horror, nightmares, monsters, ghosts, scary stories
14. **Dismissive Language**: "too hard", "you can't do this", "give up" (discourages learning)

**Example Response:**
```json
{
  "safe": false,
  "categories": ["violence", "profanity"],
  "explanation": "Content contains references to violence and profanity",
  "confidence": 0.95,
  "usingAI": true,
  "fallback": false
}
```

### Bad-Words Fallback

When AI is unavailable or free tier quota exceeded:

- Quick pattern-based check (~1ms)
- Detects common profanity
- Limited context awareness (can't detect violence, sexual references, etc.)
- Confidence score: 0.75 (vs 0.95 for AI)

**Fallback Response:**
```json
{
  "safe": false,
  "categories": ["profanity"],
  "explanation": "Content contains profanity or inappropriate language",
  "confidence": 0.75,
  "usingAI": false,
  "fallback": true
}
```

## Implementation Code

### Module: `src/lib/server/aiSafety.ts`

The module exports:

```typescript
// Main function - handles AI + fallback logic
export async function checkContentSafetyWithAI(
  env: { AI?: unknown },
  content: { title: string; subtitle?: string; description?: string },
  model: string = AI_SAFETY_CONFIG.DEFAULT_MODEL
): Promise<SafetyCheckResult>

// Fallback function - uses bad-words library
export function fallbackToBadWords(text: string): SafetyCheckResult

// Type definition
export interface SafetyCheckResult {
  safe: boolean
  categories?: string[]
  explanation?: string
  confidence: number
  usingAI: boolean
  fallback: boolean
}

// Configuration constant
export const AI_SAFETY_CONFIG = {
  DEFAULT_MODEL: "@cf/meta/llama-3.1-8b-instruct-fast",
  ALTERNATIVE_MODELS: [/* ... */]
}
```

### Integration: `workers/api.ts`

Used in POST `/api/v1/worksheets/share` endpoint:

```typescript
// Check for inappropriate content using AI with fallback to bad-words
const safetyResult = await checkContentSafetyWithAI(c.env, {
  title: worksheetData.title,
  subtitle: worksheetData.subtitle,
  description: worksheetData.description,
});

if (!safetyResult.safe) {
  return c.json({
    error: 'Worksheet contains inappropriate content for children. Please review and try again.',
    categories: safetyResult.categories || [],
    explanation: safetyResult.explanation,
    suggestion: 'Please ensure all content is age-appropriate for K-2 children.',
    method: safetyResult.usingAI ? 'AI-based safety check' : 'Pattern-based filter',
    fallback: safetyResult.fallback || false,
  }, 400);
}
```

## Safety Content Validator Tool

### Purpose

Dedicated web page for testing and iterating on content safety rules without creating full worksheets. Enables rapid testing of different AI models and safety rules.

### Location & Access

- **URL**: `/test/validate-content`
- **Access**: Available in development and production environments
- **Authentication**: None required (testing tool)

### Features

**Input Section:**
- **Title Input**: Main content to validate
- **Subtitle Input**: Optional secondary content
- **Model Selector Dropdown**: Choose from AI_SAFETY_CONFIG models:
  - Default: Llama 3.1 8B (recommended)
  - Llama Guard 3 8B (specialized safety model)
  - Llama 3.3 70B (larger model)
  - Llama 4 Scout 17B (scout model)

**Validation Results:**
- **Classification**: SAFE or UNSAFE status with color coding
- **Categories**: List of detected safety violations
- **Confidence**: Confidence score (0.95 for AI, 0.75 for fallback)
- **Explanation**: Human-readable reason for rejection
- **Model Used**: Indicates which model was used (AI or fallback)

### API Endpoint

Behind the scenes, the Safety Validator uses the API endpoint:

```http
POST /api/v1/test/validate-content
```

**Request:**
```json
{
  "title": "Fun Addition Practice",
  "subtitle": "Add two numbers",
  "model": "@cf/meta/llama-3.1-8b-instruct-fast"
}
```

**Response (Safe Content):**
```json
{
  "safe": true,
  "confidence": 0.95,
  "usingAI": true
}
```

**Response (Unsafe Content):**
```json
{
  "safe": false,
  "categories": [
    "NEGATIVE_SENTIMENT",
    "BODY_SHAMING",
    "DEATH_MORBID"
  ],
  "explanation": "Content contains negative sentiment and demeaning language",
  "confidence": 0.95,
  "usingAI": true
}
```

### Usage Example

1. Navigate to `/test/validate-content`
2. Enter test content in Title field (e.g., "I hate everyone i hate math")
3. Optionally add Subtitle
4. Select AI model from dropdown (or use default Llama 3.1 8B)
5. Click "Validate Content" button
6. View instant results with categorization and confidence
7. Iterate on safety rules based on results

## Configuration

### Cloudflare Workers

Update `wrangler.json` to enable AI binding:

```json
{
  "env": {
    "production": {
      "bindings": [
        {
          "binding": "AI",
          "type": "ai"
        }
      ]
    },
    "development": {
      "bindings": [
        {
          "binding": "AI",
          "type": "ai"
        }
      ]
    }
  }
}
```

**Requirements:**
- Cloudflare Workers Paid Plan ($10/month) OR
- Free Plan with Workers AI enabled

**Models Available:**
- `@cf/meta/llama-guard-3-8b` - Recommended (8B parameters, balance of speed/accuracy)
- Other Llama Guard variants available (3B, 70B)

### Environment Variables

```typescript
// In Env interface
interface Env {
  AI?: unknown;  // Optional AI binding (graceful fallback if missing)
  KV: KVNamespace;
  ADMIN_PASSWORD?: string;
}
```

## Monitoring & Observability

### Logging

Check function logs to monitor:

1. **AI usage**: How many checks used AI vs fallback
2. **Error rates**: When/why AI fails
3. **Neuron consumption**: Track daily quota usage

```typescript
// Errors logged to console
console.error('[Content Safety] AI check failed, falling back to bad-words', {
  error: error.message,
  code: error.code,
});
```

### Metrics to Track

- Daily AI checks: ~850 = 85% utilization
- Daily AI errors: Should be <5% (timeouts, failures)
- Daily fallback activations: Trend indicates quota pressure
- False positives: Users reporting legitimate content rejected
- False negatives: Inappropriate content not caught

### Alert Thresholds

- **Warning**: >80% daily neuron budget used
- **Critical**: >95% daily neuron budget used (will hit quota)
- **Action Needed**: >10% of daily checks failing (increase fallback dependency)

## Cost Analysis

### Pricing

**Cloudflare Workers AI Costs:**
- Input: $0.484 per 1M tokens
- Output: $0.030 per 1M tokens
- Neurons: ~44,003 per 1M input tokens

**Example Scenarios:**

| Scenario | Worksheets/Day | Neurons/Day | Cost/Month |
|----------|---|---|---|
| Light (50 worksheets) | 50 | 500-600 | $0.15-0.18 |
| Medium (200 worksheets) | 200 | 2,000-2,400 | $0.60-0.72 |
| Heavy (850 worksheets) | 850 | 8,500-10,200 | $2.55-3.06 |
| Peak (1,000 worksheets) | 1,000 | Hits free tier limit | $3.00-3.60 |

**Recommendation:** Keep below 850 worksheets/day to stay within free tier (10,000 neurons) to maintain $0 cost.

## Testing

### Unit Tests

Comprehensive test suite in `src/__tests__/lib/server/aiSafety.test.ts`:

```bash
npm run test -- aiSafety.test.ts
```

**Test Coverage:**
- ✅ AI available and working
- ✅ AI unavailable (graceful fallback)
- ✅ AI timeout (fallback)
- ✅ Rate limit exceeded (fallback)
- ✅ Bad-words detects profanity
- ✅ Response parsing errors
- ✅ Edge cases (empty text, long text, unicode)

### Local Development

In development environment:
- AI binding available in `wrangler.json`
- Runs against Cloudflare's local AI workers

```bash
# Run locally
npm run dev

# Test endpoint
curl -X POST http://localhost:8787/api/v1/worksheets/share \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Math Practice",
    "subtitle": "Addition",
    "description": "Learn to add",
    "problems": [...]
  }'
```

## Troubleshooting

### "Worksheet contains inappropriate content" - But content is fine

**Possible causes:**
1. **AI false positive**: Llama Guard 3 flagged legitimate content
2. **Bad-words false positive**: Content triggered pattern matching
3. **Prompt interpretation**: Model misunderstood context

**Solution:**
- Check `method` field in error response
- If `usingAI: true`: Content was flagged by Llama Guard 3
- If `fallback: true`: Triggered bad-words library
- Rephrase content to avoid triggering words

**Examples:**
- ❌ "Defeat the enemy" → ✅ "Beat the challenger"
- ❌ "Attack the problem" → ✅ "Solve the problem"
- ❌ "Kill the timer" → ✅ "Stop the timer"

### Rate Limit Errors (429)

**Cause:** Free tier daily quota exceeded (10,000 neurons/day)

**Solution:**
1. All safety checks fall back to bad-words (automatic)
2. Users can still create worksheets (less advanced filtering)
3. Quota resets at midnight UTC
4. Plan to upgrade to Paid plan if exceeding 850 worksheets/day

### AI Binding Missing

**Cause:** `AI` binding not configured in `wrangler.json`

**Solution:**
```bash
# Update wrangler.json to include AI binding
# Then redeploy:
npx wrangler deploy
```

### Tests Failing

**Common issues:**
1. **bad-words not installed**: Run `npm install`
2. **vitest not found**: Run `npm install --save-dev vitest`
3. **Wrong import paths**: Verify `@/lib/server/aiSafety` resolves

```bash
npm install bad-words
npm install --save-dev vitest
npm run test
```

## Security Considerations

### Prompt Injection

Llama Guard 3 is designed to resist prompt injection attacks. However:
- Worksheet content is treated as data, not instructions
- Prompt is structured to prevent prompt injection
- Model is trained on adversarial examples

### PII Protection

The safety check:
- Does NOT store worksheet content
- Does NOT log sensitive information
- Does NOT track user identity
- Results stored only briefly during request

### Compliance

- **COPPA**: AI check ensures content is age-appropriate (K-2 = 5-8 years old)
- **GDPR**: No personal data collected during safety check
- **Privacy**: Content not retained after check completes

## References

- [Llama Guard 3 Model Card](https://huggingface.co/meta-llama/LlamaGuard-3-8B)
- [Cloudflare Workers AI Docs](https://developers.cloudflare.com/workers-ai/)
- [bad-words Library](https://www.npmjs.com/package/bad-words)
- [COPPA Compliance Guide](https://www.ftc.gov/business-guidance/privacy-security/childrens-privacy)

## Frequently Asked Questions

**Q: Will content get rejected more often when quota is exceeded?**
A: No, fallback to bad-words is automatic and transparent. Most worksheets will still be accepted (unless they contain obvious profanity).

**Q: Can we use a different safety model?**
A: Yes, Cloudflare Workers AI supports other models. Update the model name in `checkContentSafetyWithAI()` function and adjust the prompt accordingly.

**Q: What about non-English content?**
A: Llama Guard 3 supports multiple languages. The prompt structure remains the same. bad-words library supports English primarily.

**Q: How can we improve false positive rate?**
A: 
1. Fine-tune the prompt in `checkContentSafetyWithAI()`
2. Add custom bad-words patterns
3. Review flagged content and adjust thresholds
4. Consider upgrading to Llama Guard 70B for better accuracy

**Q: Is the AI check COPPA compliant?**
A: Yes, the check is designed specifically for K-2 age appropriateness (5-8 years old), which is COPPA's target demographic. All checks are done server-side without storing content.
