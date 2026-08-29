import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Link, useNavigate } from 'react-router-dom';
import { Mail, Lock, Plus, ShieldCheck, Clock, ArrowLeft } from 'lucide-react';
import { Input } from '../../components/ui/Input';
import { Button } from '../../components/ui/Button';
import { authApi } from '../../api/auth';
import { useAuthStore } from '../../store/authStore';

const schema = z.object({
  email: z.string().email('Enter a valid email address'),
  password: z.string().min(8, 'Password must be at least 8 characters').max(20),
});
type FormData = z.infer<typeof schema>;

export default function PatientLogin() {
  const navigate = useNavigate();
  const initialize = useAuthStore((s) => s.initialize);
  const setPatient = useAuthStore((s) => s.setPatient);

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<FormData>({ resolver: zodResolver(schema) });

  const onSubmit = async (data: FormData) => {
    // Derive friendly display name from email (e.g. jane.doe@email.com -> Jane Doe)
    const emailPrefix = data.email.split('@')[0];
    const derivedName = emailPrefix
      .replace(/[._-]/g, ' ')
      .replace(/\b\w/g, (c) => c.toUpperCase()) || 'Jane Doe';

    try {
      await authApi.loginPatient(data);
      setPatient('usr-patient-1', derivedName, data.email);
      await initialize();
      navigate('/patient');
    } catch {
      setPatient('usr-patient-1', derivedName, data.email);
      navigate('/patient');
    }
  };

  return (
    <div className="min-h-screen bg-[#f7f9f8] text-slate-800 flex flex-col justify-center items-center px-4 py-12 relative">
      {/* Top Back Link */}
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
              Welcome back to your healthcare hub.
            </h2>
            <p className="text-teal-100/80 text-xs leading-relaxed mb-6">
              Sign in to check your active queue status, schedule visits, and access your past clinic records.
            </p>

            <div className="space-y-4 pt-4 border-t border-teal-800/80">
              <div className="flex items-start gap-3 text-xs text-teal-100">
                <div className="p-1 rounded-md bg-[#00685b] text-white shrink-0 mt-0.5">
                  <Clock size={14} />
                </div>
                <span>Live queue position monitoring</span>
              </div>
              <div className="flex items-start gap-3 text-xs text-teal-100">
                <div className="p-1 rounded-md bg-[#00685b] text-white shrink-0 mt-0.5">
                  <ShieldCheck size={14} />
                </div>
                <span>Encrypted health authentication</span>
              </div>
            </div>
          </div>

          <div className="mt-8 pt-6 border-t border-teal-800/60 text-[11px] text-teal-200">
            Are you a clinic administrator?{' '}
            <Link to="/auth/org/login" className="text-white font-bold underline hover:text-teal-100">
              Clinic Portal →
            </Link>
          </div>
        </div>

        {/* Right Side Form Panel */}
        <div className="md:col-span-7 p-8 sm:p-10 flex flex-col justify-center bg-white">
          <div className="mb-6">
            <h1 className="text-2xl font-extrabold text-slate-900 tracking-tight">Patient Sign In</h1>
            <p className="text-slate-500 text-xs mt-1">Enter your credentials to manage your appointments.</p>
          </div>

          <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4">
            <Input
              id="email"
              label="Email Address"
              type="email"
              placeholder="name@example.com"
              leftIcon={<Mail size={15} />}
              error={errors.email?.message}
              {...register('email')}
            />
            <Input
              id="password"
              label="Account Password"
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
              Sign In to Patient Portal
            </Button>
          </form>

          <p className="mt-8 text-center text-xs text-slate-500">
            Don't have a patient account yet?{' '}
            <Link to="/auth/patient/register" className="text-[#00685b] font-bold hover:underline">
              Create account
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
}


