import React from 'react';

type Variant = 'primary' | 'secondary' | 'outline' | 'danger' | 'ghost';
type Size = 'sm' | 'md' | 'lg';

interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: Variant;
  size?: Size;
  isLoading?: boolean;
  leftIcon?: React.ReactNode;
  rightIcon?: React.ReactNode;
}

const variantClasses: Record<Variant, string> = {
  primary:
    'bg-[#00685b] hover:bg-[#005247] active:bg-[#004239] text-white font-medium shadow-sm border border-transparent',
  secondary:
    'bg-[#e6f4f1] hover:bg-[#d4ece6] active:bg-[#c2e4db] text-[#004d40] font-medium border border-transparent',
  outline:
    'bg-white hover:bg-slate-50 active:bg-slate-100 text-slate-700 border border-slate-300 shadow-2xs font-medium',
  danger:
    'bg-rose-600 hover:bg-rose-700 active:bg-rose-800 text-white font-medium shadow-sm border border-transparent',
  ghost:
    'bg-transparent hover:bg-slate-100 active:bg-slate-200 text-slate-600 hover:text-slate-900',
};

const sizeClasses: Record<Size, string> = {
  sm: 'px-3 py-1.5 text-xs rounded-full gap-1.5 font-medium',
  md: 'px-4.5 py-2.5 text-sm rounded-full gap-2 font-medium',
  lg: 'px-6 py-3.5 text-base rounded-full gap-2.5 font-semibold tracking-tight',
};

export function Button({
  variant = 'primary',
  size = 'md',
  isLoading = false,
  leftIcon,
  rightIcon,
  className = '',
  children,
  disabled,
  ...props
}: ButtonProps) {
  return (
    <button
      {...props}
      disabled={disabled || isLoading}
      className={[
        'inline-flex items-center justify-center select-none',
        'transition-all duration-150 ease-out cursor-pointer active:scale-[0.98]',
        'focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#00685b]/40 focus-visible:ring-offset-2 focus-visible:ring-offset-white',
        'disabled:opacity-50 disabled:cursor-not-allowed disabled:active:scale-100 disabled:shadow-none',
        variantClasses[variant],
        sizeClasses[size],
        className,
      ].join(' ')}
    >
      {isLoading ? (
        <svg className="animate-spin h-4 w-4 shrink-0 text-current" viewBox="0 0 24 24" fill="none">
          <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
          <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8H4z" />
        </svg>
      ) : (
        leftIcon
      )}
      {children}
      {!isLoading && rightIcon}
    </button>
  );
}

