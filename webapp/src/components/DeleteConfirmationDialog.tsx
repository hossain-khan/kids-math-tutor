import { AlertTriangle, Loader2 } from "lucide-react";
import Button from "@/components/Button";
import Card from "@/components/Card";

interface DeleteConfirmationDialogProps {
  /** The title of the worksheet to be deleted */
  title: string;
  /** Indicates if a delete operation is in progress */
  isDeleting?: boolean;
  /** Callback when user confirms deletion */
  onConfirm: () => void;
  /** Callback when user cancels deletion */
  onCancel: () => void;
  /** Optional description/subtitle of worksheet */
  subtitle?: string;
  /** Warning message to display */
  warningMessage?: string;
}

/**
 * DeleteConfirmationDialog component
 * Provides a clear confirmation dialog for destructive actions
 * Shows worksheet details and requires explicit confirmation
 */
export default function DeleteConfirmationDialog({
  title,
  isDeleting = false,
  onConfirm,
  onCancel,
  subtitle,
  warningMessage = "This action cannot be undone.",
}: DeleteConfirmationDialogProps) {
  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
      <Card className="max-w-md w-full">
        <div className="p-6 space-y-4">
          {/* Icon and Heading */}
          <div className="flex gap-3 items-start">
            <div className="flex-shrink-0">
              <AlertTriangle
                className="h-6 w-6 text-red-600"
                aria-hidden="true"
              />
            </div>
            <div className="flex-1">
              <h3 className="text-lg font-bold text-gray-900">
                Delete Worksheet?
              </h3>
              <p className="text-sm text-gray-600 mt-1">{warningMessage}</p>
            </div>
          </div>

          {/* Worksheet Details */}
          <div className="bg-gray-50 rounded-lg p-4 space-y-2 border border-gray-200">
            <div>
              <p className="text-xs font-semibold text-gray-500 uppercase tracking-wide">
                Worksheet
              </p>
              <p className="text-sm font-semibold text-gray-900 mt-1 break-words">
                {title}
              </p>
            </div>
            {subtitle && (
              <div className="pt-2 border-t border-gray-300">
                <p className="text-xs text-gray-600">{subtitle}</p>
              </div>
            )}
          </div>

          {/* Action Buttons */}
          <div className="flex gap-3 pt-4">
            <Button
              variant="secondary"
              onClick={onCancel}
              disabled={isDeleting}
              className="flex-1"
              data-testid="cancel-delete-button"
            >
              Cancel
            </Button>
            <Button
              variant="primary"
              onClick={onConfirm}
              disabled={isDeleting}
              className="flex-1 gap-2 bg-red-600 hover:bg-red-700 disabled:bg-red-400"
              data-testid="confirm-delete-button"
            >
              {isDeleting ? (
                <>
                  <Loader2 className="h-4 w-4 animate-spin" />
                  Deleting...
                </>
              ) : (
                "Delete"
              )}
            </Button>
          </div>

          {/* Helper Text */}
          <p className="text-xs text-gray-500 text-center">
            All associated data will be permanently removed
          </p>
        </div>
      </Card>
    </div>
  );
}
