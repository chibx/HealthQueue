import type { AppointmentStatus } from '../../types';

type BadgeVariant = 'green' | 'blue' | 'yellow' | 'purple' | 'red' | 'slate';

interface BadgeProps {
  children: React.ReactNode;
  variant?: BadgeVariant;
  pulse?: boolean;
  className?: string;
}

const variantClasses: Record<BadgeVariant, { container: string; dot: string }> = {
  green:  { container: 'bg-emerald-50 text-emerald-700 border border-emerald-200/80', dot: 'bg-emerald-500' },
  blue:   { container: 'bg-sky-50 text-sky-700 border border-sky-200/80',             dot: 'bg-sky-500' },
  yellow: { container: 'bg-amber-50 text-amber-800 border border-amber-200/80',         dot: 'bg-amber-500' },
  purple: { container: 'bg-violet-50 text-violet-700 border border-violet-200/80',       dot: 'bg-violet-500' },
  red:    { container: 'bg-rose-50 text-rose-700 border border-rose-200/80',             dot: 'bg-rose-500' },
  slate:  { container: 'bg-slate-100 text-slate-700 border border-slate-200/80',           dot: 'bg-slate-500' },
};

export function Badge({ children, variant = 'slate', pulse = false, className = '' }: BadgeProps) {
  const style = variantClasses[variant];
  return (
    <span
      className={[
        'inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold tracking-wide',
        style.container,
        className,
      ].join(' ')}
    >
      {pulse && (
        <span className="relative flex h-2 w-2">
          <span className={`animate-ping absolute inline-flex h-full w-full rounded-full opacity-75 ${style.dot}`} />
          <span className={`relative inline-flex rounded-full h-2 w-2 ${style.dot}`} />
        </span>
      )}
      {children}
    </span>
  );
}

// Convenience: maps AppointmentStatus → Badge
const statusMap: Record<AppointmentStatus, { label: string; variant: BadgeVariant; pulse: boolean }> = {
  SCHEDULED:   { label: 'Scheduled',   variant: 'blue',   pulse: false },
  IN_QUEUE:    { label: 'In Queue',    variant: 'yellow', pulse: true },
  IN_PROGRESS: { label: 'In Progress', variant: 'purple', pulse: true },
  COMPLETED:   { label: 'Completed',   variant: 'green',  pulse: false },
  OVERSTAYED:  { label: 'Overstayed',  variant: 'red',    pulse: false },
  CANCELLED:   { label: 'Cancelled',   variant: 'slate',  pulse: false },
};

export function StatusBadge({ status }: { status: AppointmentStatus }) {
  const { label, variant, pulse } = statusMap[status] ?? { label: status, variant: 'slate', pulse: false };
  return <Badge variant={variant} pulse={pulse}>{label}</Badge>;
}

