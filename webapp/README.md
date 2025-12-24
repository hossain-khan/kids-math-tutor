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

# Start development server
pnpm dev

# Open http://localhost:5173
```

### Development

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

```bash
# Login to Cloudflare (first time only)
npx wrangler login

# Build and deploy
pnpm build && npx wrangler deploy
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

# Deploy to Cloudflare Workers
npx wrangler deploy
```

### First-Time Setup

If you haven't deployed before:

```bash
# 1. Login to Cloudflare
npx wrangler login

# 2. Build and deploy
pnpm build && npx wrangler deploy
```

The configuration is in `wrangler.json`:
- Worker name: `pup-tutor-worksheet-generator`
- Static assets directory: `./dist`
- SPA mode enabled (all routes serve `index.html`)

### View Deployment

After deployment, visit: https://math-worksheet.gohk.xyz/

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
