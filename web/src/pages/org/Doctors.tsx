import { useState } from 'react';
import { Plus, Stethoscope, User, Building2 } from 'lucide-react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { Modal } from '../../components/ui/Modal';
import { Badge } from '../../components/ui/Badge';
import { doctorsApi } from '../../api/doctors';
import { SAMPLE_DOCTORS } from '../../data/sampleData';

const schema = z.object({
  branchId: z.number().min(1, 'Branch ID required'),
  firstName: z.string().min(1, 'First name required'),
  lastName: z.string().min(1, 'Last name required'),
  specialty: z.string().min(1, 'Specialty required'),
});
type FormData = z.infer<typeof schema>;

export default function Doctors() {
  const [isOpen, setIsOpen] = useState(false);
  const queryClient = useQueryClient();

  const {
    register,
    handleSubmit,
    reset,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<FormData>({ resolver: zodResolver(schema) });

  const createDoctor = useMutation({
    mutationFn: doctorsApi.createDoctor,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['doctors'] });
      setIsOpen(false);
      reset();
    },
    onError: (err: Error) => setError('root', { message: err.message }),
  });

  return (
    <div className="flex flex-col gap-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-2 mb-1">
            <Badge variant="blue">CLINICAL STAFF</Badge>
          </div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight">Doctors Directory</h1>
          <p className="text-slate-500 text-xs sm:text-sm mt-1">Manage practicing medical personnel across your branch network.</p>
        </div>
        <Button onClick={() => setIsOpen(true)} leftIcon={<Plus size={15} />} className="bg-[#00685b] hover:bg-[#005247] text-white rounded-full shadow-sm">
          Register Doctor
        </Button>
      </div>

      {/* Doctors Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {SAMPLE_DOCTORS.map((doc) => (
          <div key={doc.id} className="bg-white border border-slate-200 rounded-2xl p-5 flex flex-col gap-3 hover:border-[#b2e2d8] hover:shadow-sm transition-all duration-200">
              <div className="flex items-start justify-between">
                <div className="flex items-center gap-3">
                  <div className="w-11 h-11 rounded-full bg-[#e6f4f1] border border-[#b2e2d8] flex items-center justify-center text-[#00685b] text-sm font-extrabold">
                    {doc.firstName[0]}{doc.lastName[0]}
                  </div>
                  <div>
                    <h3 className="font-bold text-slate-900 text-base">{doc.firstName} {doc.lastName}</h3>
                    <p className="text-xs text-[#00685b] font-medium mt-0.5">{doc.specialty}</p>
                  </div>
                </div>
                <Badge variant={doc.isAvailable ? 'green' : 'slate'} pulse={doc.isAvailable}>
                  {doc.isAvailable ? 'AVAILABLE' : 'OFF DUTY'}
                </Badge>
              </div>
              <div className="mt-2 pt-3 border-t border-slate-100 flex items-center justify-between text-xs text-slate-400">
                <span className="flex items-center gap-1 text-[11px]">
                  <Building2 size={12} className="text-slate-400" /> Branch #{doc.branchId}
                </span>
                <span className="text-slate-400 text-[11px]">ID: #{doc.id}</span>
              </div>
          </div>
        ))}
      </div>

      {/* Create Modal */}
      <Modal isOpen={isOpen} onClose={() => setIsOpen(false)} title="Register New Doctor" maxWidth="max-w-md">
        <form onSubmit={handleSubmit((d) => createDoctor.mutate(d))} className="flex flex-col gap-4">
          <Input
            id="branchId"
            label="Assigned Branch ID"
            type="number"
            placeholder="e.g. 1"
            leftIcon={<Building2 size={14} />}
            error={errors.branchId?.message}
            {...register('branchId', { valueAsNumber: true })}
          />
          <div className="grid grid-cols-2 gap-3">
            <Input id="firstName" label="First Name" placeholder="Dr. Sarah" leftIcon={<User size={14} />} error={errors.firstName?.message} {...register('firstName')} />
            <Input id="lastName"  label="Last Name"  placeholder="Jenkins" leftIcon={<User size={14} />} error={errors.lastName?.message}  {...register('lastName')} />
          </div>
          <Input
            id="specialty"
            label="Medical Specialty"
            placeholder="Cardiology, Pediatrics, General Practice..."
            leftIcon={<Stethoscope size={14} />}
            error={errors.specialty?.message}
            {...register('specialty')}
          />

          {errors.root && (
            <div className="text-xs text-rose-400 bg-rose-500/10 border border-rose-500/20 px-3.5 py-2.5 rounded-xl">
              {errors.root.message}
            </div>
          )}
          <div className="flex justify-end gap-3 mt-4 pt-3 border-t border-slate-100">
            <Button type="button" variant="outline" onClick={() => setIsOpen(false)}>Cancel</Button>
            <Button type="submit" isLoading={isSubmitting || createDoctor.isPending} className="bg-[#00685b] text-white rounded-full">
              Save Doctor Record
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
}

