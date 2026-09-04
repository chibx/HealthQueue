import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Link, useNavigate } from 'react-router-dom';
import { Mail, Lock, Plus, Building2, ShieldCheck, ArrowLeft } from 'lucide-react';
import { Input } from '../../components/ui/Input';
import { Button } from '../../components/ui/Button';
import { authApi } from '../../api/auth';
import { useAuthStore } from '../../store/authStore';
import { getErrorMessage, getFieldErrors } from '../../utils/errors';

const schema = z.object({
  email: z.string().email('Enter a valid organization email'),
  password: z.string().min(8, 'Password must be at least 8 characters').max(20),
});
type FormData = z.infer<typeof schema>;

export default function OrgLogin() {
  const navigate = useNavigate();
  const initialize = useAuthStore((s) => s.initialize);

  const {
    register,
    handleSubmit,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<FormData>({ resolver: zodResolver(schema) });

  const onSubmit = async (data: FormData) => {
    try {
      await authApi.loginOrganization(data);
      await initialize();
      navigate('/org');
    } catch (err) {
      const fieldErrors = getFieldErrors(err);
      if (Object.keys(fieldErrors).length > 0) {
        Object.entries(fieldErrors).forEach(([field, message]) =>
          setError(field as keyof FormData, { message })
        );
      } else {
        setError('root', { message: getErrorMessage(err) });
      }
    }
  };

  return (
    <div className="min-h-screen bg-[#f7f9f8] text-slate-800 flex flex-col justify-center items-center px-4 py-12 relative">
      {/* Top back button */}
      <div className="w-full max-w-4xl mb-6">
        <Link to="/" className="inline-flex items-center gap-2 text-xs font-semibold text-slate-500 hover:text-slate-900 transition-colors">
          <ArrowLeft size={14} /> Back to HealthQueue Home
        </Link>
      </div>

      {/* Main Container Card */}
      <div className="w-full max-w-4xl grid md:grid-cols-12 rounded-3xl border border-slate-200 bg-white shadow-xl overflow-hidden">
        {/* Left Side Highlight Panel */}
        <div className="md:col-span-5 bg-[#0b3b36] text-white p-8 flex flex-col justify-between border-b md:border-b-0 md:border-r border-teal-900">
          <div>
            <div className="flex items-center gap-2.5 mb-8">
              <div className="w-8 h-8 rounded-lg bg-[#00685b] flex items-center justify-center text-white shadow-xs">
                <Plus size={20} strokeWidth={3} />
              </div>
              <span className="font-bold text-white tracking-tight text-lg">HealthQueue</span>
            </div>

            <h2 className="text-2xl font-extrabold text-white tracking-tight leading-tight mb-3">
              Manage clinic branches & patient queues seamlessly.
            </h2>
            <p className="text-teal-100/80 text-xs leading-relaxed mb-6">
              Access your operational portal to reassign doctors, monitor live patient arrival flow, and update ticket statuses in real time.
            </p>

            <div className="space-y-4 pt-4 border-t border-teal-800/80">
              <div className="flex items-start gap-3 text-xs text-teal-100">
                <div className="p-1 rounded-md bg-[#00685b] text-white shrink-0 mt-0.5">
                  <Building2 size={14} />
                </div>
                <span>Multi-branch clinic management</span>
              </div>
              <div className="flex items-start gap-3 text-xs text-teal-100">
                <div className="p-1 rounded-md bg-[#00685b] text-white shrink-0 mt-0.5">
                  <ShieldCheck size={14} />
                </div>
                <span>HIPAA compliant clinic admin portal</span>
              </div>
            </div>
          </div>

          <div className="mt-8 pt-6 border-t border-teal-800/60 text-[11px] text-teal-200 flex flex-col gap-1">
            <span>
              Are you a patient?{' '}
              <Link to="/auth/patient/login" className="text-white font-bold underline hover:text-teal-100">
                Patient Portal →
              </Link>
            </span>
            <span>
              Are you a doctor or nurse?{' '}
              <Link to="/auth/staff/login" className="text-white font-bold underline hover:text-teal-100">
                Staff Portal →
              </Link>
            </span>
          </div>
        </div>

        {/* Right Side Form Panel */}
        <div className="md:col-span-7 p-8 sm:p-10 flex flex-col justify-center bg-white">
          <div className="mb-6">
            <h1 className="text-2xl font-extrabold text-slate-900 tracking-tight">Organization Sign In</h1>
            <p className="text-slate-500 text-xs mt-1">Enter your clinic admin credentials.</p>
          </div>

          <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4">
            <Input
              id="email"
              label="Organization Email"
              type="email"
              placeholder="admin@hospital.com"
              leftIcon={<Mail size={15} />}
              error={errors.email?.message}
              {...register('email')}
            />
            <Input
              id="password"
              label="Password"
              type="password"
              placeholder="••••••••"
              leftIcon={<Lock size={15} />}
              error={errors.password?.message}
              {...register('password')}
            />

            {errors.root && (
              <div className="text-xs text-rose-700 bg-rose-50 border border-rose-200 px-3.5 py-2.5 rounded-xl">
                {errors.root.message}
              </div>
            )}

            <Button type="submit" isLoading={isSubmitting} size="lg" className="mt-2 w-full bg-[#00685b] text-white rounded-full">
              Sign In to Clinic Console
            </Button>
          </form>

          <p className="mt-8 text-center text-xs text-slate-500">
            Need to register a new organization?{' '}
            <Link to="/auth/org/register" className="text-[#00685b] font-bold hover:underline">
              Register here
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
}

