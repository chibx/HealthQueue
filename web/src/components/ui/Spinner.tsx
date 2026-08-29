interface SpinnerProps {
  size?: number;
  className?: string;
}

export function Spinner({ size = 24, className = '' }: SpinnerProps) {
  return (
    <svg
      className={`animate-spin text-teal-400 ${className}`}
      width={size}
      height={size}
      viewBox="0 0 24 24"
      fill="none"
    >
      <circle
        className="opacity-25"
        cx="12" cy="12" r="10"
        stroke="currentColor"
        strokeWidth="4"
      />
      <path
        className="opacity-75"
        fill="currentColor"
        d="M4 12a8 8 0 018-8v8H4z"
      />
    </svg>
  );
}

export function FullPageSpinner() {
  return (
    <div className="min-h-screen bg-slate-950 flex items-center justify-center">
      <Spinner size={40} />
    </div>
  );
}
