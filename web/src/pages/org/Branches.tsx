import { useState } from 'react';
import { Plus, MapPin, Building2 } from 'lucide-react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { Modal } from '../../components/ui/Modal';
import { Badge } from '../../components/ui/Badge';
import { branchesApi } from '../../api/branches';
import { useAuthStore } from '../../store/authStore';
import { SAMPLE_BRANCHES } from '../../data/sampleData';

const schema = z.object({
  name: z.string().min(1, 'Branch name required'),
  address: z.string().min(1, 'Address required'),
  latitude: z.number().min(-90).max(90),
  longitude: z.number().min(-180).max(180),
});
type FormData = z.infer<typeof schema>;

export default function Branches() {
  const [isOpen, setIsOpen] = useState(false);
  const orgId = useAuthStore((s) => s.orgId);
  const queryClient = useQueryClient();

  const {
    register,
    handleSubmit,
    reset,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<FormData>({ resolver: zodResolver(schema) });

  const createBranch = useMutation({
    mutationFn: (data: FormData) =>
      branchesApi.createBranch({ ...data, organizationId: orgId || 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11' }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['branches'] });
      setIsOpen(false);
      reset();
    },
    onError: (err: Error) => {
      setError('root', { message: err.message });
    },
  });

  return (
    <div className="flex flex-col gap-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-2 mb-1">
            <Badge variant="purple">CLINIC FACILITIES</Badge>
          </div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight">Organization Branches</h1>
          <p className="text-slate-500 text-xs sm:text-sm mt-1">Configure physical medical branches and GPS coordinates.</p>
        </div>
        <Button onClick={() => setIsOpen(true)} leftIcon={<Plus size={15} />} className="bg-[#00685b] hover:bg-[#005247] text-white rounded-full shadow-sm">
          Add New Branch
        </Button>
      </div>

      {/* Branches List */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {SAMPLE_BRANCHES.map((b) => (
          <div key={b.id} className="bg-white border border-slate-200 rounded-2xl p-5 flex flex-col gap-3 hover:border-[#b2e2d8] hover:shadow-sm transition-all duration-200">
              <div className="flex items-start justify-between">
                <div className="flex items-center gap-3">
                  <div className="w-10 h-10 rounded-xl bg-[#e6f4f1] border border-[#b2e2d8] flex items-center justify-center text-[#00685b]">
                    <Building2 size={20} />
                  </div>
                  <div>
                    <h3 className="font-bold text-slate-900 text-base">{b.name}</h3>
                    <p className="text-xs text-slate-400 flex items-center gap-1 mt-0.5">
                      <MapPin size={13} className="text-slate-400" /> {b.address}
                    </p>
                  </div>
                </div>
                <Badge variant="purple">ID #{b.id}</Badge>
              </div>
              <div className="mt-2 pt-3 border-t border-slate-100 flex items-center justify-between text-xs text-slate-400">
                <span className="font-mono text-[11px] text-slate-400">GPS: {b.latitude}, {b.longitude}</span>
                <span className="text-[#00685b] font-semibold">Active Facility</span>
              </div>
          </div>
        ))}
      </div>

      {/* Create Modal */}
      <Modal isOpen={isOpen} onClose={() => setIsOpen(false)} title="Add New Clinic Branch" maxWidth="max-w-md">
        <form onSubmit={handleSubmit((d) => createBranch.mutate(d))} className="flex flex-col gap-4">
          <Input
            id="name"
            label="Branch Name"
            placeholder="Main Downtown Clinic"
            leftIcon={<Building2 size={14} />}
            error={errors.name?.message}
            {...register('name')}
          />
          <Input
            id="address"
            label="Physical Address"
            placeholder="123 Medical Center Way, Suite 400"
            leftIcon={<MapPin size={14} />}
            error={errors.address?.message}
            {...register('address')}
          />
          <div className="grid grid-cols-2 gap-3">
            <Input id="latitude"  label="Latitude"  type="number" step="any" placeholder="37.7749" error={errors.latitude?.message}  {...register('latitude', { valueAsNumber: true })} />
            <Input id="longitude" label="Longitude" type="number" step="any" placeholder="-122.4194" error={errors.longitude?.message} {...register('longitude', { valueAsNumber: true })} />
          </div>

          {errors.root && (
            <div className="text-xs text-rose-400 bg-rose-500/10 border border-rose-500/20 px-3.5 py-2.5 rounded-xl">
              {errors.root.message}
            </div>
          )}
          <div className="flex justify-end gap-3 mt-4 pt-3 border-t border-slate-100">
            <Button type="button" variant="outline" onClick={() => setIsOpen(false)}>Cancel</Button>
            <Button type="submit" isLoading={isSubmitting || createBranch.isPending} className="bg-[#00685b] text-white rounded-full">
              Create Branch
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
}

