import { useState } from 'react';
import { Search, Activity, Ticket, ArrowRight, UserCheck } from 'lucide-react';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { UpdateStatus } from './UpdateStatus';
import { useToast } from '../../components/ui/Toast';
import { SAMPLE_APPOINTMENTS } from '../../data/sampleData';
import type { AppointmentResponse } from '../../types';

export default function FastStatus() {
  const [ticketId, setTicketId] = useState('');
  const [selected, setSelected] = useState<AppointmentResponse | null>(null);
  const { error } = useToast();

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    if (!ticketId) return;
    
    // Replace with real API call later
    const found = SAMPLE_APPOINTMENTS.find((a) => a.id.toString() === ticketId);
    if (found) {
      setSelected(found);
    } else {
      error('Not Found', `Ticket #${ticketId} could not be found.`);
    }
  };

  return (
    <div className="max-w-2xl mx-auto flex flex-col gap-8 animate-fade-in-up py-6">
      <div className="text-center space-y-2">
        <div className="inline-flex items-center justify-center w-16 h-16 rounded-full bg-[#e6f4f1] text-[#00685b] mb-2 shadow-xs">
          <Activity size={32} />
        </div>
        <h1 className="text-3xl font-extrabold text-slate-900 tracking-tight">Fast Status Update</h1>
        <p className="text-slate-500 text-sm">
          Quickly scan or enter a Ticket ID to update its status or reassign the doctor.
        </p>
      </div>

      <div className="bg-white p-6 sm:p-10 rounded-3xl border border-slate-200 shadow-xl shadow-slate-200/50">
        <form onSubmit={handleSearch} className="flex flex-col sm:flex-row gap-3">
          <Input
            id="ticketId"
            type="number"
            placeholder="Enter Ticket ID (e.g. 101)"
            value={ticketId}
            onChange={(e) => setTicketId(e.target.value)}
            leftIcon={<Ticket size={18} className="text-slate-400" />}
            className="flex-1 text-lg py-3"
            autoFocus
          />
          <Button 
            type="submit" 
            size="lg" 
            className="bg-[#00685b] text-white whitespace-nowrap h-[52px]"
            rightIcon={<ArrowRight size={18} />}
          >
            Find Ticket
          </Button>
        </form>

        <div className="mt-10 grid grid-cols-1 sm:grid-cols-2 gap-4 border-t border-slate-100 pt-8">
          <div className="flex items-start gap-3 p-4 rounded-2xl bg-slate-50 border border-slate-100">
            <div className="p-2 bg-white rounded-lg shadow-2xs text-[#00685b]">
              <Search size={20} />
            </div>
            <div>
              <h3 className="font-bold text-slate-900 text-sm">Quick Lookup</h3>
              <p className="text-xs text-slate-500 mt-0.5">Find any active or past ticket instantly without scrolling the queue.</p>
            </div>
          </div>
          <div className="flex items-start gap-3 p-4 rounded-2xl bg-slate-50 border border-slate-100">
            <div className="p-2 bg-white rounded-lg shadow-2xs text-[#00685b]">
              <UserCheck size={20} />
            </div>
            <div>
              <h3 className="font-bold text-slate-900 text-sm">Instant Reassign</h3>
              <p className="text-xs text-slate-500 mt-0.5">Quickly pass a patient to another doctor if their current doctor is busy.</p>
            </div>
          </div>
        </div>
      </div>

      {selected && (
        <UpdateStatus 
          appointment={selected} 
          onClose={() => {
            setSelected(null);
            setTicketId('');
          }} 
        />
      )}
    </div>
  );
}
