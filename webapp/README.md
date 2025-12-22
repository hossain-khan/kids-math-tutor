# Math Pup Worksheet Creator - Web App

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
```

### Cloudflare Workers (API)

```bash
# Start local worker
pnpm worker:dev

# Deploy to Cloudflare
pnpm worker:deploy
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
├── workers/           # Cloudflare Workers API
├── public/            # Static assets
└── tests/             # Test files
```

## Deployment

### Frontend (Cloudflare Pages)

```bash
# Build for production
pnpm build

# Deploy to Cloudflare Pages
wrangler pages deploy dist
```

### Backend (Cloudflare Workers)

```bash
# Deploy API worker
pnpm worker:deploy
```

## JSON Schema

The web app generates JSON that matches the Android app's custom challenge schema exactly:

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
