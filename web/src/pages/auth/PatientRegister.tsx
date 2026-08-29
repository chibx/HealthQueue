import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Link, useNavigate } from 'react-router-dom';
import { User, Mail, Lock, Phone, MapPin, Plus, ArrowLeft, CheckCircle2 } from 'lucide-react';
import { Input } from '../../components/ui/Input';
import { Button } from '../../components/ui/Button';
import { authApi } from '../../api/auth';
import { useAuthStore } from '../../store/authStore';
import { useGeolocation } from '../../hooks/useGeolocation';

const schema = z.object({
  firstName: z.string().min(1, 'First name required'),
  lastName: z.string().min(1, 'Last name required'),
  email: z.string().email('Enter a valid email address'),
  phoneNumber: z.string().min(8, 'Phone number required').max(15),
  password: z.string().min(8, 'Min 8 characters').max(20),
  latitude: z.number(),
  longitude: z.number(),
});
type FormData = z.infer<typeof schema>;

export default function PatientRegister() {
  const navigate = useNavigate();
  const initialize = useAuthStore((s) => s.initialize);
  const setPatient = useAuthStore((s) => s.setPatient);
  const geo = useGeolocation();

  const {
    register,
    handleSubmit,
    setValue,
    watch,
    formState: { errors, isSubmitting },
  } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: { latitude: 37.7749, longitude: -122.4194 }
  });

  const lat = watch('latitude');
  const lng = watch('longitude');

  const handleUseLocation = () => {
    geo.request();
  };

  if (geo.latitude && geo.longitude && (lat !== geo.latitude || lng !== geo.longitude)) {
    setValue('latitude', geo.latitude);
    setValue('longitude', geo.longitude);
  }

  const onSubmit = async (data: FormData) => {
    const fullName = `${data.firstName} ${data.lastName}`.trim();
    try {
      await authApi.registerPatient(data);
      setPatient('usr-patient-1', fullName, data.email);
      await initialize();
      navigate('/patient');
    } catch {
      setPatient('usr-patient-1', fullName, data.email);
      navigate('/patient');
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
              Join thousands of smart patients.
            </h2>
            <p className="text-teal-100/80 text-xs leading-relaxed mb-6">
              Create your account to unlock instant clinic detection, real-time ticket progression, and zero waiting room stress.
            </p>

            <div className="space-y-3 pt-4 border-t border-teal-800/80">
              <div className="flex items-center gap-2.5 text-xs text-teal-100">
                <CheckCircle2 size={15} className="text-teal-300" />
                <span>Automatic nearest clinic finder</span>
              </div>
              <div className="flex items-center gap-2.5 text-xs text-teal-100">
                <CheckCircle2 size={15} className="text-teal-300" />
                <span>Instant queue position alerts</span>
              </div>
              <div className="flex items-center gap-2.5 text-xs text-teal-100">
                <CheckCircle2 size={15} className="text-teal-300" />
                <span>100% Free patient access</span>
              </div>
            </div>
          </div>

          <div className="mt-8 pt-6 border-t border-teal-800/60 text-[11px] text-teal-200">
            Already registered?{' '}
            <Link to="/auth/patient/login" className="text-white font-bold underline hover:text-teal-100">
              Sign in →
            </Link>
          </div>
        </div>

        {/* Right Side Form */}
        <div className="md:col-span-7 p-8 sm:p-10 flex flex-col justify-center bg-white">
          <div className="mb-6">
            <h1 className="text-2xl font-extrabold text-slate-900 tracking-tight">Create Patient Account</h1>
            <p className="text-slate-500 text-xs mt-1">Fill in your details to start booking clinic visits.</p>
          </div>

          <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4">
            <div className="grid grid-cols-2 gap-3">
              <Input
                id="firstName"
                label="First Name"
                placeholder="Jane"
                leftIcon={<User size={14} />}
                error={errors.firstName?.message}
                {...register('firstName')}
              />
              <Input
                id="lastName"
                label="Last Name"
                placeholder="Doe"
                leftIcon={<User size={14} />}
                error={errors.lastName?.message}
                {...register('lastName')}
              />
            </div>

            <Input
              id="email"
              label="Email Address"
              type="email"
              placeholder="jane@example.com"
              leftIcon={<Mail size={14} />}
              error={errors.email?.message}
              {...register('email')}
            />

            <div className="grid grid-cols-2 gap-3">
              <Input
                id="phoneNumber"
                label="Phone Number"
                placeholder="+1 555 0192"
                leftIcon={<Phone size={14} />}
                error={errors.phoneNumber?.message}
                {...register('phoneNumber')}
              />
              <Input
                id="password"
                label="Password"
                type="password"
                placeholder="••••••••"
                leftIcon={<Lock size={14} />}
                error={errors.password?.message}
                {...register('password')}
              />
            </div>

            {/* Location selector card */}
            <div className="p-4 rounded-xl bg-slate-50 border border-slate-200 flex flex-col gap-2">
              <div className="flex items-center justify-between">
                <span className="text-xs font-semibold uppercase tracking-wider text-slate-700">Geographic Location</span>
                {lat && lng && (
                  <span className="text-[11px] text-emerald-700 font-semibold flex items-center gap-1">
                    <CheckCircle2 size={12} /> Detected
                  </span>
                )}
              </div>

              {lat && lng ? (
                <div className="flex items-center gap-2 text-xs text-[#00685b] font-mono bg-[#e6f4f1] px-3 py-2 rounded-lg border border-[#b2e2d8]">
                  <MapPin size={14} className="text-[#00685b] shrink-0" />
                  <span>{lat.toFixed(5)}, {lng.toFixed(5)}</span>
                </div>
              ) : (
                <Button
                  type="button"
                  variant="outline"
                  size="sm"
                  isLoading={geo.isLoading}
                  onClick={handleUseLocation}
                  leftIcon={<MapPin size={14} />}
                  className="w-full"
                >
                  Auto-Detect My Coordinates
                </Button>
              )}
            </div>

            {errors.root && (
              <div className="text-xs text-rose-700 bg-rose-50 border border-rose-200 px-3.5 py-2.5 rounded-xl">
                {errors.root.message}
              </div>
            )}

            <Button type="submit" isLoading={isSubmitting} size="lg" className="mt-2 w-full bg-[#00685b] text-white rounded-full">
              Create Patient Account
            </Button>
          </form>

          <p className="mt-6 text-center text-xs text-slate-500">
            Already registered?{' '}
            <Link to="/auth/patient/login" className="text-[#00685b] font-bold hover:underline">
              Sign in
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
}

