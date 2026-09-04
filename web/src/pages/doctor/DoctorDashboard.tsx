import { useState } from 'react';
import {
  Users,
  Clock,
  CheckCircle2,
  Stethoscope,
  Play,
  FileText,
  Activity,
} from 'lucide-react';
import { Badge } from '../../components/ui/Badge';
import { Button } from '../../components/ui/Button';
import { Modal } from '../../components/ui/Modal';
import { useAuthStore } from '../../store/authStore';
import { SAMPLE_APPOINTMENTS } from '../../data/sampleData';
import type { AppointmentResponse } from '../../types';

export default function DoctorDashboard() {
  const userName = useAuthStore((s) => s.userName);
  const [appointments, setAppointments] = useState<AppointmentResponse[]>(SAMPLE_APPOINTMENTS);
  const [activeAppointment, setActiveAppointment] = useState<AppointmentResponse | null>(null);
  const [notesModalOpen, setNotesModalOpen] = useState(false);
  const [visitNotes, setVisitNotes] = useState('');
  const [prescription, setPrescription] = useState('');
  const [successMsg, setSuccessMsg] = useState<string | null>(null);

  const waitingQueue = appointments.filter((a) => a.status === 'IN_QUEUE' || a.status === 'SCHEDULED');
  const inProgress = appointments.find((a) => a.status === 'IN_PROGRESS');
  const completedToday = appointments.filter((a) => a.status === 'COMPLETED');

  const handleCallNext = () => {
    if (waitingQueue.length === 0) return;
    const nextApt = waitingQueue[0];
    setAppointments((prev) =>
      prev.map((a) => (a.id === nextApt.id ? { ...a, status: 'IN_PROGRESS' as const, actualStartTime: new Date().toISOString() } : a))
    );
    setActiveAppointment({ ...nextApt, status: 'IN_PROGRESS' });
    setSuccessMsg(`Called Ticket #${nextApt.id} to Consultation Room.`);
    setTimeout(() => setSuccessMsg(null), 4000);
  };

  const handleCompleteConsultation = (apt: AppointmentResponse) => {
    setActiveAppointment(apt);
    setNotesModalOpen(true);
  };

  const handleSaveVisit = () => {
    if (!activeAppointment) return;
    setAppointments((prev) =>
      prev.map((a) =>
        a.id === activeAppointment.id
          ? { ...a, status: 'COMPLETED' as const, actualEndTime: new Date().toISOString() }
          : a
      )
    );
    setNotesModalOpen(false);
    setActiveAppointment(null);
    setVisitNotes('');
    setPrescription('');
    setSuccessMsg(`Consultation for Ticket #${activeAppointment.id} saved & completed.`);
    setTimeout(() => setSuccessMsg(null), 4000);
  };

  return (
    <div className="flex flex-col gap-8 animate-fade-in-up">
      {/* Top Welcome Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-white border border-slate-200 rounded-3xl p-6 sm:p-8 shadow-xs">
        <div className="flex items-start gap-4">
          <div className="w-12 h-12 rounded-2xl bg-[#00685b] flex items-center justify-center text-white shadow-sm shrink-0">
            <Stethoscope size={24} />
          </div>
          <div>
            <div className="flex items-center gap-2 mb-1">
              <Badge variant="green" pulse>ON DUTY</Badge>
              <span className="text-xs text-slate-400">UNILAG Medical Centre</span>
            </div>
            <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight">
              {userName ? `${userName}` : 'Clinical Consultation Station'}
            </h1>
            <p className="text-slate-500 text-xs sm:text-sm mt-1">
              Manage patient arrival triage, consultation timers, and clinical prescriptions.
            </p>
          </div>
        </div>

        <div className="flex items-center gap-3">
          <Button
            onClick={handleCallNext}
            disabled={waitingQueue.length === 0}
            className="bg-[#00685b] hover:bg-[#005247] text-white rounded-full px-5 py-2.5 font-bold shadow-xs text-xs"
            leftIcon={<Play size={14} />}
          >
            Call Next Patient
          </Button>
        </div>
      </div>

      {successMsg && (
        <div className="p-4 bg-emerald-50 border border-emerald-200 text-emerald-800 text-xs font-semibold rounded-2xl flex items-center gap-2">
          <CheckCircle2 size={16} className="text-emerald-600" />
          {successMsg}
        </div>
      )}

      {/* Stats Counter Bar */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <div className="bg-white border border-slate-200 rounded-2xl p-5 flex items-center justify-between shadow-2xs">
          <div>
            <span className="text-xs font-medium text-slate-500">In Waiting Queue</span>
            <div className="text-2xl font-extrabold text-slate-900 mt-1">{waitingQueue.length} patients</div>
          </div>
          <div className="w-10 h-10 rounded-xl bg-amber-50 text-amber-700 flex items-center justify-center font-bold">
            <Users size={18} />
          </div>
        </div>

        <div className="bg-white border border-slate-200 rounded-2xl p-5 flex items-center justify-between shadow-2xs">
          <div>
            <span className="text-xs font-medium text-slate-500">Active Consultation</span>
            <div className="text-2xl font-extrabold text-teal-700 mt-1">
              {inProgress ? `Ticket #${inProgress.id}` : 'None active'}
            </div>
          </div>
          <div className="w-10 h-10 rounded-xl bg-teal-50 text-teal-700 flex items-center justify-center font-bold">
            <Activity size={18} />
          </div>
        </div>

        <div className="bg-white border border-slate-200 rounded-2xl p-5 flex items-center justify-between shadow-2xs">
          <div>
            <span className="text-xs font-medium text-slate-500">Completed Today</span>
            <div className="text-2xl font-extrabold text-slate-900 mt-1">{completedToday.length} visits</div>
          </div>
          <div className="w-10 h-10 rounded-xl bg-emerald-50 text-emerald-700 flex items-center justify-center font-bold">
            <CheckCircle2 size={18} />
          </div>
        </div>
      </div>

      {/* Active Consultation Spotlight */}
      {inProgress && (
        <div className="bg-gradient-to-br from-[#0b3b36] to-[#124e47] text-white rounded-3xl p-6 sm:p-8 shadow-lg">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-6 border-b border-teal-800/80">
            <div>
              <span className="text-xs font-bold text-teal-200 tracking-wider uppercase flex items-center gap-1.5 mb-2">
                <span className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse" />
                Active Consultation In Progress
              </span>
              <h2 className="text-2xl font-extrabold text-white">Ticket #{inProgress.id}</h2>
              <p className="text-xs text-teal-100/70 mt-1">
                Started: {inProgress.actualStartTime ? new Date(inProgress.actualStartTime).toLocaleTimeString() : 'Just now'} · Branch #{inProgress.branchId}
              </p>
            </div>
            <Button
              onClick={() => handleCompleteConsultation(inProgress)}
              className="bg-white text-[#0b3b36] hover:bg-teal-50 font-bold rounded-full px-6 py-2.5 text-xs shadow-md"
              leftIcon={<FileText size={14} />}
            >
              Complete &amp; Add Visit Notes
            </Button>
          </div>
          <div className="mt-4 pt-2 flex items-center gap-2 text-xs text-teal-100/90">
            <Clock size={14} />
            <span>Consultation in progress. Remember to record diagnosis and prescriptions before concluding ticket.</span>
          </div>
        </div>
      )}

      {/* Live Patient Queue List */}
      <div className="bg-white border border-slate-200 rounded-3xl p-6 sm:p-8 shadow-xs">
        <div className="flex items-center justify-between mb-6">
          <div>
            <h3 className="text-lg font-bold text-slate-900">Today's Patient Queue</h3>
            <p className="text-xs text-slate-500 mt-0.5">All waiting, active, and completed consultation tickets</p>
          </div>
          <Badge variant="slate">{appointments.length} Total Today</Badge>
        </div>

        <div className="divide-y divide-slate-100">
          {appointments.map((apt) => {
            const isAptInProgress = apt.status === 'IN_PROGRESS';
            return (
              <div
                key={apt.id}
                className="py-4 flex flex-col sm:flex-row sm:items-center justify-between gap-4 hover:bg-slate-50/60 px-3 rounded-2xl transition-colors"
              >
                <div className="flex items-center gap-3.5">
                  <div className="w-10 h-10 rounded-xl bg-[#e6f4f1] text-[#00685b] font-bold text-xs flex items-center justify-center border border-[#b2e2d8]">
                    #{apt.id}
                  </div>
                  <div>
                    <div className="flex items-center gap-2">
                      <span className="font-bold text-slate-900 text-sm">Ticket #{apt.id}</span>
                      <Badge
                        variant={
                          apt.status === 'COMPLETED'
                            ? 'green'
                            : apt.status === 'IN_PROGRESS'
                            ? 'blue'
                            : apt.status === 'IN_QUEUE'
                            ? 'yellow'
                            : 'slate'
                        }
                        pulse={isAptInProgress}
                      >
                        {apt.status.replace(/_/g, ' ')}
                      </Badge>
                    </div>
                    <span className="text-xs text-slate-400 mt-0.5 block">
                      Scheduled: {new Date(apt.scheduledStartTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })} · Branch #{apt.branchId}
                    </span>
                  </div>
                </div>

                <div className="flex items-center gap-2">
                  {apt.status === 'IN_QUEUE' || apt.status === 'SCHEDULED' ? (
                    <Button
                      size="sm"
                      variant="outline"
                      onClick={() => {
                        setAppointments((prev) =>
                          prev.map((a) => (a.id === apt.id ? { ...a, status: 'IN_PROGRESS', actualStartTime: new Date().toISOString() } : a))
                        );
                        setActiveAppointment({ ...apt, status: 'IN_PROGRESS' });
                      }}
                      className="rounded-full text-xs font-semibold text-[#00685b] border-[#b2e2d8] hover:bg-[#e6f4f1]"
                    >
                      Call Now
                    </Button>
                  ) : isAptInProgress ? (
                    <Button
                      size="sm"
                      onClick={() => handleCompleteConsultation(apt)}
                      className="rounded-full text-xs font-bold bg-[#00685b] text-white shadow-xs"
                    >
                      Conclude Visit
                    </Button>
                  ) : (
                    <span className="text-xs font-medium text-emerald-600 flex items-center gap-1">
                      <CheckCircle2 size={13} /> Completed
                    </span>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      </div>

      {/* Record Visit Notes Modal */}
      <Modal
        isOpen={notesModalOpen}
        onClose={() => setNotesModalOpen(false)}
        title={`Record Clinical Consultation (Ticket #${activeAppointment?.id})`}
        maxWidth="max-w-lg"
      >
        <div className="flex flex-col gap-4">
          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">Visit Diagnosis &amp; Clinical Notes</label>
            <textarea
              value={visitNotes}
              onChange={(e) => setVisitNotes(e.target.value)}
              rows={3}
              placeholder="E.g. Patient presents with mild seasonal allergic rhinitis. Clear lungs, normal vitals."
              className="w-full text-xs p-3 rounded-xl border border-slate-200 focus:outline-none focus:border-[#00685b] focus:ring-1 focus:ring-[#00685b]"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">Prescription &amp; Dosage Details</label>
            <textarea
              value={prescription}
              onChange={(e) => setPrescription(e.target.value)}
              rows={2}
              placeholder="E.g. Cetirizine 10mg once daily for 5 days. Paracetamol 500mg PRN for headache."
              className="w-full text-xs p-3 rounded-xl border border-slate-200 focus:outline-none focus:border-[#00685b] focus:ring-1 focus:ring-[#00685b]"
            />
          </div>

          <div className="flex justify-end gap-3 mt-4 pt-3 border-t border-slate-100">
            <Button type="button" variant="outline" onClick={() => setNotesModalOpen(false)}>
              Cancel
            </Button>
            <Button
              type="button"
              onClick={handleSaveVisit}
              className="bg-[#00685b] hover:bg-[#005247] text-white rounded-full font-bold text-xs shadow-xs"
            >
              Save &amp; Complete Ticket
            </Button>
          </div>
        </div>
      </Modal>
    </div>
  );
}
