import { useState } from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { Plus, Calendar, MapPin, ClipboardList, LogOut, Menu, X, Activity, Ticket, User } from 'lucide-react';
import { useAuthStore } from '../../store/authStore';
import { authApi } from '../../api/auth';

const navItems = [
  { to: '/patient',         label: 'Dashboard',   icon: Activity },
  { to: '/patient/find',    label: 'Find Clinic', icon: MapPin },
  { to: '/patient/book',    label: 'Book Visit',  icon: Calendar },
  { to: '/patient/queue',   label: 'Live Queue',  icon: Ticket },
  { to: '/patient/history', label: 'History',     icon: ClipboardList },
  { to: '/patient/profile', label: 'Profile',     icon: User },
];

export function PatientNav() {
  const clear = useAuthStore((s) => s.clear);
  const userName = useAuthStore((s) => s.userName);
  const userEmail = useAuthStore((s) => s.userEmail);
  const displayName = userName || 'Jane Doe';
  const initials = displayName.split(' ').map((n) => n[0]).join('').slice(0, 2).toUpperCase();
  const navigate = useNavigate();
  const [mobileOpen, setMobileOpen] = useState(false);

  const handleLogout = async () => {
    try { await authApi.logoutPatient(); } catch { /* ignore */ }
    clear();
    navigate('/');
  };

  return (
    <header className="bg-white border-b border-slate-200/80 sticky top-0 z-40 shadow-2xs">
      <div className="container mx-auto px-6 max-w-6xl flex items-center justify-between h-16">
        {/* Brand */}
        <div className="flex items-center gap-3">
          <div className="w-8 h-8 rounded-lg bg-[#00685b] flex items-center justify-center text-white shadow-xs">
            <Plus size={20} strokeWidth={3} />
          </div>
          <div className="flex flex-col">
            <span className="font-bold text-slate-900 tracking-tight text-base leading-tight">HealthQueue</span>
            <span className="text-[10px] font-semibold text-[#00685b] tracking-wider uppercase">Student &amp; Staff Portal</span>
          </div>
        </div>

        {/* Desktop Nav */}
        <nav className="hidden md:flex items-center gap-1 bg-slate-100/70 p-1 rounded-xl border border-slate-200/60">
          {navItems.map(({ to, label, icon: Icon }) => (
            <NavLink
              key={to}
              to={to}
              end={to === '/patient'}
              className={({ isActive }) =>
                [
                  'flex items-center gap-2 px-3.5 py-1.5 rounded-lg text-xs font-semibold transition-all duration-150',
                  isActive
                    ? 'text-[#00685b] bg-white shadow-2xs border border-slate-200/60'
                    : 'text-slate-600 hover:text-slate-900 hover:bg-slate-200/50',
                ].join(' ')
              }
            >
              <Icon size={14} />
              {label}
            </NavLink>
          ))}
        </nav>

        {/* Right action bar */}
        <div className="flex items-center gap-3">
          {/* Patient Profile Pill */}
          <div className="hidden sm:flex items-center gap-2 pl-1.5 pr-3 py-1 rounded-full bg-[#e6f4f1] text-[#00685b] text-xs font-bold border border-[#b2e2d8]">
            <div className="w-6 h-6 rounded-full bg-[#00685b] text-white flex items-center justify-center text-[10px] font-extrabold shadow-2xs">
              {initials}
            </div>
            <span>{displayName}</span>
          </div>

          <button
            onClick={handleLogout}
            className="hidden sm:flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-medium text-slate-500 hover:text-rose-600 hover:bg-rose-50 transition-colors"
          >
            <LogOut size={14} />
            <span>Logout</span>
          </button>

          {/* Mobile menu button */}
          <button
            onClick={() => setMobileOpen(!mobileOpen)}
            className="md:hidden p-2 rounded-lg text-slate-600 hover:text-slate-900 hover:bg-slate-100"
          >
            {mobileOpen ? <X size={20} /> : <Menu size={20} />}
          </button>
        </div>
      </div>

      {/* Mobile menu drawer */}
      {mobileOpen && (
        <div className="md:hidden border-t border-slate-100 bg-white px-4 py-4 space-y-2 shadow-lg">
          {/* Mobile Profile Card */}
          <div className="flex items-center gap-3 px-3 py-2.5 mb-2 rounded-xl bg-[#e6f4f1] border border-[#b2e2d8]">
            <div className="w-8 h-8 rounded-full bg-[#00685b] text-white flex items-center justify-center text-xs font-extrabold">
              {initials}
            </div>
            <div className="flex flex-col">
              <span className="font-bold text-slate-900 text-xs">{displayName}</span>
              {userEmail && <span className="text-[10px] text-slate-500">{userEmail}</span>}
            </div>
          </div>

          {navItems.map(({ to, label, icon: Icon }) => (
            <NavLink
              key={to}
              to={to}
              end={to === '/patient'}
              onClick={() => setMobileOpen(false)}
              className={({ isActive }) =>
                [
                  'flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-medium transition-colors',
                  isActive ? 'text-[#00685b] bg-[#e6f4f1] font-semibold' : 'text-slate-700 hover:bg-slate-100',
                ].join(' ')
              }
            >
              <Icon size={16} />
              {label}
            </NavLink>
          ))}
          <button
            onClick={handleLogout}
            className="flex items-center gap-3 w-full px-3 py-2.5 rounded-xl text-sm font-medium text-rose-600 hover:bg-rose-50 transition-colors"
          >
            <LogOut size={16} />
            Logout
          </button>
        </div>
      )}
    </header>
  );
}

