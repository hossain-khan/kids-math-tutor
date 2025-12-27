# Shared Worksheets Community Library

## Overview

The Shared Worksheets Community Library is a feature that allows users to share custom math worksheets with the community and browse/use worksheets created by other users. This feature enables collaborative learning and worksheet discovery across the Kids Math Pup Tutor platform.

**Status:** Phase 1 - Complete (Basic sharing, browsing, and usage tracking)

## Architecture

### Tech Stack

- **Backend:** Cloudflare Workers + Hono 4.6.3
- **Storage:** Cloudflare KV (Key-Value Store)
- **Frontend:** React 18 + TypeScript + React Router v6
- **Validation:** Zod schemas
- **Content Moderation:** bad-words 4.0.0
- **Unique IDs:** nanoid 5.1.6

### High-Level Flow

```
User Creates Worksheet
  ↓
Share to Community Button
  ↓
POST /api/v1/worksheets/share (Hono API)
  ↓
Validate → Profanity Check → Grade Detection → Rate Limit Check
  ↓
Save to KV + Return Share Link
  ↓
User Gets Shareable Link
  ↓
Users Browse at /worksheets
  ↓
View Detail & Use Worksheet
  ↓
Generate Deeplink (Android) or Copy Link (Desktop)
```

## API Endpoints

### 1. POST /api/v1/worksheets/share

**Purpose:** Create and share a custom worksheet to the community

**Request Body:**
```typescript
{
  title: string;              // Required, max 100 chars
  subtitle?: string;          // Optional, max 200 chars
  description?: string;       // Optional, max 500 chars
  problems: ProblemSpec[];    // Required, min 1 problem
}
```

**Response (201):**
```typescript
{
  id: string;                 // Unique worksheet ID (nanoid-12)
  shareUrl: string;          // Relative path: /worksheets/{id}
  shareLink: string;         // Full URL: https://math-worksheet.gohk.xyz/worksheets/{id}
}
```

**Error Responses:**
- `400` - Invalid worksheet structure or data format
- `400` - Contains inappropriate content (profanity filter)
- `429` - Rate limit exceeded (10 shares per day per IP)
- `500` - Server error

**Business Logic:**
1. Validates worksheet structure (title required, min 1 problem)
2. Validates against ExplicitChallengeSpec schema
3. Checks for profanity in title, subtitle, description
4. Auto-detects grade levels (K, 1st, 2nd) from problem operands
5. Checks rate limit (10 shares/day per IP, tracked via KV)
6. Generates unique ID (nanoid-12)
7. Saves to KV with `worksheet:<id>` key
8. Increments share count for rate limiting
9. Returns shareable link

### 2. GET /api/v1/worksheets

**Purpose:** List shared worksheets with filtering and sorting

**Query Parameters:**
```
grades=K,1,2        // Optional, comma-separated grade levels (K, 1, 2)
sort=newest|views|downloads  // Optional, default: newest
```

**Response (200):**
```typescript
WorksheetListItem[] = [
  {
    id: string;
    title: string;
    subtitle?: string;
    grades: GradeLevel[];
    problemCount: number;
    createdAt: string;        // ISO 8601 timestamp
    stats: {
      views: number;
      downloads: number;
    }
  },
  ...
]
```

**Error Responses:**
- `500` - Server error

**Business Logic:**
1. Fetches all worksheets from KV
2. Filters by selected grades (if provided)
3. Sorts by requested criteria (newest, views, downloads)
4. Returns summary list (problems not included)

### 3. GET /api/v1/worksheets/:id

**Purpose:** Fetch a single worksheet with all details and auto-increment views

**Response (200):**
```typescript
{
  id: string;
  type: 'explicit';
  title: string;
  subtitle?: string;
  description?: string;
  grades: GradeLevel[];
  problems: ProblemSpec[];     // Full problem details
  createdAt: string;           // ISO 8601 timestamp
  stats: {
    views: number;
    downloads: number;
  }
}
```

**Error Responses:**
- `404` - Worksheet not found
- `500` - Server error

**Business Logic:**
1. Fetches full worksheet from KV
2. **Increments view counter** asynchronously (awaited to ensure count updates before response)
3. Refetches updated worksheet to return current stats
4. Returns complete worksheet with all problems

### 4. POST /api/v1/worksheets/:id/download

**Purpose:** Track when a worksheet is used/downloaded

**Response (200):**
```typescript
{
  success: boolean;
  stats: {
    views: number;
    downloads: number;
  }
}
```

**Error Responses:**
- `404` - Worksheet not found
- `500` - Server error

**Business Logic:**
1. Finds worksheet in KV
2. Increments download counter
3. Updates worksheet in KV
4. Returns updated stats

## Data Models

### SharedWorksheet (Full Record)

```typescript
interface SharedWorksheet {
  id: string;                    // nanoid-12
  type: 'explicit';              // Currently only explicit type
  title: string;                 // User-provided title
  subtitle?: string;             // Optional subtitle
  description?: string;          // Optional description
  grades: GradeLevel[];          // Auto-detected: K, 1, 2
  problems: ProblemSpec[];       // Full problem array
  createdAt: string;             // ISO 8601 timestamp
  stats: {
    views: number;               // View counter
    downloads: number;           // Download/usage counter
  }
}
```

### WorksheetListItem (Summary)

```typescript
interface WorksheetListItem {
  id: string;
  title: string;
  subtitle?: string;
  grades: GradeLevel[];
  problemCount: number;
  createdAt: string;
  stats: {
    views: number;
    downloads: number;
  }
}
```

### GradeLevel

```typescript
type GradeLevel = 'K' | '1' | '2';
```

### ProblemSpec

```typescript
interface ProblemSpec {
  operand1: number;
  operand2: number;
  operation: '+' | '-' | '*' | '/';
}
```

## KV Storage Structure

### Worksheet Storage

**Key:** `worksheet:<id>`  
**Value:** JSON-serialized `SharedWorksheet`  
**TTL:** None (persistent)

```
worksheet:abc123def456 → {
  id: "abc123def456",
  type: "explicit",
  title: "Addition Basics",
  ...
  stats: { views: 5, downloads: 2 }
}
```

### Rate Limiting

**Key:** `ratelimit:<ip>:<date>`  
**Value:** Share count (number)  
**TTL:** 86400 seconds (24 hours)

```
ratelimit:192.168.1.1:2025-12-26 → 3
```

When TTL expires, the key is automatically deleted, resetting the daily quota.

## Frontend Components

### Sharing Feature (Result.tsx)

Located in `/src/pages/Result.tsx`

**Component:** "Share to Community" button
- **Placement:** Result page after worksheet is generated
- **Visibility:** Only for custom (not template-based) worksheets
- **States:** Default, Loading, Success, Error
- **Actions:**
  - Click to open share dialog or inline share
  - Displays share link on success
  - Shows error message on failure

**Integration:**
```typescript
const handleShareToCommunity = async () => {
  const response = await fetch('/api/v1/worksheets/share', {
    method: 'POST',
    body: JSON.stringify({
      title: worksheet.title,
      subtitle: worksheet.subtitle,
      description: worksheet.description,
      problems: worksheet.problems,
    }),
  });
  // Handle response and show share link
};
```

### Community Library List (SharedWorksheets.tsx - List View)

Located in `/src/pages/SharedWorksheets.tsx`

**Features:**
- Grid layout of worksheet cards
- Grade filter (multi-select: K, 1st, 2nd)
- Sorting: Newest, Most Viewed, Most Downloaded
- Problem preview (first 3 problems shown)
- Metadata display: Problem count, Views, Downloads
- Click to view detail

**Filters:**
- Grade selection stored in URL query params (`?grades=K,1,2`)
- Sort option stored in URL query params (`?sort=newest|views|downloads`)
- Applied when fetching from GET `/api/v1/worksheets`

### Community Library Detail (SharedWorksheets.tsx - Detail View)

Located in `/src/pages/SharedWorksheets.tsx`

**Features:**
- Full worksheet title, subtitle, description
- Problem preview (first 3 problems + count of remaining)
- Complete metadata
- Action buttons:
  - "Use This Worksheet" - Generates deeplink
  - "Browse More" - Return to list view
- Android-only note for non-mobile users

**Deeplink Generation:**
```typescript
const deeplink = `mathpup://worksheet/${worksheetId}`;
window.location.href = deeplink;
```

- **Android Behavior:** App installed and configured to handle `mathpup://` scheme opens the app with worksheet data
- **Desktop Behavior:** Shows system dialog or allows copy of link

## Server Utilities

### Profanity Filtering (lib/server/profanity.ts)

**Library:** bad-words 4.0.0

```typescript
function validateWorksheetContent(content: {
  title: string;
  subtitle?: string;
  description?: string;
}): boolean {
  // Returns true if profanity detected
}
```

**Approach:** 
- Uses industry-standard bad-words filter
- Checks title, subtitle, and description
- Blocks submission if inappropriate content detected
- Returns user-friendly error message

### Grade Detection (lib/server/grades.ts)

**Algorithm:** Analyzes maximum operand value

```typescript
function detectGrades(problems: ProblemSpec[]): GradeLevel[] {
  // K: max operand 0-10
  // 1st: max operand 1-20
  // 2nd: max operand 1-100
  // Returns array of applicable grades
}
```

**Logic:**
- Examines all operands in all problems
- Finds maximum operand value
- Maps to grade ranges:
  - **K (Kindergarten):** operands 0-10
  - **1st Grade:** operands 1-20
  - **2nd Grade:** operands 1-100

### Rate Limiter (lib/server/rateLimiter.ts)

**Strategy:** IP-based daily quotas

```typescript
interface RateLimitContext {
  env: { KV: KVNamespace };
  clientIp: string;
}

async function checkRateLimit(ctx: RateLimitContext): Promise<{
  allowed: boolean;
  remaining: number;
  resetTime: number;  // Unix timestamp
}>;

async function incrementShareCount(ctx: RateLimitContext): Promise<void>;
```

**Details:**
- Tracks shares per IP per day
- Limit: 10 shares per day
- Uses ISO 8601 date as key component
- TTL: 24 hours (86400 seconds)
- Automatic reset after 24 hours

**Flow:**
1. Get current count from KV for `ratelimit:<ip>:<date>`
2. If count < 10, allow
3. If count >= 10, reject with remaining=0
4. On success, increment count
5. KV key expires after 24 hours

### Worksheet Storage (lib/server/worksheetStorage.ts)

**Functions:**

```typescript
async function saveWorksheet(
  ctx: StorageContext,
  worksheet: SharedWorksheet
): Promise<void>;

async function getWorksheet(
  ctx: StorageContext,
  id: string
): Promise<SharedWorksheet | null>;

async function listWorksheets(
  ctx: StorageContext,
  options?: {
    grades?: GradeLevel[];
    sortBy?: 'newest' | 'views' | 'downloads';
  }
): Promise<WorksheetListItem[]>;

async function incrementViews(
  ctx: StorageContext,
  id: string
): Promise<void>;

async function incrementDownloads(
  ctx: StorageContext,
  id: string
): Promise<void>;
```

## Routing

### Frontend Routes

**List View:**
- **Path:** `/worksheets`
- **Component:** `SharedWorksheets` (list mode)
- **Query Params:** 
  - `grades=K,1,2` (optional, multi-select)
  - `sort=newest|views|downloads` (optional)
- **Data Source:** GET `/api/v1/worksheets?grades=...&sort=...`

**Detail View:**
- **Path:** `/worksheets/:id`
- **Component:** `SharedWorksheets` (detail mode)
- **Data Source:** GET `/api/v1/worksheets/:id`
- **Auto-loads** when component mounts with worksheet ID

### API Routes

**Local Development (localhost:8787):**
- `http://localhost:8787/api/v1/worksheets`
- `http://localhost:8787/api/v1/worksheets/share`
- `http://localhost:8787/api/v1/worksheets/{id}`
- `http://localhost:8787/api/v1/worksheets/{id}/download`

**Production (math-worksheet.gohk.xyz):**
- `https://math-worksheet.gohk.xyz/api/v1/worksheets`
- `https://math-worksheet.gohk.xyz/api/v1/worksheets/share`
- `https://math-worksheet.gohk.xyz/api/v1/worksheets/{id}`
- `https://math-worksheet.gohk.xyz/api/v1/worksheets/{id}/download`

**Cloudflare Workers Routing:**
- Development: `localhost:8787/api/*` → Worker
- Production: `math-worksheet.gohk.xyz/api/*` → Worker

## Deployment

### Configuration (wrangler.json)

```json
{
  "kv_namespaces": [
    {
      "binding": "KV",
      "id": "1a434626c9c143ec874e6c2e1ec2348a",        // Production
      "preview_id": "b94eac3c955f44df8eb8722850956947"  // Preview
    }
  ],
  "env": {
    "production": {
      "routes": [
        {
          "pattern": "math-worksheet.gohk.xyz/api/*",
          "zone_name": "math-worksheet.gohk.xyz"
        }
      ]
    }
  }
}
```

### KV Namespaces

- **Production ID:** `1a434626c9c143ec874e6c2e1ec2348a`
  - Used for published worksheets
  - Persistent storage
  - Live traffic

- **Preview ID:** `b94eac3c955f44df8eb8722850956947`
  - Used during local development and testing
  - Isolated from production

### Free Tier Limits

Cloudflare Workers KV free tier provides:
- **Read Operations:** 100,000 per day
- **Write Operations:** 10,000 per day
- **Storage:** 1 GB total
- **Sufficient for:** ~1,000-10,000 active worksheets depending on size

## Testing

### Test Coverage

**Existing Tests:**
- Deeplink generation: `/src/__tests__/lib/deeplink.test.ts`
- Result page sharing: `/src/__tests__/pages/Result.test.tsx`

**To Add (Phase 2):**
- API endpoint tests (unit tests for Hono routes)
- Profanity filter tests
- Grade detection tests
- Rate limiter tests
- SharedWorksheets component tests
- KV storage integration tests

### Build & Lint

```bash
# Build
pnpm run build
# Output: 356.42 kB gzipped

# Lint
pnpm run lint
# Status: 0 errors

# Tests
pnpm run test
# Status: 95 passing
```

## Security Considerations

1. **Rate Limiting:** IP-based daily quotas prevent abuse
2. **Content Moderation:** Profanity filter blocks inappropriate content
3. **Input Validation:** Zod schema validation on all API inputs
4. **Type Safety:** Full TypeScript coverage prevents runtime errors
5. **HTTPS Only:** All routes use HTTPS in production
6. **CORS:** API routes scoped to domain

## Future Enhancements (Phase 2+)

- **Search:** Keyword-based search across worksheets
- **Pagination:** Load more/infinite scroll for large datasets
- **Ratings:** User ratings and reviews of worksheets
- **QR Codes:** Generate QR codes for easy mobile sharing
- **User Profiles:** User profile pages with shared worksheets
- **Trending:** Community statistics and trending worksheets
- **Comments:** Discussion threads on worksheets
- **Favorites:** Save favorite worksheets
- **Analytics:** Detailed usage analytics per worksheet

## Troubleshooting

### Views Not Incrementing
- Ensure `/api/v1/worksheets/:id` GET endpoint is awaiting `incrementViews()`
- Verify KV namespace is correctly configured
- Check browser console for API errors

### Rate Limit Issues
- Rate limit is per IP per calendar day
- Check client IP detection (CF-Connecting-IP header)
- Test with different IP addresses

### Profanity Filter False Positives
- bad-words library may have limitations
- Consider custom word list in Phase 2

### Deeplink Not Working
- Verify Android app is installed and has `mathpup://` scheme registered
- Test on physical Android device, not emulator
- Check deeplink format: `mathpup://worksheet/{id}`

## Performance Notes

- KV operations are fast (~1-5ms average)
- Grade detection is O(n) where n = number of problems
- Profanity check is O(m) where m = text length
- Rate limiting check is single KV read/write
- Consider caching common worksheet lists if needed

## Related Files

- `/workers/api.ts` - Main API implementation
- `/workers/index.ts` - Worker entry point
- `/src/pages/Result.tsx` - Share button
- `/src/pages/SharedWorksheets.tsx` - Community library UI
- `/src/lib/server/*` - Server utilities
- `/webapp/wrangler.json` - Cloudflare configuration
