import { Ticket, Clock, CheckCircle2 } from 'lucide-react';
import { useAuthStore } from '../../store/authStore';
import { SAMPLE_APPOINTMENTS, SAMPLE_DOCTORS } from '../../data/sampleData';

export default function LiveQueue() {
  const userId = useAuthStore((s) => s.userId);
  const userName = useAuthStore((s) => s.userName);
  
  // Find active appointment for this user
  const myActiveApts = SAMPLE_APPOINTMENTS.filter(
    (a) => a.userId === userId && (a.status === 'IN_QUEUE' || a.status === 'IN_PROGRESS' || a.status === 'SCHEDULED')
  );

  return (
    <div className="flex flex-col gap-6 animate-fade-in-up max-w-3xl mx-auto py-6">
      <div className="text-center space-y-3 mb-4">
        <h1 className="text-3xl font-extrabold text-slate-900 tracking-tight">Your Live Queue</h1>
        <p className="text-slate-500 text-sm">
          Track your active appointments and wait times in real-time.
        </p>
      </div>

      {myActiveApts.length === 0 ? (
        <div className="bg-white border border-dashed border-slate-300 rounded-2xl p-16 flex flex-col items-center text-center gap-4">
          <div className="w-14 h-14 rounded-2xl bg-[#e6f4f1] flex items-center justify-center">
            <CheckCircle2 size={28} className="text-[#00685b]" />
          </div>
          <div>
            <p className="text-lg font-bold text-slate-900">You're all caught up, {userName?.split(' ')[0]}!</p>
            <p className="text-sm text-slate-500 mt-1">You don't have any active tickets in the queue right now.</p>
          </div>
        </div>
      ) : (
        <div className="flex flex-col gap-6">
          {myActiveApts.map((apt) => {
            const doctor = SAMPLE_DOCTORS.find((d) => d.id === apt.doctorId);
            const isNext = apt.status === 'IN_PROGRESS';
            
            return (
              <div key={apt.id} className="relative">
                {/* Active Indicator */}
                {isNext && (
                  <div className="absolute -top-3 left-1/2 -translate-x-1/2 bg-rose-500 text-white text-[10px] font-black uppercase tracking-widest px-3 py-1 rounded-full shadow-lg shadow-rose-500/30 z-10 animate-bounce">
                    It's your turn!
                  </div>
                )}
                
                <div className={`
                  bg-white rounded-3xl border p-6 sm:p-8 transition-all
                  ${isNext ? 'border-rose-300 shadow-2xl shadow-rose-900/5 scale-[1.02]' : 'border-slate-200 shadow-xl shadow-slate-200/50'}
                `}>
                  <div className="flex flex-col md:flex-row items-center justify-between gap-6">
                    
                    {/* Status Circle */}
                    <div className="flex flex-col items-center justify-center shrink-0">
                      <div className={`
                        w-24 h-24 rounded-full border-4 flex items-center justify-center flex-col
                        ${isNext ? 'border-rose-500 text-rose-500 bg-rose-50' : 'border-[#00685b] text-[#00685b] bg-[#e6f4f1]'}
                      `}>
                        <span className="text-xs font-bold uppercase tracking-widest mb-0.5">Ticket</span>
                        <span className="text-3xl font-black leading-none">#{apt.id}</span>
                      </div>
                      <div className="mt-3 text-center">
                        <span className={`inline-flex items-center gap-1.5 font-bold text-xs uppercase tracking-wider px-2.5 py-1 rounded-full ${isNext ? 'text-rose-600 bg-rose-100' : 'text-emerald-700 bg-emerald-100'}`}>
                          {isNext ? 'Go to Doctor' : 'Please Wait'}
                        </span>
                      </div>
                    </div>
                    
                    {/* Details */}
                    <div className="flex-1 flex flex-col gap-4 text-center md:text-left w-full">
                      <div>
                        <h3 className="text-xl font-bold text-slate-900">{doctor ? `Dr. ${doctor.firstName} ${doctor.lastName}` : `Doctor #${apt.doctorId}`}</h3>
                        <p className="text-sm text-slate-500 font-medium">{doctor?.specialty || 'General Practitioner'}</p>
                      </div>
                      
                      <div className="flex flex-wrap items-center justify-center md:justify-start gap-4">
                        <div className="flex items-center gap-2 bg-slate-50 px-3 py-2 rounded-xl border border-slate-100">
                          <Clock size={16} className="text-amber-500" />
                          <div className="flex flex-col text-left">
                            <span className="text-[10px] font-bold text-slate-400 uppercase">Estimated Wait</span>
                            <span className="text-sm font-black text-slate-800">{isNext ? '0 min' : '~15 mins'}</span>
                          </div>
                        </div>
                        
                        <div className="flex items-center gap-2 bg-slate-50 px-3 py-2 rounded-xl border border-slate-100">
                          <Ticket size={16} className="text-[#00685b]" />
                          <div className="flex flex-col text-left">
                            <span className="text-[10px] font-bold text-slate-400 uppercase">People Ahead</span>
                            <span className="text-sm font-black text-slate-800">{isNext ? '0' : '3'}</span>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}
