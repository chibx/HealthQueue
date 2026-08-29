import React, { createContext, useCallback, useContext, useState } from 'react';
import { CheckCircle2, XCircle, Info, X } from 'lucide-react';

type ToastType = 'success' | 'error' | 'info';

interface Toast {
  id: string;
  type: ToastType;
  title: string;
  message?: string;
}

interface ToastContextValue {
  toast: (type: ToastType, title: string, message?: string) => void;
  success: (title: string, message?: string) => void;
  error: (title: string, message?: string) => void;
  info: (title: string, message?: string) => void;
}

const ToastContext = createContext<ToastContextValue | null>(null);

const icons: Record<ToastType, React.ReactNode> = {
  success: <CheckCircle2 size={18} className="text-emerald-500 shrink-0" />,
  error:   <XCircle size={18} className="text-rose-500 shrink-0" />,
  info:    <Info size={18} className="text-sky-500 shrink-0" />,
};

const borders: Record<ToastType, string> = {
  success: 'border-l-4 border-l-emerald-500',
  error:   'border-l-4 border-l-rose-500',
  info:    'border-l-4 border-l-sky-500',
};

export function ToastProvider({ children }: { children: React.ReactNode }) {
  const [toasts, setToasts] = useState<Toast[]>([]);

  const dismiss = useCallback((id: string) => {
    setToasts((prev) => prev.filter((t) => t.id !== id));
  }, []);

  const toast = useCallback((type: ToastType, title: string, message?: string) => {
    const id = `${Date.now()}-${Math.random()}`;
    setToasts((prev) => [...prev.slice(-4), { id, type, title, message }]);
    setTimeout(() => dismiss(id), 4000);
  }, [dismiss]);

  const success = useCallback((title: string, msg?: string) => toast('success', title, msg), [toast]);
  const error   = useCallback((title: string, msg?: string) => toast('error', title, msg), [toast]);
  const info    = useCallback((title: string, msg?: string) => toast('info', title, msg), [toast]);

  return (
    <ToastContext.Provider value={{ toast, success, error, info }}>
      {children}

      {/* Toast container */}
      <div className="fixed bottom-5 right-5 z-[9999] flex flex-col gap-3 pointer-events-none">
        {toasts.map((t) => (
          <div
            key={t.id}
            className={[
              'toast-enter pointer-events-auto',
              'flex items-start gap-3 min-w-[280px] max-w-[360px]',
              'bg-white rounded-2xl shadow-xl p-4',
              borders[t.type],
            ].join(' ')}
          >
            {icons[t.type]}
            <div className="flex-1 min-w-0">
              <p className="text-sm font-semibold text-slate-900 leading-tight">{t.title}</p>
              {t.message && <p className="text-xs text-slate-500 mt-0.5 leading-relaxed">{t.message}</p>}
            </div>
            <button
              onClick={() => dismiss(t.id)}
              className="p-0.5 rounded-md text-slate-400 hover:text-slate-700 hover:bg-slate-100 transition-colors shrink-0"
            >
              <X size={14} />
            </button>
          </div>
        ))}
      </div>
    </ToastContext.Provider>
  );
}

// eslint-disable-next-line react-refresh/only-export-components
export function useToast() {
  const ctx = useContext(ToastContext);
  if (!ctx) throw new Error('useToast must be used inside <ToastProvider>');
  return ctx;
}
