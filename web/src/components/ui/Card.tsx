import React from 'react';

interface CardProps {
  children: React.ReactNode;
  className?: string;
  onClick?: () => void;
  hoverable?: boolean;
  glow?: boolean;
}

export function Card({ children, className = '', onClick, hoverable = false, glow = false }: CardProps) {
  return (
    <div
      onClick={onClick}
      className={[
        'relative bg-white border border-slate-200/90 rounded-2xl shadow-xs overflow-hidden',
        hoverable && 'card-light-hover cursor-pointer',
        glow && 'ring-1 ring-[#00685b]/30 shadow-md',
        className,
      ]
        .filter(Boolean)
        .join(' ')}
    >
      {children}
    </div>
  );
}

export function CardHeader({ children, className = '' }: { children: React.ReactNode; className?: string }) {
  return (
    <div className={`px-6 py-4 border-b border-slate-100 bg-slate-50/50 ${className}`}>
      {children}
    </div>
  );
}

export function CardBody({ children, className = '' }: { children: React.ReactNode; className?: string }) {
  return <div className={`px-6 py-5 ${className}`}>{children}</div>;
}

export function CardFooter({ children, className = '' }: { children: React.ReactNode; className?: string }) {
  return (
    <div className={`px-6 py-3.5 border-t border-slate-100 bg-slate-50/50 text-xs text-slate-500 ${className}`}>
      {children}
    </div>
  );
}

