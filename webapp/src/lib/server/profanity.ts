/**
 * Content moderation utilities for detecting inappropriate content in worksheets.
 * Uses bad-words library for basic profanity detection.
 */

import { Filter } from 'bad-words';

// Initialize profanity filter with default word list
const filter = new Filter();

// Add custom inappropriate words/phrases for children's education context
const customProfanityWords: string[] = [
  // Add any custom words specific to your community guidelines here
];

customProfanityWords.forEach((word) => {
  filter.addWords(word);
});

/**
 * Check if text contains profanity or inappropriate content
 * @param text - Text to check for profanity
 * @returns true if inappropriate content detected, false otherwise
 */
export function containsProfanity(text: string): boolean {
  if (!text) return false;
  return filter.isProfane(text);
}

/**
 * Check multiple fields for profanity
 * @param worksheet - Object with title, subtitle, description to check
 * @returns true if any field contains inappropriate content
 */
export function validateWorksheetContent(worksheet: {
  title: string;
  subtitle?: string;
  description?: string;
}): boolean {
  const textToCheck = [
    worksheet.title,
    worksheet.subtitle || '',
    worksheet.description || '',
  ]
    .join(' ')
    .toLowerCase();

  return containsProfanity(textToCheck);
}
