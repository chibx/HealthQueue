import { Link } from 'react-router-dom';
import { ArrowRight, TrendingUp, TrendingDown, GitBranch, Users, Ticket, Activity } from 'lucide-react';
import { Button } from '../../components/ui/Button';
import { useAuthStore } from '../../store/authStore';

const QUEUE_HEALTH = [
  { dept: 'Cardiology',       tickets: 12, avgMin: 18, trend: 'up'   },
  { dept: 'General Medicine', tickets: 15, avgMin: 11, trend: 'down' },
  { dept: 'Dermatology',      tickets: 9,  avgMin: 8,  trend: 'down' },
];

const MANAGE_LINKS = [
  { to: '/org/branches', label: 'Add a new branch',    desc: 'Configure a new clinic location' },
  { to: '/org/doctors',  label: 'Register a doctor',   desc: 'Add medical staff to a branch' },
  { to: '/org/queue',    label: 'Review visit records', desc: 'Inspect appointment history' },
];

function getHourGreeting() {
  const h = new Date().getHours();
  if (h < 12) return 'Good morning';
  if (h < 18) return 'Good afternoon';
  return 'Good evening';
}

function waitColour(min: number) {
  if (min <= 10) return 'text-emerald-700 bg-emerald-50 border-emerald-200';
  if (min <= 20) return 'text-amber-700 bg-amber-50 border-amber-200';
  return 'text-rose-700 bg-rose-50 border-rose-200';
}

export default function OrgDashboard() {
  const orgId = useAuthStore((s) => s.orgId);

  return (
    <div className="flex flex-col gap-8 max-w-6xl animate-fade-in-up">

      {/* ── Header ─────────────────────────────────────────────── */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div>
          <span className="text-[11px] font-bold text-[#00685b] tracking-widest uppercase block mb-1">
            Operations Overview {orgId && `· Org #${orgId}`}
          </span>
          <h1 className="text-3xl sm:text-4xl font-bold text-slate-900 tracking-tight">
            {getHourGreeting()} 👋
          </h1>
          <p className="text-slate-500 text-xs sm:text-sm mt-1">Here's what's happening across your network right now.</p>
        </div>
        <Link to="/org/queue">
          <Button size="md" className="bg-[#00685b] hover:bg-[#005247] text-white rounded-xl px-5 shadow-sm" leftIcon={<Activity size={15} />}>
            Open live queue
          </Button>
        </Link>
      </div>

      {/* ── 3 metric cards ─────────────────────────────────────── */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-5">
        {[
          { icon: GitBranch, label: 'Branches',        value: '4',  sub: 'Across 2 cities',      iconBg: 'bg-[#e6f4f1]', iconColor: 'text-[#00685b]' },
          { icon: Users,     label: 'Doctors',         value: '28', sub: '22 currently on duty', iconBg: 'bg-sky-50',    iconColor: 'text-sky-600' },
          { icon: Ticket,    label: 'Active tickets',  value: '36', sub: '8 ready to call',      iconBg: 'bg-amber-50',  iconColor: 'text-amber-600' },
        ].map(({ icon: Icon, label, value, sub, iconBg, iconColor }) => (
          <div key={label} className="bg-white border border-slate-200 rounded-2xl p-6 shadow-sm flex flex-col gap-3">
            <div className="flex items-center justify-between">
              <span className="text-xs font-medium text-slate-500">{label}</span>
              <div className={`w-8 h-8 rounded-xl ${iconBg} flex items-center justify-center`}>
                <Icon size={16} className={iconColor} />
              </div>
            </div>
            <div>
              <span className="text-4xl font-extrabold text-slate-900 tracking-tight">{value}</span>
            </div>
            <span className="text-xs font-semibold text-[#00685b] flex items-center gap-1">
              <TrendingUp size={12} />
              {sub}
            </span>
          </div>
        ))}
      </div>

      {/* ── Bottom: Queue health + Management ──────────────────── */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">

        {/* Queue health table */}
        <div className="lg:col-span-7 bg-white border border-slate-200 rounded-3xl p-6 sm:p-8 shadow-sm">
          <div className="flex items-center justify-between mb-6">
            <h2 className="text-base font-bold text-slate-900">Today's queue health</h2>
            <span className="inline-flex items-center gap-1.5 text-[10px] font-bold text-emerald-700 bg-emerald-50 border border-emerald-200 px-2.5 py-1 rounded-full">
              <span className="w-1.5 h-1.5 rounded-full bg-emerald-500 animate-pulse" />
              LIVE
            </span>
          </div>

          <div className="flex flex-col divide-y divide-slate-100">
            {QUEUE_HEALTH.map(({ dept, tickets, avgMin, trend }) => (
              <div key={dept} className="flex items-center justify-between py-4">
                <div>
                  <h3 className="font-bold text-slate-900 text-sm">{dept}</h3>
                  <p className="text-xs text-slate-400 mt-0.5">{tickets} active tickets</p>
                </div>
                <div className="flex items-center gap-3">
                  {trend === 'up'
                    ? <TrendingUp  size={14} className="text-rose-400" />
                    : <TrendingDown size={14} className="text-emerald-500" />
                  }
                  <span className={`text-xs font-bold px-2.5 py-1 rounded-full border ${waitColour(avgMin)}`}>
                    {avgMin} min avg
                  </span>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Management quick links */}
        <div className="lg:col-span-5 bg-[#e6f4f1]/60 border border-[#b2e2d8] rounded-3xl p-6 sm:p-8 flex flex-col gap-5">
          <h2 className="text-base font-bold text-[#0b3b36]">Manage your system</h2>
          <div className="flex flex-col gap-3">
            {MANAGE_LINKS.map(({ to, label, desc }) => (
              <Link
                key={to}
                to={to}
                className="bg-white hover:bg-slate-50 border border-slate-200/80 rounded-2xl p-4 flex items-center justify-between group transition-all duration-150 hover:shadow-sm hover:border-[#b2e2d8]"
              >
                <div>
                  <p className="text-sm font-bold text-slate-900">{label}</p>
                  <p className="text-[11px] text-slate-400 mt-0.5">{desc}</p>
                </div>
                <ArrowRight size={15} className="text-[#00685b] group-hover:translate-x-1 transition-transform shrink-0" />
              </Link>
            ))}
          </div>
        </div>
      </div>

    </div>
  );
}
