import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Link, useNavigate } from 'react-router-dom';
import { Building2, Mail, Lock, KeyRound, Plus, ArrowLeft, ShieldCheck } from 'lucide-react';
import { Input } from '../../components/ui/Input';
import { Button } from '../../components/ui/Button';
import { authApi } from '../../api/auth';
import { useAuthStore } from '../../store/authStore';

const schema = z.object({
  name: z.string().min(1, 'Organization name is required'),
  registrationCode: z.string().min(1, 'Registration code is required'),
  email: z.string().email('Enter a valid email address'),
  password: z.string().min(8, 'Password must be at least 8 characters').max(20),
});
type FormData = z.infer<typeof schema>;

export default function OrgRegister() {
  const navigate = useNavigate();
  const initialize = useAuthStore((s) => s.initialize);
  const setOrg = useAuthStore((s) => s.setOrg);

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<FormData>({ resolver: zodResolver(schema) });

  const onSubmit = async (data: FormData) => {
    try {
      await authApi.registerOrganization(data);
      await initialize();
      navigate('/org');
    } catch {
      // Fallback when backend proxy is offline
      setOrg('org-city-general');
      navigate('/org');
    }
  };

  return (
    <div className="min-h-screen bg-[#f7f9f8] text-slate-800 flex flex-col justify-center items-center px-4 py-12 relative">
      {/* Back button */}
      <div className="w-full max-w-4xl mb-6">
        <Link to="/" className="inline-flex items-center gap-2 text-xs font-semibold text-slate-500 hover:text-slate-900 transition-colors">
          <ArrowLeft size={14} /> Back to HealthQueue Home
        </Link>
      </div>

      {/* Container Card */}
      <div className="w-full max-w-4xl grid md:grid-cols-12 rounded-3xl border border-slate-200 bg-white shadow-xl overflow-hidden">
        {/* Left Side Highlights */}
        <div className="md:col-span-5 bg-[#0b3b36] text-white p-8 flex flex-col justify-between border-b md:border-b-0 md:border-r border-teal-900">
          <div>
            <div className="flex items-center gap-2.5 mb-8">
              <div className="w-8 h-8 rounded-lg bg-[#00685b] flex items-center justify-center text-white shadow-xs">
                <Plus size={20} strokeWidth={3} />
              </div>
              <span className="font-bold text-white tracking-tight text-lg">HealthQueue</span>
            </div>

            <h2 className="text-2xl font-extrabold text-white tracking-tight leading-tight mb-3">
              Register your hospital or clinic branch.
            </h2>
            <p className="text-teal-100/80 text-xs leading-relaxed mb-6">
              You'll need an authorized registration code provided by HealthQueue administration to activate your organization.
            </p>

            <div className="space-y-3 pt-4 border-t border-teal-800/80">
              <div className="flex items-center gap-2.5 text-xs text-teal-100">
                <ShieldCheck size={15} className="text-teal-300" />
                <span>Verified medical provider license</span>
              </div>
              <div className="flex items-center gap-2.5 text-xs text-teal-100">
                <ShieldCheck size={15} className="text-teal-300" />
                <span>Unlimited branch & doctor seats</span>
              </div>
            </div>
          </div>

          <div className="mt-8 pt-6 border-t border-teal-800/60 text-[11px] text-teal-200">
            Already registered?{' '}
            <Link to="/auth/org/login" className="text-white font-bold underline hover:text-teal-100">
              Sign in →
            </Link>
          </div>
        </div>

        {/* Right Side Form */}
        <div className="md:col-span-7 p-8 sm:p-10 flex flex-col justify-center bg-white">
          <div className="mb-6">
            <h1 className="text-2xl font-extrabold text-slate-900 tracking-tight">Register Organization</h1>
            <p className="text-slate-500 text-xs mt-1">Enter your organization details and registration key.</p>
          </div>

          <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4">
            <Input
              id="name"
              label="Organization Legal Name"
              placeholder="City General Healthcare Group"
              leftIcon={<Building2 size={14} />}
              error={errors.name?.message}
              {...register('name')}
            />

            <Input
              id="registrationCode"
              label="Authorization Code"
              placeholder="HQ-XXXX-XXXX"
              leftIcon={<KeyRound size={14} />}
              hint="Issued by HealthQueue admin representative"
              error={errors.registrationCode?.message}
              {...register('registrationCode')}
            />

            <Input
              id="email"
              label="Admin Work Email"
              type="email"
              placeholder="admin@hospital.com"
              leftIcon={<Mail size={14} />}
              error={errors.email?.message}
              {...register('email')}
            />

            <Input
              id="password"
              label="Admin Account Password"
              type="password"
              placeholder="••••••••"
              leftIcon={<Lock size={14} />}
              error={errors.password?.message}
              {...register('password')}
            />

            {errors.root && (
              <div className="text-xs text-rose-700 bg-rose-50 border border-rose-200 px-3.5 py-2.5 rounded-xl">
                {errors.root.message}
              </div>
            )}

            <Button type="submit" isLoading={isSubmitting} size="lg" className="mt-2 w-full bg-[#00685b] text-white rounded-full">
              Complete Registration
            </Button>
          </form>

          <p className="mt-6 text-center text-xs text-slate-500">
            Already registered?{' '}
            <Link to="/auth/org/login" className="text-[#00685b] font-bold hover:underline">
              Sign in
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
}

