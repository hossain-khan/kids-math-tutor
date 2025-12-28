import { useEffect, useState } from "react";
import clsx from "clsx";

interface ToastProps {
  message: string;
  duration?: number;
  isVisible: boolean;
  onClose: () => void;
  variant?: "success" | "error" | "info";
}

const icons = {
  success: "✅",
  error: "❌",
  info: "ℹ️",
};

export default function Toast({
  message,
  duration = 3000,
  isVisible,
  onClose,
  variant = "success",
}: ToastProps) {
  const [show, setShow] = useState(isVisible);

  useEffect(() => {
    if (isVisible) {
      setShow(true);
      const timer = setTimeout(() => {
        setShow(false);
        onClose();
      }, duration);
      return () => clearTimeout(timer);
    } else {
      setShow(false);
    }
  }, [isVisible, duration, onClose]);

  if (!show) return null;

  const baseClasses =
    "text-white px-4 py-3 rounded-lg shadow-lg flex items-center gap-2 animate-in fade-in slide-in-from-bottom-4 duration-300";
  const variantClasses = {
    success: "bg-green-500",
    error: "bg-red-500",
    info: "bg-blue-500",
  };

  return (
    <div className="fixed bottom-4 left-4 right-4 z-50 max-w-sm mx-auto">
      <div className={clsx(baseClasses, variantClasses[variant])}>
        <span className="text-xl">{icons[variant]}</span>
        <span className="font-medium">{message}</span>
      </div>
    </div>
  );
}
