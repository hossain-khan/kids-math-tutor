# Cloudflare KV Setup for Shared Worksheets API

This document explains how to properly set up Cloudflare KV storage for the Shared Worksheets API.

## Overview

The Hono API backend uses Cloudflare KV to store shared worksheets, rate limiting data, and statistics. Two KV namespaces are needed:
- **Production**: For live worksheets
- **Preview**: For local development and testing

## Setup Steps

### 1. Create KV Namespaces in Cloudflare Dashboard

1. Log in to [Cloudflare Dashboard](https://dash.cloudflare.com)
2. Go to **Workers & Pages** > **KV**
3. Click **Create a namespace**
4. Create two namespaces:
   - Name: `worksheets-storage` (for production)
   - Name: `worksheets-storage-preview` (for local testing)

### 2. Get Namespace IDs

After creating the namespaces:
1. Click on each namespace
2. Copy the **Namespace ID** (appears at the top, looks like: `abc123def456789abc123def456789ab`)
3. Keep these IDs for the next step

### 3. Update wrangler.json

In `/webapp/wrangler.json`, the KV namespace is configured:

```json
"kv_namespaces": [
  {
    "binding": "KV",
    "id": "1a434626c9c143ec874e6c2e1ec2348a",        // Production: worksheets-storage
    "preview_id": "b94eac3c955f44df8eb8722850956947"   // Preview: for local dev
  }
]
```

> **Note**: These IDs are already configured. Only update if creating new namespaces.

### 4. KV Usage by Environment

- **Local Development** (`wrangler dev` / `pnpm run worker:dev`):
  - Runs on `localhost:8787`
  - Uses **preview KV** namespace (`preview_id`)
  - Data is separate from production

- **Production** (`npx wrangler deploy`):
  - Deploys to `math-worksheet.gohk.xyz`
  - Uses **production KV** namespace (`id`)
  - Data persists across deployments

## Local Development

### Test the Worker Locally

```bash
# Install dependencies
pnpm install

# Start local worker (uses preview KV namespace)
pnpm run worker:dev
```

This starts:
- React app on `http://localhost:5173`
- Hono API on `http://localhost:8787`
- Connected to preview KV namespace

### Test API Endpoints

```bash
# Test share endpoint
curl -X POST http://localhost:8787/api/v1/worksheets/share \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Test Worksheet",
    "problems": [
      {"operand1": 2, "operand2": 3, "operation": "addition"}
    ]
  }'

# List worksheets
curl http://localhost:8787/api/v1/worksheets?grades=kindergarten&sort=newest
```

## Production Deployment

### Prerequisites

- Cloudflare account with `math-worksheet.gohk.xyz` configured
- KV namespaces created (already configured in `wrangler.json`)

### Deploy

```bash
# Build and deploy
pnpm build && npx wrangler deploy
```

This:
1. Builds React app to `/dist`
2. Deploys Hono worker + static assets to Cloudflare
3. API routes (`/api/*`) handled by Hono worker
4. SPA routes served with fallback to `index.html`

**Live URL**: https://math-worksheet.gohk.xyz/

## Data Model in KV

### Worksheet Storage
**Key**: `worksheet:<id>`
**Value**: JSON-serialized `SharedWorksheet`

```typescript
{
  id: "abc123def456",
  type: "explicit",
  title: "Addition Practice",
  grades: ["kindergarten"],
  problems: [/* ProblemSpec[] */],
  createdAt: "2025-12-26T10:30:00Z",
  stats: {
    views: 42,
    downloads: 8
  }
}
```

### Rate Limiting
**Key**: `ratelimit:<ip>:<date>`
**Value**: `<share_count>`
**TTL**: 1 day (expires automatically)

```
ratelimit:192.168.1.1:2025-12-26 → "5"
```

## Troubleshooting

### "KV Namespace not found" Error
- Verify namespace IDs in `wrangler.json` are correct
- Check that namespaces exist in Cloudflare Dashboard
- For local dev, use `wrangler dev` (uses preview namespace)

### Data Not Persisting Across Requests
- Local preview KV is ephemeral (resets on each dev server restart)
- Use production namespaces for persistent data
- Production KV data persists across deployments

### CORS Issues with API
- Hono automatically handles CORS for simple requests
- Complex requests need explicit CORS handling
- Check browser console for specific CORS errors

## Quick Reference

| Environment | Command | KV Used | URL |
|-------------|---------|---------|-----|
| Local Dev | `pnpm run worker:dev` | Preview | http://localhost:8787 |
| Production | `npx wrangler deploy` | Production | https://math-worksheet.gohk.xyz |

## Resources

- [Cloudflare KV Documentation](https://developers.cloudflare.com/kv/)
- [Wrangler Configuration Reference](https://developers.cloudflare.com/workers/wrangler/configuration/)
- [Hono on Cloudflare Workers](https://hono.dev/docs/getting-started/cloudflare-workers)
