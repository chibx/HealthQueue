import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useNavigate, useSearchParams } from 'react-router-dom';
import {
  Building2, Stethoscope, CheckCircle2, ArrowRight, ArrowLeft,
  Calendar, Clock, MapPin, Ticket, Check,
} from 'lucide-react';
import { Input } from '../../components/ui/Input';
import { Button } from '../../components/ui/Button';
import { useCreateAppointment } from '../../hooks/useAppointments';
import { useAuthStore } from '../../store/authStore';
import { useToast } from '../../components/ui/Toast';
import { SAMPLE_BRANCHES, SAMPLE_DOCTORS } from '../../data/sampleData';

const schema = z.object({
  branchId:            z.number().min(1, 'Select a branch'),
  doctorId:            z.number().min(1, 'Select a doctor'),
  scheduledStartTime:  z.string().min(1, 'Select start time'),
  scheduledEndTime:    z.string().min(1, 'Select end time'),
});
type FormData = z.infer<typeof schema>;

const STEPS = ['Choose Branch', 'Choose Doctor', 'Confirm'];

/* ── Step indicator ─────────────────────────────────────────────── */
function StepIndicator({ step }: { step: number }) {
  return (
    <div className="flex items-center gap-2 mb-8">
      {STEPS.map((label, i) => {
        const status = i < step ? 'done' : i === step ? 'active' : 'pending';
        return (
          <div key={label} className="flex items-center gap-2 flex-1 last:flex-none">
            <div className="flex flex-col items-center gap-1.5">
              <div className={[
                'w-8 h-8 rounded-full flex items-center justify-center text-xs font-bold border-2 transition-all',
                status === 'active'  ? 'bg-[#00685b] border-[#00685b] text-white' : '',
                status === 'done'    ? 'bg-[#e6f4f1] border-[#b2e2d8] text-[#00685b]' : '',
                status === 'pending' ? 'bg-white border-slate-200 text-slate-400' : '',
              ].join(' ')}>
                {status === 'done' ? <Check size={14} /> : i + 1}
              </div>
              <span className={`text-[10px] font-semibold whitespace-nowrap ${status === 'active' ? 'text-[#00685b]' : status === 'done' ? 'text-slate-500' : 'text-slate-300'}`}>
                {label}
              </span>
            </div>
            {i < STEPS.length - 1 && (
              <div className={`flex-1 h-px mt-[-12px] transition-all ${i < step ? 'bg-[#00685b]' : 'bg-slate-200'}`} />
            )}
          </div>
        );
      })}
    </div>
  );
}

export default function BookAppointment() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const userId           = useAuthStore((s) => s.userId);
  const createAppointment = useCreateAppointment();
  const { success, error: toastError } = useToast();

  const preselectedBranch = searchParams.get('branchId');
  const [step, setStep]   = useState(0);
  const [done, setDone]   = useState(false);
  const [ticketNo]        = useState(() => Math.floor(1000 + Math.random() * 9000));

  const { register, handleSubmit, watch, setValue, formState: { errors, isSubmitting } } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: {
      branchId:           preselectedBranch ? Number(preselectedBranch) : 1,
      doctorId:           1,
      scheduledStartTime: new Date(Date.now() + 30 * 60000).toISOString().slice(0, 16),
      scheduledEndTime:   new Date(Date.now() + 60 * 60000).toISOString().slice(0, 16),
    },
  });

  const selectedBranchId = watch('branchId');
  const selectedDoctorId = watch('doctorId');
  const selectedBranch   = SAMPLE_BRANCHES.find((b) => b.id === selectedBranchId);
  const selectedDoctor   = SAMPLE_DOCTORS.find((d) => d.id === selectedDoctorId);

  const onSubmit = async (data: FormData) => {
    try {
      if (userId) {
        await createAppointment.mutateAsync({
          userId,
          branchId: data.branchId,
          doctorId: data.doctorId,
          scheduledStartTime: new Date(data.scheduledStartTime).toISOString(),
          scheduledEndTime:   new Date(data.scheduledEndTime).toISOString(),
        });
      }
      success('Booking confirmed!', `Ticket #${ticketNo} — head to ${selectedBranch?.name ?? 'the clinic'}.`);
      setDone(true);
    } catch (err) {
      const msg = err instanceof Error ? err.message : 'Could not book appointment. Please try again.';
      toastError('Booking failed', msg);
    }
  };

  /* ── Success screen ─────────────────────────────────────────── */
  if (done) {
    return (
      <div className="flex flex-col items-center text-center gap-6 max-w-sm mx-auto pt-10 animate-fade-in-up">
        <div className="w-20 h-20 rounded-full bg-[#e6f4f1] flex items-center justify-center">
          <CheckCircle2 size={42} className="text-[#00685b]" />
        </div>
        <div>
          <h1 className="text-2xl font-extrabold text-slate-900 tracking-tight">You're booked!</h1>
          <p className="text-slate-500 text-sm mt-2 leading-relaxed">
            Your queue ticket has been reserved. Track your live position from the dashboard.
          </p>
        </div>
        <div className="bg-white border border-slate-200 rounded-2xl p-5 w-full text-left shadow-sm flex flex-col gap-3 text-xs">
          <div className="flex items-center gap-2 text-slate-500">
            <Ticket size={13} className="text-[#00685b]" />
            <span>Ticket <strong className="text-slate-900">#{ticketNo}</strong></span>
          </div>
          {selectedBranch && (
            <div className="flex items-center gap-2 text-slate-500">
              <MapPin size={13} className="text-[#00685b]" />
              <span>{selectedBranch.name}</span>
            </div>
          )}
          {selectedDoctor && (
            <div className="flex items-center gap-2 text-slate-500">
              <Stethoscope size={13} className="text-[#00685b]" />
              <span>Dr. {selectedDoctor.firstName} {selectedDoctor.lastName} · {selectedDoctor.specialty}</span>
            </div>
          )}
        </div>
        <Button
          size="md"
          className="w-full bg-[#00685b] text-white rounded-full"
          onClick={() => navigate('/patient')}
          rightIcon={<ArrowRight size={16} />}
        >
          Go to dashboard
        </Button>
      </div>
    );
  }

  return (
    <div className="flex flex-col gap-6 max-w-xl mx-auto animate-fade-in-up">
      {/* Header */}
      <div>
        <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight">Book Appointment</h1>
        <p className="text-slate-500 text-xs sm:text-sm mt-1">Reserve your queue slot at a nearby clinic.</p>
      </div>

      {/* Step indicator */}
      <StepIndicator step={step} />

      {/* Card */}
      <div className="bg-white border border-slate-200 rounded-3xl p-7 shadow-sm">
        <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-5">

          {/* ── Step 0: Choose branch ──────────────────────────── */}
          {step === 0 && (
            <div className="flex flex-col gap-5 animate-fade-in-up">
              <div>
                <p className="text-xs font-bold text-[#00685b] uppercase tracking-widest mb-1">Step 1</p>
                <h2 className="text-base font-bold text-slate-900">Select a branch</h2>
                <p className="text-xs text-slate-400 mt-0.5">Choose which clinic branch to visit.</p>
              </div>

              <div className="grid gap-3">
                {SAMPLE_BRANCHES.map((b) => (
                  <button
                    key={b.id}
                    type="button"
                    onClick={() => setValue('branchId', b.id)}
                    className={[
                      'w-full text-left p-4 rounded-2xl border-2 transition-all flex items-start gap-3',
                      selectedBranchId === b.id
                        ? 'border-[#00685b] bg-[#e6f4f1]'
                        : 'border-slate-200 bg-white hover:border-slate-300',
                    ].join(' ')}
                  >
                    <div className={`w-9 h-9 rounded-xl flex items-center justify-center shrink-0 ${selectedBranchId === b.id ? 'bg-[#00685b] text-white' : 'bg-slate-100 text-slate-500'}`}>
                      <Building2 size={16} />
                    </div>
                    <div>
                      <p className="font-bold text-slate-900 text-sm">{b.name}</p>
                      <p className="text-xs text-slate-400 mt-0.5 flex items-center gap-1">
                        <MapPin size={10} /> {b.address}
                      </p>
                    </div>
                    {selectedBranchId === b.id && <Check size={16} className="ml-auto text-[#00685b] shrink-0 mt-1" />}
                  </button>
                ))}
              </div>

              {/* Branch ID fallback input */}
              <div className="border-t border-slate-100 pt-4">
                <Input
                  id="branchId"
                  label="Or enter Branch ID manually"
                  type="number"
                  placeholder="e.g. 1"
                  leftIcon={<Building2 size={14} />}
                  error={errors.branchId?.message}
                  {...register('branchId', { valueAsNumber: true })}
                />
              </div>

              <Button
                type="button"
                size="md"
                className="w-full bg-[#00685b] text-white rounded-full"
                onClick={() => setStep(1)}
                rightIcon={<ArrowRight size={16} />}
              >
                Continue — Choose Doctor
              </Button>
            </div>
          )}

          {/* ── Step 1: Choose doctor ──────────────────────────── */}
          {step === 1 && (
            <div className="flex flex-col gap-5 animate-fade-in-up">
              <div>
                <p className="text-xs font-bold text-[#00685b] uppercase tracking-widest mb-1">Step 2</p>
                <h2 className="text-base font-bold text-slate-900">Choose a specialist</h2>
                <p className="text-xs text-slate-400 mt-0.5">Pick the doctor you'd like to see.</p>
              </div>

              <div className="grid gap-3">
                {SAMPLE_DOCTORS.filter((d) => d.isAvailable).map((doc) => (
                  <button
                    key={doc.id}
                    type="button"
                    onClick={() => setValue('doctorId', doc.id)}
                    className={[
                      'w-full text-left p-4 rounded-2xl border-2 transition-all flex items-center gap-3',
                      selectedDoctorId === doc.id
                        ? 'border-[#00685b] bg-[#e6f4f1]'
                        : 'border-slate-200 bg-white hover:border-slate-300',
                    ].join(' ')}
                  >
                    <div className={`w-10 h-10 rounded-full flex items-center justify-center shrink-0 text-sm font-extrabold ${selectedDoctorId === doc.id ? 'bg-[#00685b] text-white' : 'bg-slate-100 text-slate-600'}`}>
                      {doc.firstName[0]}{doc.lastName[0]}
                    </div>
                    <div className="flex-1">
                      <p className="font-bold text-slate-900 text-sm">Dr. {doc.firstName} {doc.lastName}</p>
                      <p className="text-xs text-[#00685b] font-medium mt-0.5">{doc.specialty}</p>
                    </div>
                    {selectedDoctorId === doc.id && <Check size={16} className="text-[#00685b] shrink-0" />}
                  </button>
                ))}
              </div>

              <Input
                id="doctorId"
                label="Or enter Doctor ID manually"
                type="number"
                placeholder="e.g. 1"
                leftIcon={<Stethoscope size={14} />}
                error={errors.doctorId?.message}
                {...register('doctorId', { valueAsNumber: true })}
              />

              <div className="flex gap-3">
                <Button type="button" variant="outline" size="md" className="flex-1 rounded-full" onClick={() => setStep(0)} leftIcon={<ArrowLeft size={16} />}>
                  Back
                </Button>
                <Button type="button" size="md" className="flex-1 bg-[#00685b] text-white rounded-full" onClick={() => setStep(2)} rightIcon={<ArrowRight size={16} />}>
                  Continue
                </Button>
              </div>
            </div>
          )}

          {/* ── Step 2: Confirm ────────────────────────────────── */}
          {step === 2 && (
            <div className="flex flex-col gap-5 animate-fade-in-up">
              <div>
                <p className="text-xs font-bold text-[#00685b] uppercase tracking-widest mb-1">Step 3</p>
                <h2 className="text-base font-bold text-slate-900">Confirm your booking</h2>
              </div>

              {/* Summary card */}
              <div className="bg-slate-50 border border-slate-200 rounded-2xl p-5 flex flex-col gap-3 text-xs">
                {selectedBranch && (
                  <div className="flex items-center gap-2 text-slate-600">
                    <Building2 size={13} className="text-[#00685b] shrink-0" />
                    <span className="font-semibold text-slate-900">{selectedBranch.name}</span>
                  </div>
                )}
                {selectedDoctor && (
                  <div className="flex items-center gap-2 text-slate-600">
                    <Stethoscope size={13} className="text-[#00685b] shrink-0" />
                    <span>Dr. {selectedDoctor.firstName} {selectedDoctor.lastName} · <span className="text-[#00685b]">{selectedDoctor.specialty}</span></span>
                  </div>
                )}
              </div>

              {/* Time window */}
              <div className="flex flex-col gap-3 border-t border-slate-100 pt-4">
                <p className="text-xs font-bold text-slate-700 uppercase tracking-wider flex items-center gap-1.5">
                  <Calendar size={13} /> Schedule Window
                </p>
                <div className="grid grid-cols-2 gap-3">
                  <Input
                    id="scheduledStartTime"
                    label="Start time"
                    type="datetime-local"
                    leftIcon={<Clock size={14} />}
                    error={errors.scheduledStartTime?.message}
                    {...register('scheduledStartTime')}
                  />
                  <Input
                    id="scheduledEndTime"
                    label="End time"
                    type="datetime-local"
                    leftIcon={<Clock size={14} />}
                    error={errors.scheduledEndTime?.message}
                    {...register('scheduledEndTime')}
                  />
                </div>
              </div>

              <div className="flex gap-3 pt-2">
                <Button type="button" variant="outline" size="md" className="flex-1 rounded-full" onClick={() => setStep(1)} leftIcon={<ArrowLeft size={16} />}>
                  Back
                </Button>
                <Button
                  type="submit"
                  isLoading={isSubmitting || createAppointment.isPending}
                  size="md"
                  className="flex-1 bg-[#00685b] text-white rounded-full"
                  rightIcon={<CheckCircle2 size={16} />}
                >
                  Confirm Booking
                </Button>
              </div>
            </div>
          )}
        </form>
      </div>
    </div>
  );
}
