import { useEffect, useState } from "react";

interface ToastProps {
  message: string;
  duration?: number;
  isVisible: boolean;
  onClose: () => void;
}

export default function Toast({
  message,
  duration = 3000,
  isVisible,
  onClose,
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
    }
  }, [isVisible, duration, onClose]);

  if (!show) return null;

  return (
    <div className="fixed bottom-4 left-4 right-4 z-50 max-w-sm mx-auto">
      <div className="bg-green-500 text-white px-4 py-3 rounded-lg shadow-lg flex items-center gap-2 animate-in fade-in slide-in-from-bottom-4 duration-300">
        <span className="text-xl">✅</span>
        <span className="font-medium">{message}</span>
      </div>
    </div>
  );
}
