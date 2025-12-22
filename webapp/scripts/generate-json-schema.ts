#!/usr/bin/env tsx
/**
 * Script to generate JSON Schema from Zod schemas
 * Run with: pnpm tsx scripts/generate-json-schema.ts
 */
import { zodToJsonSchema } from 'zod-to-json-schema'
import { writeFileSync } from 'fs'
import { resolve, dirname } from 'path'
import { fileURLToPath } from 'url'
import {
  GeneratedChallengeSpecSchema,
  ExplicitChallengeSpecSchema,
} from '../src/lib/schemas/challenge-schema'
import { z } from 'zod'

const __filename = fileURLToPath(import.meta.url)
const __dirname = dirname(__filename)

// Create a union schema for both challenge types
const ChallengeSpecSchema = z.union([
  GeneratedChallengeSpecSchema,
  ExplicitChallengeSpecSchema,
])

// Convert to JSON Schema
const jsonSchema = zodToJsonSchema(ChallengeSpecSchema, {
  name: 'ChallengeSpec',
  $refStrategy: 'none',
  target: 'jsonSchema7',
})

// Add metadata and examples
const schema = {
  $schema: 'http://json-schema.org/draft-07/schema#',
  $id: 'https://math-worksheet.gohk.xyz/challenge-schema.json',
  title: 'Math Pup Challenge Specification',
  description:
    'JSON schema for custom math practice challenges in Kids Math Pup Tutor app',
  ...jsonSchema,
  examples: [
    {
      type: 'generated',
      title: 'Addition Practice 1-20',
      subtitle: 'Master basic addition skills',
      operation: 'addition',
      problemCount: 10,
      numberRange: {
        min: 1,
        max: 20,
      },
    },
    {
      type: 'explicit',
      title: 'Mixed Math Practice',
      subtitle: 'Custom problems',
      problems: [
        { operand1: 5, operand2: 3, operation: 'addition' },
        { operand1: 12, operand2: 4, operation: 'division' },
        { operand1: 8, operand2: 2, operation: 'subtraction' },
        { operand1: 6, operand2: 7, operation: 'multiplication' },
      ],
    },
  ],
}

// Write to public folder
const outputPath = resolve(__dirname, '../public/challenge-schema.json')
writeFileSync(outputPath, JSON.stringify(schema, null, 2), 'utf-8')

console.log('✅ JSON Schema generated successfully!')
console.log(`📄 Output: ${outputPath}`)
console.log(
  `🌐 Available at: https://math-worksheet.gohk.xyz/challenge-schema.json`
)
