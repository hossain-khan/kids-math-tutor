import { useState } from 'react';

interface StarRatingProps {
  rating: number;
  count: number;
  onRate: (stars: number) => Promise<void>;
  disabled?: boolean;
  size?: 'sm' | 'md' | 'lg';
}

export default function StarRating({
  rating,
  count,
  onRate,
  disabled = false,
  size = 'md',
}: StarRatingProps) {
  const [hoverRating, setHoverRating] = useState(0);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [ratingSubmitted, setRatingSubmitted] = useState(false);

  const sizeClass = {
    sm: 'text-sm',
    md: 'text-base',
    lg: 'text-lg',
  }[size];

  const starSizeClass = {
    sm: 'w-4 h-4',
    md: 'w-5 h-5',
    lg: 'w-6 h-6',
  }[size];

  const handleStarClick = async (star: number) => {
    if (disabled || isSubmitting) return;

    setIsSubmitting(true);
    try {
      await onRate(star);
      setRatingSubmitted(true);
      // Reset submission state after 2 seconds
      setTimeout(() => setRatingSubmitted(false), 2000);
    } catch (error) {
      console.error('Failed to submit rating:', error);
    } finally {
      setIsSubmitting(false);
    }
  };

  const displayRating = hoverRating || rating;

  return (
    <div className={`flex items-center gap-2 ${sizeClass}`}>
      <div className="flex gap-1">
        {[1, 2, 3, 4, 5].map((star) => (
          <button
            key={star}
            onClick={() => handleStarClick(star)}
            onMouseEnter={() => !disabled && !isSubmitting && setHoverRating(star)}
            onMouseLeave={() => setHoverRating(0)}
            disabled={disabled || isSubmitting}
            className="transition-transform hover:scale-110 disabled:cursor-not-allowed disabled:opacity-50 focus:outline-none focus:ring-2 focus:ring-offset-1 focus:ring-blue-500 rounded"
            title={`Rate ${star} star${star > 1 ? 's' : ''}`}
            aria-label={`Rate ${star} star${star > 1 ? 's' : ''}`}
          >
            <span
              className={`${starSizeClass} inline-block transition-colors ${
                star <= displayRating
                  ? 'text-yellow-400'
                  : 'text-gray-300'
              }`}
            >
              ★
            </span>
          </button>
        ))}
      </div>

      <span className="text-gray-600 whitespace-nowrap">
        {rating > 0 ? (
          <>
            <span className="font-semibold text-gray-900">{rating.toFixed(1)}★</span>
            {' '}
            <span className="text-gray-500">({count})</span>
          </>
        ) : (
          <span className="text-gray-500">No ratings yet</span>
        )}
      </span>

      {ratingSubmitted && (
        <span className="text-xs text-green-600 font-medium animate-fade-in">
          ✓ Rated!
        </span>
      )}

      {isSubmitting && (
        <span className="text-xs text-blue-600 font-medium">Saving...</span>
      )}
    </div>
  );
}
