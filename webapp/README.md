# Math Pup Worksheet Creator - Web App

[![Webapp CI](https://github.com/hossain-khan/kids-math-tutor/actions/workflows/webapp.yml/badge.svg)](https://github.com/hossain-khan/kids-math-tutor/actions/workflows/webapp.yml)

Web-based worksheet creator for the Kids Math Pup Tutor Android app. Create custom math practice problems with instant validation and easy sharing.

## Features

- 🎨 **Fun, Child-Friendly Design** - Engaging interface with Math Pup mascot
- ⚡ **Two Creation Modes**:
  - Quick Generator: Rule-based automatic problem generation
  - Custom Problems: Full control over each problem
- ✅ **Instant Validation** - Real-time error checking matching app schema
- 📱 **Mobile Responsive** - Works perfectly on phones, tablets, and desktops
- 🚀 **Powered by Cloudflare** - Fast, global CDN delivery

## Tech Stack

- **Frontend**: React 18 + TypeScript + Vite
- **Styling**: Tailwind CSS 3.4
- **Validation**: Zod (type-safe schema validation)
- **State**: Zustand
- **Animations**: Framer Motion
- **Testing**: Vitest + React Testing Library
- **Backend**: Cloudflare Workers + Hono
- **Icons**: Lucide React

## Getting Started

### Prerequisites

- Node.js 18+ and pnpm
- Cloudflare account (for deployment)

### Installation

```bash
# Install dependencies
pnpm install
```

### Prerequisites for Development

Before running the development server, ensure you have the Cloudflare Workers CLI installed:

```bash
# Install wrangler (Cloudflare Workers CLI) globally
npm install -g wrangler

# Or use npx to run wrangler commands without global installation
# All wrangler commands in this project are run with: npx wrangler <command>
```

You can verify wrangler is installed:
```bash
wrangler --version
```

### Development

#### Local Development Server (Frontend + Backend)

To run the full development environment with both the frontend and API:

```bash
# Terminal 1: Start the Wrangler dev server (API on port 8787)
npx wrangler dev --env development

# Terminal 2: Start the Vite dev server (Frontend on port 5173)
pnpm dev

# Open http://localhost:5173
```

The Vite dev server is configured with a proxy that forwards `/api` requests to the Wrangler dev server on port 8787. This allows the frontend to communicate with the backend API locally.

**Important**: Both servers must be running simultaneously for full functionality.

#### Frontend-Only Development

If you only need to develop the frontend UI without API changes:

```bash
# Start Vite dev server
pnpm dev

# Open http://localhost:5173
```

**Note**: Pages requiring API calls (like the admin portal at `/manage-worksheets`) will not work without the Wrangler dev server running.

#### Linting and Testing

```bash
# Run linter
pnpm lint

# Format code
pnpm format

# Run tests
pnpm test

# Run tests with UI
pnpm test:ui

# Generate coverage report
pnpm test:coverage
```

### Test Coverage

The webapp has comprehensive test coverage for core functionality:
- **Utility functions** - String manipulation, clipboard operations, JSON handling
- **Zod schemas** - Challenge data validation for both generated and explicit modes
- **React components** - Form submission, validation, and navigation

Current coverage: ~70% overall
- Core validation logic: 90%+
- UI components: 50-90%

### Cloudflare Deployment

The webapp is deployed to Cloudflare Workers with static assets.

**Live URL**: https://math-worksheet.gohk.xyz/

#### Deployment Steps

```bash
# Login to Cloudflare (first time only)
npx wrangler login

# Build and deploy to production
pnpm build && npx wrangler deploy --env production
```

#### Environment Configuration

The deployment uses environment-specific configurations in `wrangler.json`:
- **Production**: Uses Cloudflare secrets for the admin password, but deploys to the **same worker instance** as the main deployment (not a separate `-production` worker)
- **Development**: Uses local password from `wrangler.json` for local testing

**⚠️ Important Configuration Detail**:
When using `npx wrangler deploy --env production`, Cloudflare automatically creates a new worker with a `-production` suffix if the `name` field is not explicitly set in the environment config. This can create a separate, unreachable worker instance without routes.

To prevent this, the `wrangler.json` must include `"name": "pup-tutor-worksheet-generator"` in the production environment section:

```json
"env": {
  "production": {
    "name": "pup-tutor-worksheet-generator",  // ← CRITICAL: Prevents -production suffix
    "kv_namespaces": [...],
    "vars": {"ADMIN_PASSWORD": "..."},
    "workers_dev": false
  }
}
```

Without this, the production deployment creates `pup-tutor-worksheet-generator-production` (a separate, unrouted worker) instead of updating the main `pup-tutor-worksheet-generator` worker that has the routes configured.

For production, set the admin password as a Cloudflare secret:
```bash
npx wrangler secret put ADMIN_PASSWORD --env production
```

## Project Structure

```
webapp/
├── src/
│   ├── components/    # React components
│   ├── lib/           # Utilities and schemas
│   ├── pages/         # Page components
│   ├── styles/        # Global styles
│   └── main.tsx       # Entry point
├── public/            # Static assets
├── dist/              # Build output (generated)
├── wrangler.json      # Cloudflare Workers config
└── vite.config.ts     # Vite configuration
```

## Deployment

**Production URL**: https://math-worksheet.gohk.xyz/

The webapp is deployed as a static SPA to Cloudflare Workers.

### Deploy Updates

```bash
# Build for production
pnpm build

# Deploy to Cloudflare Workers (production environment)
npx wrangler deploy --env production
```

### First-Time Setup

If you haven't deployed before:

```bash
# 1. Login to Cloudflare
npx wrangler login

# 2. Set the admin password as a Cloudflare secret
npx wrangler secret put ADMIN_PASSWORD --env production
# Enter your secure admin password when prompted

# 3. Build and deploy
pnpm build && npx wrangler deploy --env production
```

The configuration is in `wrangler.json`:
- Worker name: `pup-tutor-worksheet-generator`
- Static assets directory: `./dist`
- SPA mode enabled (all routes serve `index.html`)
- KV Namespace binding for storing shared worksheets
- Admin password from environment variables

### Admin Portal

Once deployed, access the admin portal at:
**https://math-worksheet.gohk.xyz/manage-worksheets**

Features:
- View all community-shared worksheets
- See worksheet statistics (views, downloads, ratings)
- Delete inappropriate or duplicate worksheets
- Password-protected access with 24-hour session tokens

See [ADMIN_SETUP.md](./ADMIN_SETUP.md) for complete admin portal documentation.

### View Deployment

After deployment, visit: https://math-worksheet.gohk.xyz/

### Troubleshooting Deployment Issues

#### Blank Page in Production

If you deploy successfully but see a blank page:

1. **Check Cloudflare Workers Dashboard**:
   - Ensure only ONE worker instance exists: `pup-tutor-worksheet-generator`
   - If a `-production` variant exists, **delete it** - it means the `name` field was missing from `wrangler.json`
   - Verify the worker has active routes (should show `math-worksheet.gohk.xyz`)

2. **Verify wrangler.json Configuration**:
   - The production environment MUST include: `"name": "pup-tutor-worksheet-generator"`
   - Without it, Cloudflare creates a separate, unrouted worker instance

3. **Hard Refresh Browser**:
   - Clear cache: DevTools → Application → Clear site data
   - Hard refresh: Cmd+Shift+R (macOS) or Ctrl+Shift+R (Windows/Linux)

4. **Check Browser Console**:
   - Open DevTools (F12) → Console tab
   - You should see initialization logs starting with 🚀
   - If blank, the JavaScript bundle isn't loading - check Network tab for failed asset requests

#### API Requests Return 404

If the admin portal loads but API calls fail:

1. **Local Development**: Ensure both servers are running:
   ```bash
   # Terminal 1
   wrangler dev --env development
   # Terminal 2
   pnpm dev
   ```
   - Vite proxies `/api` calls to port 8787 where Wrangler dev server runs

2. **Production**: The API routes should be handled by the Worker itself - no proxy needed

## JSON Schema

The web app generates JSON that matches the Android app's custom challenge schema exactly.

### Public JSON Schema

A JSON Schema (draft-07) specification is available for validation in the Android app:

**Schema URL**: https://math-worksheet.gohk.xyz/challenge-schema.json

This schema is auto-generated from the Zod validation schemas and provides:
- Type-safe validation for custom challenges
- Documentation of required fields and constraints
- Examples for both challenge types

To regenerate the schema after updating Zod schemas:

```bash
pnpm generate-schema
```

### Generated Challenge
```json
{
  "type": "generated",
  "title": "Addition Practice",
  "subtitle": "Numbers 1-20",
  "operation": "addition",
  "problemCount": 10,
  "numberRange": { "min": 1, "max": 20 }
}
```

### Explicit Challenge
```json
{
  "type": "explicit",
  "title": "Mixed Practice",
  "subtitle": "Custom problems",
  "problems": [
    { "operand1": 5, "operand2": 3, "operation": "addition" },
    { "operand1": 8, "operand2": 4, "operation": "division" }
  ]
}
```

## License

Part of the Kids Math Pup Tutor project. See parent LICENSE file.
