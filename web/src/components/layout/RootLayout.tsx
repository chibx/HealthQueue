import { Outlet } from 'react-router-dom';
import { useAuthStore } from '../../store/authStore';
import { PatientNav } from './PatientNav';
import { OrgNav } from './OrgNav';

export function RootLayout() {
  const role = useAuthStore((s) => s.role);

  if (role === 'organization') {
    return (
      <div className="min-h-screen bg-[#f7f9f8] text-slate-800 flex flex-col lg:flex-row relative selection:bg-[#e6f4f1]">
        <OrgNav />
        <main className="flex-1 p-6 md:p-10 max-w-7xl w-full mx-auto">
          <Outlet />
        </main>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-[#f7f9f8] text-slate-800 flex flex-col relative selection:bg-[#e6f4f1]">
      <div className="relative z-10 flex flex-col min-h-screen">
        {role === 'patient' && <PatientNav />}
        <main className="flex-1 container mx-auto px-6 py-8 max-w-6xl">
          <Outlet />
        </main>
      </div>
    </div>
  );
}

