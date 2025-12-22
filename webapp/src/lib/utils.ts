import { type ClassValue, clsx } from "clsx";
import { twMerge } from "tailwind-merge";

/**
 * Utility function to merge Tailwind CSS classes with proper precedence
 */
export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

/**
 * Format a number with commas for better readability
 */
export function formatNumber(num: number): string {
  return new Intl.NumberFormat("en-US").format(num);
}

/**
 * Download a JSON file to the user's device
 */
export function downloadJson(data: unknown, filename: string): void {
  const json = JSON.stringify(data, null, 2);
  const blob = new Blob([json], { type: "application/json" });
  const url = URL.createObjectURL(blob);

  const link = document.createElement("a");
  link.href = url;
  link.download = filename;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);

  URL.revokeObjectURL(url);
}

/**
 * Copy text to clipboard
 */
export async function copyToClipboard(text: string): Promise<boolean> {
  try {
    await navigator.clipboard.writeText(text);
    return true;
  } catch (err) {
    console.error("Failed to copy to clipboard:", err);
    return false;
  }
}

/**
 * Share data using Web Share API (fallback to clipboard)
 */
export async function shareOrCopy(data: {
  title?: string;
  text: string;
}): Promise<{ success: boolean; method: "share" | "clipboard" }> {
  // Try Web Share API first
  if (navigator.share) {
    try {
      await navigator.share(data);
      return { success: true, method: "share" };
    } catch (err) {
      // User cancelled or error occurred
      console.log("Share cancelled or failed:", err);
    }
  }

  // Fallback to clipboard
  const copied = await copyToClipboard(data.text);
  return { success: copied, method: "clipboard" };
}
