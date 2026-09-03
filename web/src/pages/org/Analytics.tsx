import { BarChart3, TrendingUp, Users, Clock, CalendarDays } from 'lucide-react';

const STATS = [
  { label: 'Total Visits Today', value: '142', icon: Users, trend: '+12%', color: 'text-blue-500', bg: 'bg-blue-50' },
  { label: 'Avg Wait Time', value: '18 min', icon: Clock, trend: '-5%', color: 'text-emerald-500', bg: 'bg-emerald-50' },
  { label: 'Completed Appointments', value: '98', icon: CalendarDays, trend: '+8%', color: 'text-purple-500', bg: 'bg-purple-50' },
];

export default function Analytics() {
  return (
    <div className="flex flex-col gap-6 animate-fade-in-up">
      <div className="flex items-center gap-3">
        <div className="w-10 h-10 rounded-xl bg-[#e6f4f1] text-[#00685b] flex items-center justify-center">
          <BarChart3 size={20} />
        </div>
        <div>
          <h1 className="text-2xl font-bold text-slate-900 tracking-tight">Analytics Overview</h1>
          <p className="text-slate-500 text-sm">Key performance metrics for your organization.</p>
        </div>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        {STATS.map((stat) => (
          <div key={stat.label} className="bg-white p-5 rounded-2xl border border-slate-200 shadow-sm flex items-start justify-between">
            <div>
              <p className="text-xs font-semibold text-slate-500 uppercase tracking-wider">{stat.label}</p>
              <h3 className="text-2xl font-black text-slate-900 mt-1">{stat.value}</h3>
              <div className="flex items-center gap-1 mt-2 text-emerald-600 text-xs font-bold bg-emerald-50 px-2 py-0.5 rounded-md w-fit">
                <TrendingUp size={12} />
                <span>{stat.trend} from yesterday</span>
              </div>
            </div>
            <div className={`p-3 rounded-xl ${stat.bg} ${stat.color}`}>
              <stat.icon size={24} />
            </div>
          </div>
        ))}
      </div>

      <div className="bg-white border border-slate-200 rounded-2xl p-8 text-center text-slate-500 flex flex-col items-center justify-center h-64 mt-4">
        <BarChart3 size={40} className="text-slate-300 mb-3" />
        <p className="font-semibold text-slate-700">Detailed charts coming soon</p>
        <p className="text-sm">We are working on bringing you advanced charting capabilities.</p>
      </div>
    </div>
  );
}
