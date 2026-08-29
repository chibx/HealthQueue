import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Modal } from '../../components/ui/Modal';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { StatusBadge } from '../../components/ui/Badge';
import { Clock, UserCheck } from 'lucide-react';
import { useUpdateAppointmentStatus, useReassignDoctor } from '../../hooks/useAppointments';
import type { AppointmentResponse, AppointmentStatus } from '../../types';

const statusOptions: AppointmentStatus[] = [
  'SCHEDULED', 'IN_QUEUE', 'IN_PROGRESS', 'COMPLETED', 'OVERSTAYED', 'CANCELLED',
];

const statusSchema = z.object({
  status: z.enum(['SCHEDULED', 'IN_QUEUE', 'IN_PROGRESS', 'COMPLETED', 'OVERSTAYED', 'CANCELLED']),
  actualStartTime: z.string().optional(),
  actualEndTime: z.string().optional(),
});

const reassignSchema = z.object({
  newDoctorId: z.number().min(1, 'Doctor ID required'),
});

type StatusForm = z.infer<typeof statusSchema>;
type ReassignForm = z.infer<typeof reassignSchema>;

interface Props {
  appointment: AppointmentResponse;
  onClose: () => void;
}

export function UpdateStatus({ appointment, onClose }: Props) {
  const updateStatus = useUpdateAppointmentStatus(appointment.id);
  const reassign = useReassignDoctor(appointment.id);

  const statusForm = useForm<StatusForm>({
    resolver: zodResolver(statusSchema),
    defaultValues: { status: appointment.status },
  });

  const reassignForm = useForm<ReassignForm>({ resolver: zodResolver(reassignSchema) });

  const onStatusSubmit = async (data: StatusForm) => {
    try {
      await updateStatus.mutateAsync({ ...data, appointmentId: appointment.id });
    } catch { /* ignore fallback */ }
    onClose();
  };

  const onReassignSubmit = async (data: ReassignForm) => {
    try {
      await reassign.mutateAsync({ appointmentId: appointment.id, newDoctorId: data.newDoctorId });
    } catch { /* ignore fallback */ }
    onClose();
  };

  return (
    <Modal isOpen onClose={onClose} title={`Manage Ticket #${appointment.id}`} maxWidth="max-w-xl">
      <div className="flex flex-col gap-6">
        {/* Current status header */}
        <div className="flex items-center justify-between p-3.5 rounded-xl bg-slate-50 border border-slate-200">
          <span className="text-xs font-semibold uppercase text-slate-500">Current Ticket Status</span>
          <StatusBadge status={appointment.status} />
        </div>

        {/* Update status section */}
        <form onSubmit={statusForm.handleSubmit(onStatusSubmit)} className="flex flex-col gap-4">
          <div>
            <span className="text-xs font-bold uppercase tracking-wider text-slate-700 block mb-2">
              1. Transition Status State
            </span>
            <div className="grid grid-cols-2 sm:grid-cols-3 gap-2">
              {statusOptions.map((s) => {
                const selected = statusForm.watch('status') === s;
                return (
                  <label key={s} className="cursor-pointer">
                    <input type="radio" value={s} {...statusForm.register('status')} className="sr-only" />
                    <span
                      className={[
                        'block text-center px-3 py-2 rounded-xl text-xs font-bold border transition-all select-none',
                        selected
                          ? 'border-[#00685b] bg-[#e6f4f1] text-[#00685b] shadow-2xs font-bold'
                          : 'border-slate-200 bg-white text-slate-600 hover:border-slate-300 hover:bg-slate-50',
                      ].join(' ')}
                    >
                      {s.replace('_', ' ')}
                    </span>
                  </label>
                );
              })}
            </div>
          </div>

          <div className="grid grid-cols-2 gap-3 pt-2">
            <Input id="actualStartTime" label="Actual Start Time" type="datetime-local" leftIcon={<Clock size={14} />} {...statusForm.register('actualStartTime')} />
            <Input id="actualEndTime"   label="Actual End Time"   type="datetime-local" leftIcon={<Clock size={14} />} {...statusForm.register('actualEndTime')} />
          </div>

          <Button type="submit" size="sm" isLoading={updateStatus.isPending} className="self-end mt-1 bg-[#00685b] text-white rounded-full">
            Update Status Ticket
          </Button>
        </form>

        <hr className="border-slate-100" />

        {/* Reassign doctor section */}
        <form onSubmit={reassignForm.handleSubmit(onReassignSubmit)} className="flex flex-col gap-3">
          <span className="text-xs font-bold uppercase tracking-wider text-slate-700 block">
            2. Reassign Medical Specialist
          </span>
          <div className="flex gap-3 items-end">
            <Input
              id="newDoctorId"
              label="New Doctor ID"
              type="number"
              placeholder="Enter Doctor ID (e.g. 5)"
              leftIcon={<UserCheck size={14} />}
              error={reassignForm.formState.errors.newDoctorId?.message}
              className="flex-1"
              {...reassignForm.register('newDoctorId', { valueAsNumber: true })}
            />
            <Button
              type="submit"
              variant="outline"
              size="md"
              isLoading={reassign.isPending}
              className="rounded-full border-slate-200"
            >
              Reassign Doctor
            </Button>
          </div>
        </form>
      </div>
    </Modal>
  );
}

