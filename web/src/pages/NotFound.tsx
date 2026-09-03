import { Link } from 'react-router-dom';
import { Home, ArrowLeft, HeartPulse } from 'lucide-react';
import { useAuthStore } from '../store/authStore';

export default function NotFound() {
  const role = useAuthStore((s) => s.role);
  
  const dashboardLink = role === 'patient' ? '/patient' : role === 'organization' ? '/org' : '/';

  return (
    <div className="min-h-screen bg-slate-50 flex items-center justify-center p-4">
      <div className="max-w-md w-full text-center space-y-8">
        <div className="flex justify-center">
          <div className="w-20 h-20 bg-rose-100 rounded-full flex items-center justify-center">
            <HeartPulse size={40} className="text-rose-500 animate-pulse" />
          </div>
        </div>
        
        <div className="space-y-3">
          <h1 className="text-7xl font-extrabold text-slate-900 tracking-tighter">404</h1>
          <h2 className="text-2xl font-bold text-slate-800">Page Not Found</h2>
          <p className="text-slate-500">
            We couldn't find the page you're looking for. It might have been moved, or the link you followed may be broken.
          </p>
        </div>

        <div className="flex flex-col sm:flex-row items-center justify-center gap-3 pt-4">
          <button 
            onClick={() => window.history.back()}
            className="w-full sm:w-auto px-6 py-2.5 rounded-xl border border-slate-200 text-slate-600 font-medium hover:bg-slate-100 hover:text-slate-900 transition-colors flex items-center justify-center gap-2"
          >
            <ArrowLeft size={18} />
            Go Back
          </button>
          
          <Link 
            to={dashboardLink}
            className="w-full sm:w-auto px-6 py-2.5 rounded-xl bg-[#00685b] text-white font-medium hover:bg-[#005248] transition-colors flex items-center justify-center gap-2 shadow-md shadow-teal-900/10"
          >
            <Home size={18} />
            {role ? 'Go to Dashboard' : 'Back to Home'}
          </Link>
        </div>
      </div>
    </div>
  );
}
