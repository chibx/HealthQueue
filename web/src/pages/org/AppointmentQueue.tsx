import { useState } from 'react';
import { ListOrdered, Search, Activity } from 'lucide-react';
import { Badge } from '../../components/ui/Badge';
import { Button } from '../../components/ui/Button';
import { AppointmentCard } from '../../components/shared/AppointmentCard';
import { UpdateStatus } from './UpdateStatus';
import { SAMPLE_APPOINTMENTS } from '../../data/sampleData';
import type { AppointmentResponse } from '../../types';

const STATUS_FILTERS = ['ALL', 'IN_QUEUE', 'IN_PROGRESS', 'SCHEDULED', 'COMPLETED'] as const;

function statusLabel(s: string) {
  return s.replace(/_/g, ' ');
}

export default function AppointmentQueue() {
  const [selected, setSelected]     = useState<AppointmentResponse | null>(null);
  const [statusFilter, setStatus]   = useState<string>('ALL');
  const [searchQuery, setSearch]     = useState('');

  const appointments: AppointmentResponse[] = SAMPLE_APPOINTMENTS;

  const filteredAppointments = appointments.filter((apt) => {
    const matchesStatus = statusFilter === 'ALL' || apt.status === statusFilter;
    const matchesSearch = apt.id.toString().includes(searchQuery) || apt.doctorId.toString().includes(searchQuery);
    return matchesStatus && matchesSearch;
  });

  const activeCount    = appointments.filter((a) => a.status === 'IN_QUEUE' || a.status === 'IN_PROGRESS').length;
  const completedCount = appointments.filter((a) => a.status === 'COMPLETED').length;

  return (
    <div className="flex flex-col gap-6 animate-fade-in-up">

      {/* ── Header ─────────────────────────────────────────────── */}
      <div className="flex flex-col sm:flex-row sm:items-start justify-between gap-4">
        <div>
          <div className="flex items-center gap-2 mb-1">
            <Badge variant="yellow" pulse>LIVE QUEUE</Badge>
            <span className="text-xs text-slate-400">· {appointments.length} total tickets</span>
          </div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight">Appointment Queue Console</h1>
          <p className="text-slate-500 text-xs sm:text-sm mt-1">
            Real-time control for active patient tickets and doctor assignments.
          </p>
          {/* Summary row */}
          <div className="flex items-center gap-4 mt-3 text-xs">
            <span className="inline-flex items-center gap-1.5 font-semibold text-emerald-700 bg-emerald-50 px-2.5 py-1 rounded-full border border-emerald-200">
              <span className="w-1.5 h-1.5 rounded-full bg-emerald-500 animate-pulse" />
              {activeCount} active
            </span>
            <span className="text-slate-400">{completedCount} completed today</span>
          </div>
        </div>

        <Button
          size="sm"
          className="bg-[#00685b] text-white rounded-xl shadow-sm"
          leftIcon={<Activity size={14} className="animate-pulse" />}
          onClick={() => {
            const first = filteredAppointments.find((a) => a.status === 'IN_QUEUE');
            if (first) setSelected(first);
          }}
        >
          Call Next Patient
        </Button>
      </div>

      {/* ── Search + Status filters ─────────────────────────────── */}
      <div className="flex flex-col sm:flex-row items-center gap-3">
        <div className="relative w-full sm:w-80">
          <Search size={15} className="absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400" />
          <input
            type="text"
            placeholder="Search ticket # or doctor ID…"
            value={searchQuery}
            onChange={(e) => setSearch(e.target.value)}
            className="w-full bg-white border border-slate-200 rounded-xl pl-9 pr-4 py-2.5 text-xs text-slate-800 placeholder:text-slate-400 focus:outline-none focus:border-[#00685b]/50 focus:ring-2 focus:ring-[#00685b]/10 transition shadow-sm"
          />
        </div>

        <div className="flex items-center gap-1.5 w-full sm:w-auto overflow-x-auto pb-1 sm:pb-0">
          {STATUS_FILTERS.map((st) => (
            <button
              key={st}
              onClick={() => setStatus(st)}
              className={[
                'px-3.5 py-1.5 rounded-full text-xs font-semibold border transition-all whitespace-nowrap',
                statusFilter === st
                  ? 'bg-[#00685b] text-white border-[#00685b] shadow-sm'
                  : 'bg-white text-slate-500 border-slate-200 hover:border-[#00685b]/40 hover:text-[#00685b]',
              ].join(' ')}
            >
              {statusLabel(st)}
            </button>
          ))}
        </div>
      </div>

      {/* ── Queue list ──────────────────────────────────────────── */}
      {filteredAppointments.length === 0 ? (
        <div className="bg-white border border-dashed border-slate-300 rounded-2xl p-16 flex flex-col items-center text-center gap-4">
          <div className="w-14 h-14 rounded-2xl bg-slate-100 flex items-center justify-center">
            <ListOrdered size={28} className="text-slate-400" />
          </div>
          <div>
            <p className="text-sm font-bold text-slate-900">No tickets match your filter</p>
            <p className="text-xs text-slate-400 mt-1 max-w-xs">Try selecting "ALL" status or clearing your search.</p>
          </div>
          <Button size="sm" variant="secondary" className="rounded-full" onClick={() => { setStatus('ALL'); setSearch(''); }}>
            Reset filters
          </Button>
        </div>
      ) : (
        <div className="grid gap-4">
          {filteredAppointments.map((apt) => (
            <div
              key={apt.id}
              className={apt.status === 'COMPLETED' ? 'opacity-60' : ''}
            >
              <AppointmentCard
                appointment={apt}
                actionLabel="Manage Ticket"
                onAction={() => setSelected(apt)}
              />
            </div>
          ))}
        </div>
      )}

      {selected && <UpdateStatus appointment={selected} onClose={() => setSelected(null)} />}
    </div>
  );
}
