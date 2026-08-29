import { useState, useEffect } from 'react';
import { Calendar, MapPin, ClipboardList, ArrowRight, Ticket, Clock, Bell, ChevronRight } from 'lucide-react';
import { Link } from 'react-router-dom';
import { Button } from '../../components/ui/Button';
import { useAuthStore } from '../../store/authStore';

/* ── Quick action cards ────────────────────────────────────────── */
const quickLinks = [
  {
    to: '/patient/find',
    label: 'Find Nearby Clinic',
    icon: MapPin,
    desc: 'Locate open branches with live distance metrics',
    accent: '#e6f4f1',
    accentBorder: '#b2e2d8',
    iconColor: '#00685b',
  },
  {
    to: '/patient/book',
    label: 'Book Appointment',
    icon: Calendar,
    desc: 'Schedule a visit and reserve your queue slot',
    accent: '#eff6ff',
    accentBorder: '#bfdbfe',
    iconColor: '#2563eb',
  },
  {
    to: '/patient/history',
    label: 'Visit Records',
    icon: ClipboardList,
    desc: 'View completed clinic visits and medical notes',
    accent: '#faf5ff',
    accentBorder: '#ddd6fe',
    iconColor: '#7c3aed',
  },
];

/* ── Mock "active booking" — replace with real API data ────────── */
const MOCK_ACTIVE_BOOKING = {
  queuePosition: 2,
  waitMinutes: 12,
  ticketNo: '#1042',
  room: 'Room 3',
  clinicName: 'City General Hospital',
  specialty: 'Cardiology',
  hasActiveBooking: true,
};

export default function PatientDashboard() {
  const userName = useAuthStore((s) => s.userName);
  const displayName = userName || 'Jane';
  const [queuePos, setQueuePos] = useState(MOCK_ACTIVE_BOOKING.queuePosition);
  const [waitMin, setWaitMin]   = useState(MOCK_ACTIVE_BOOKING.waitMinutes);

  // Animate the live queue widget
  useEffect(() => {
    if (!MOCK_ACTIVE_BOOKING.hasActiveBooking) return;
    const t = setInterval(() => {
      setQueuePos((p) => (p > 1 ? p - 1 : 3));
      setWaitMin((w)  => (w > 5 ? w - 5 : 15));
    }, 4500);
    return () => clearInterval(t);
  }, []);

  return (
    <div className="flex flex-col gap-8 animate-fade-in-up">

      {/* ── Page header ─────────────────────────────────────────── */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <p className="text-xs font-bold text-[#00685b] tracking-widest uppercase mb-1">Patient Portal</p>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight">
            Welcome back, {displayName} 👋
          </h1>
          <p className="text-slate-500 text-xs sm:text-sm mt-1">
            Manage your healthcare queue, discover clinics, and track live wait times.
          </p>
        </div>
        <Link to="/patient/book">
          <Button size="md" className="bg-[#00685b] text-white rounded-full shadow-sm shrink-0" rightIcon={<ArrowRight size={16} />}>
            Book New Visit
          </Button>
        </Link>
      </div>

      {/* ── Live queue widget ────────────────────────────────────── */}
      {MOCK_ACTIVE_BOOKING.hasActiveBooking && (
        <div className="bg-white border border-slate-200 rounded-3xl p-6 shadow-sm flex flex-col gap-5">
          <div className="flex items-center justify-between">
            <span className="text-sm font-bold text-slate-900">Active Visit</span>
            <span className="inline-flex items-center gap-1.5 text-[11px] font-bold text-amber-700 bg-amber-50 px-2.5 py-1 rounded-full border border-amber-200">
              <span className="w-1.5 h-1.5 rounded-full bg-amber-500 animate-pulse" />
              IN QUEUE
            </span>
          </div>

          <div className="grid sm:grid-cols-2 gap-4">
            {/* Dark teal queue card */}
            <div className="bg-[#0b3b36] rounded-2xl p-5 text-white relative overflow-hidden">
              <div className="absolute inset-0 bg-grid-pattern-light opacity-10 pointer-events-none" />
              <div className="flex items-center gap-4 relative">
                <div className="w-14 h-14 rounded-full bg-[#00685b] border-2 border-teal-400/30 flex items-center justify-center text-white text-3xl font-extrabold shrink-0 animate-pulse-glow">
                  {queuePos}
                </div>
                <div>
                  <p className="text-[10px] text-teal-200 uppercase font-bold tracking-wider mb-0.5">Position in queue</p>
                  <h2 className="text-2xl font-extrabold text-white leading-none">{queuePos} {queuePos === 1 ? 'person' : 'people'} ahead</h2>
                  <p className="text-xs text-teal-200 mt-1 flex items-center gap-1">
                    <Clock size={11} className="text-teal-300" />
                    ~{waitMin} min estimated wait
                  </p>
                </div>
              </div>

              {/* Progress segments */}
              <div className="grid grid-cols-4 gap-1.5 mt-4">
                {[0,1,2,3].map((i) => (
                  <div key={i} className={`h-1.5 rounded-full transition-all duration-700 ${i < 3 ? 'bg-[#00685b]' : 'bg-white/20'}`} />
                ))}
              </div>
            </div>

            {/* Right: info + CTA */}
            <div className="flex flex-col justify-between gap-4">
              <div className="bg-slate-50 border border-slate-200 rounded-2xl p-4 flex flex-col gap-2 text-xs">
                <div className="flex items-center gap-2 text-slate-500">
                  <Ticket size={13} className="text-[#00685b]" />
                  <span>Ticket <strong className="text-slate-900">{MOCK_ACTIVE_BOOKING.ticketNo}</strong></span>
                </div>
                <div className="flex items-center gap-2 text-slate-500">
                  <MapPin size={13} className="text-[#00685b]" />
                  <span>{MOCK_ACTIVE_BOOKING.clinicName}</span>
                </div>
                <div className="flex items-center gap-2 text-slate-500">
                  <Clock size={13} className="text-[#00685b]" />
                  <span>{MOCK_ACTIVE_BOOKING.specialty} · {MOCK_ACTIVE_BOOKING.room}</span>
                </div>
              </div>

              {/* "You're next" nudge */}
              {queuePos <= 2 && (
                <div className="bg-[#e6f4f1] border border-[#b2e2d8] rounded-xl px-4 py-3 flex items-center gap-3">
                  <div className="w-7 h-7 rounded-full bg-[#00685b] flex items-center justify-center text-white shrink-0">
                    <Bell size={12} />
                  </div>
                  <div>
                    <p className="text-xs font-bold text-[#0b3b36]">You're almost up!</p>
                    <p className="text-[11px] text-slate-500">Head to the reception desk soon.</p>
                  </div>
                </div>
              )}
            </div>
          </div>
        </div>
      )}

      {/* No active booking state */}
      {!MOCK_ACTIVE_BOOKING.hasActiveBooking && (
        <div className="bg-white border border-dashed border-slate-300 rounded-3xl p-10 flex flex-col items-center text-center gap-4">
          <div className="w-14 h-14 rounded-2xl bg-[#e6f4f1] flex items-center justify-center">
            <Ticket size={28} className="text-[#00685b]" />
          </div>
          <div>
            <h3 className="font-bold text-slate-900 text-base">No active appointment</h3>
            <p className="text-xs text-slate-400 mt-1 max-w-xs">Find a clinic near you and reserve a queue spot to get started.</p>
          </div>
          <Link to="/patient/find">
            <Button size="sm" className="bg-[#00685b] text-white rounded-full" rightIcon={<ArrowRight size={14} />}>
              Find a clinic
            </Button>
          </Link>
        </div>
      )}

      {/* ── Quick action cards ───────────────────────────────────── */}
      <div>
        <h2 className="text-sm font-bold text-slate-700 mb-4 tracking-tight">Quick actions</h2>
        <div className="grid md:grid-cols-3 gap-4">
          {quickLinks.map(({ to, label, icon: Icon, desc, accent, accentBorder, iconColor }) => (
            <Link key={to} to={to} className="group">
              <div
                className="bg-white border border-slate-200 rounded-2xl p-5 h-full flex flex-col gap-4 hover:shadow-md transition-all duration-200 hover:-translate-y-0.5"
                style={{ '--accent': accent, '--accentBorder': accentBorder } as React.CSSProperties}
              >
                <div
                  className="w-10 h-10 rounded-xl flex items-center justify-center shrink-0"
                  style={{ background: accent, border: `1px solid ${accentBorder}` }}
                >
                  <Icon size={18} style={{ color: iconColor }} />
                </div>
                <div className="flex-1">
                  <h3 className="font-bold text-slate-900 text-sm mb-1">{label}</h3>
                  <p className="text-xs text-slate-400 leading-relaxed">{desc}</p>
                </div>
                <div className="flex items-center gap-1 text-xs font-semibold" style={{ color: iconColor }}>
                  <span>Go</span>
                  <ChevronRight size={13} className="group-hover:translate-x-0.5 transition-transform" />
                </div>
              </div>
            </Link>
          ))}
        </div>
      </div>

      {/* ── Queue tip ────────────────────────────────────────────── */}
      <div className="bg-[#e6f4f1]/60 border border-[#b2e2d8] rounded-2xl p-5 flex items-start gap-4 text-xs">
        <div className="p-2 rounded-xl bg-[#00685b]/10 text-[#00685b] shrink-0">
          <Bell size={16} />
        </div>
        <div>
          <span className="font-bold text-slate-900 block mb-0.5">Queue tip</span>
          <span className="text-slate-500">
            When your position reaches <strong>#2</strong>, make your way to the clinic reception desk. You'll receive a notification when the doctor is ready for you.
          </span>
        </div>
      </div>

    </div>
  );
}
