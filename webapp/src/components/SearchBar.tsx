import { useCallback } from "react";
import { X, Search } from "lucide-react";

interface SearchBarProps {
  value: string;
  onChange: (value: string) => void;
  onSearch: () => void;
  placeholder?: string;
  disabled?: boolean;
}

export default function SearchBar({
  value,
  onChange,
  onSearch,
  placeholder = "Search worksheets by title or content...",
  disabled = false,
}: SearchBarProps) {
  const handleClear = useCallback(() => {
    onChange("");
  }, [onChange]);

  const handleKeyDown = useCallback(
    (e: React.KeyboardEvent<HTMLInputElement>) => {
      if (e.key === "Enter") {
        onSearch();
      }
    },
    [onSearch],
  );

  return (
    <div className="flex gap-2">
      <div className="flex-1 relative">
        <div className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400">
          <Search size={20} />
        </div>
        <input
          type="text"
          value={value}
          onChange={(e) => onChange(e.target.value)}
          onKeyDown={handleKeyDown}
          placeholder={placeholder}
          disabled={disabled}
          className="w-full pl-10 pr-10 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent disabled:bg-gray-100 disabled:cursor-not-allowed"
          data-testid="search-input"
        />
        {value && (
          <button
            onClick={handleClear}
            disabled={disabled}
            className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-gray-600 disabled:cursor-not-allowed"
            aria-label="Clear search"
          >
            <X size={20} />
          </button>
        )}
      </div>
      <button
        onClick={onSearch}
        disabled={disabled || !value}
        className="px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 disabled:bg-gray-300 disabled:cursor-not-allowed transition-colors font-medium"
        data-testid="search-button"
      >
        Search
      </button>
    </div>
  );
}
