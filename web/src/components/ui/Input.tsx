import React from 'react';

interface InputProps extends React.InputHTMLAttributes<HTMLInputElement> {
  label?: string;
  error?: string;
  hint?: string;
  leftIcon?: React.ReactNode;
  rightIcon?: React.ReactNode;
}

export function Input({ label, error, hint, leftIcon, rightIcon, id, className = '', ...props }: InputProps) {
  return (
    <div className="flex flex-col gap-1.5 w-full">
      {label && (
        <label htmlFor={id} className="text-xs font-semibold uppercase tracking-wider text-slate-700">
          {label}
        </label>
      )}
      <div className="relative flex items-center">
        {leftIcon && (
          <span className="absolute left-3.5 text-slate-400 pointer-events-none transition-colors">
            {leftIcon}
          </span>
        )}
        <input
          id={id}
          {...props}
          className={[
            'w-full rounded-xl bg-white border text-slate-900 placeholder:text-slate-400',
            'px-3.5 py-2.5 text-sm transition-all duration-150 shadow-2xs',
            'focus:outline-none focus:ring-2 focus:ring-[#00685b]/30 focus:border-[#00685b]',
            error ? 'border-rose-500 focus:ring-rose-500/30' : 'border-slate-300 hover:border-slate-400',
            leftIcon ? 'pl-10' : '',
            rightIcon ? 'pr-10' : '',
            className,
          ].join(' ')}
        />
        {rightIcon && (
          <span className="absolute right-3.5 text-slate-400 pointer-events-none">
            {rightIcon}
          </span>
        )}
      </div>
      {hint && !error && <p className="text-xs text-slate-500">{hint}</p>}
      {error && <p className="text-xs font-medium text-rose-600 flex items-center gap-1">{error}</p>}
    </div>
  );
}

