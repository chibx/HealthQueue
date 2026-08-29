import { Calendar, Clock, User, ArrowRight } from 'lucide-react';
import { Card, CardBody } from '../ui/Card';
import { StatusBadge } from '../ui/Badge';
import type { AppointmentResponse } from '../../types';

interface AppointmentCardProps {
  appointment: AppointmentResponse;
  onAction?: () => void;
  actionLabel?: string;
}

function formatDate(iso: string) {
  try {
    return new Date(iso).toLocaleString('en-US', {
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  } catch {
    return iso;
  }
}

export function AppointmentCard({ appointment, onAction, actionLabel }: AppointmentCardProps) {
  return (
    <Card hoverable={!!onAction} onClick={onAction} className="border-slate-200 bg-white hover:border-[#b2e2d8] hover:shadow-sm transition-all duration-200">
      <CardBody className="flex flex-col gap-4 p-5">
        <div className="flex items-center justify-between gap-3 border-b border-slate-100 pb-3">
          <div className="flex items-center gap-2">
            <StatusBadge status={appointment.status} />
          </div>
          <span className="text-xs font-mono font-bold text-slate-600 bg-slate-100 px-2.5 py-1 rounded-lg border border-slate-200">
            Ticket #{appointment.id}
          </span>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-3 gap-3 text-xs">
          <div className="flex items-center gap-2.5 bg-slate-50 p-3 rounded-xl border border-slate-100">
            <Calendar size={15} className="shrink-0 text-[#00685b]" />
            <div className="flex flex-col">
              <span className="text-[10px] text-slate-400 uppercase font-bold">Start Time</span>
              <span className="font-bold text-slate-900">{formatDate(appointment.scheduledStartTime)}</span>
            </div>
          </div>

          <div className="flex items-center gap-2.5 bg-slate-50 p-3 rounded-xl border border-slate-100">
            <Clock size={15} className="shrink-0 text-[#00685b]" />
            <div className="flex flex-col">
              <span className="text-[10px] text-slate-400 uppercase font-bold">End Time</span>
              <span className="font-bold text-slate-900">{formatDate(appointment.scheduledEndTime)}</span>
            </div>
          </div>

          <div className="flex items-center gap-2.5 bg-slate-50 p-3 rounded-xl border border-slate-100">
            <User size={15} className="shrink-0 text-violet-600" />
            <div className="flex flex-col">
              <span className="text-[10px] text-slate-400 uppercase font-bold">Assigned Doctor</span>
              <span className="font-bold text-slate-900">Doctor #{appointment.doctorId}</span>
            </div>
          </div>
        </div>

        {actionLabel && onAction && (
          <div className="pt-1 flex justify-end">
            <button
              onClick={(e) => { e.stopPropagation(); onAction(); }}
              className="inline-flex items-center gap-1.5 text-xs font-bold text-[#00685b] hover:text-[#005247] transition-colors"
            >
              <span>{actionLabel}</span>
              <ArrowRight size={13} />
            </button>
          </div>
        )}
      </CardBody>
    </Card>
  );
}

