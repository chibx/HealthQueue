import { useState } from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { Plus, LayoutGrid, GitBranch, Users, ListOrdered, LogOut, Menu, X } from 'lucide-react';
import { useAuthStore } from '../../store/authStore';
import { authApi } from '../../api/auth';

const navItems = [
  { to: '/org',          label: 'Overview',   icon: LayoutGrid },
  { to: '/org/branches', label: 'Branches',   icon: GitBranch },
  { to: '/org/doctors',  label: 'Doctors',    icon: Users },
  { to: '/org/queue',    label: 'Live Queue', icon: ListOrdered },
];

export function OrgNav() {
  const clear = useAuthStore((s) => s.clear);
  const navigate = useNavigate();
  const [mobileOpen, setMobileOpen] = useState(false);

  const handleLogout = async () => {
    try { await authApi.logoutOrganization(); } catch { /* ignore */ }
    clear();
    navigate('/');
  };

  return (
    <>
      {/* Mobile Top Bar */}
      <div className="lg:hidden bg-[#0b3b36] text-white px-4 py-3 flex items-center justify-between sticky top-0 z-50">
        <div className="flex items-center gap-2.5">
          <div className="w-7 h-7 rounded-md bg-[#00685b] flex items-center justify-center text-white">
            <Plus size={16} strokeWidth={3} />
          </div>
          <span className="font-bold text-white text-base tracking-tight">HealthQueue</span>
        </div>
        <button onClick={() => setMobileOpen(!mobileOpen)} className="p-1.5 text-teal-200">
          {mobileOpen ? <X size={22} /> : <Menu size={22} />}
        </button>
      </div>

      {/* Sidebar for Desktop */}
      <aside className={`
        fixed lg:sticky top-0 left-0 z-40 h-screen w-64 bg-[#0b3b36] text-white flex flex-col justify-between p-6 shrink-0 transition-transform duration-200
        ${mobileOpen ? 'translate-x-0' : '-translate-x-full lg:translate-x-0'}
      `}>
        <div className="flex flex-col gap-8">
          {/* Top Brand Logo */}
          <div className="flex items-center gap-3 px-2">
            <div className="w-8 h-8 rounded-lg bg-[#00685b] flex items-center justify-center text-white shadow-xs">
              <Plus size={20} strokeWidth={3} />
            </div>
            <span className="font-bold text-white tracking-tight text-lg">HealthQueue</span>
          </div>

          {/* Navigation Links */}
          <nav className="flex flex-col gap-1.5">
            {navItems.map(({ to, label, icon: Icon }) => (
              <NavLink
                key={to}
                to={to}
                end={to === '/org'}
                onClick={() => setMobileOpen(false)}
                className={({ isActive }) =>
                  [
                    'flex items-center gap-3 px-3.5 py-2.5 rounded-xl text-xs font-semibold transition-all duration-150',
                    isActive
                      ? 'bg-[#14524c] text-white shadow-xs font-bold'
                      : 'text-teal-100/70 hover:text-white hover:bg-[#10443e]',
                  ].join(' ')
                }
              >
                <Icon size={16} />
                <span>{label}</span>
              </NavLink>
            ))}
          </nav>
        </div>

        {/* Bottom Status Card & Logout */}
        <div className="flex flex-col gap-3">
          <div className="bg-[#10443e] border border-[#165a52] rounded-2xl p-4 flex flex-col gap-1 text-xs">
            <div className="flex items-center gap-2 text-emerald-400 font-bold uppercase tracking-wider text-[10px]">
              <span className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse" />
              SYSTEM ONLINE
            </div>
            <span className="text-teal-100 font-medium text-[11px]">UNILAG Medical Centre</span>
          </div>

          <button
            onClick={handleLogout}
            className="flex items-center gap-2 px-3.5 py-2 rounded-xl text-xs font-medium text-teal-200 hover:text-rose-300 hover:bg-rose-900/20 transition-colors"
          >
            <LogOut size={15} />
            <span>Sign out</span>
          </button>
        </div>
      </aside>
    </>
  );
}

