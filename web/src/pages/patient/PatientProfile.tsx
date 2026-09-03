import { User, Mail, Shield, Smartphone } from 'lucide-react';
import { useAuthStore } from '../../store/authStore';
import { Input } from '../../components/ui/Input';
import { Button } from '../../components/ui/Button';

export default function PatientProfile() {
  const userName = useAuthStore((s) => s.userName) || 'Jane Doe';
  const userEmail = useAuthStore((s) => s.userEmail) || 'jane@example.com';

  return (
    <div className="flex flex-col gap-6 animate-fade-in-up max-w-4xl mx-auto w-full py-6">
      <div className="flex items-center gap-3">
        <div className="w-10 h-10 rounded-xl bg-[#e6f4f1] text-[#00685b] flex items-center justify-center">
          <User size={20} />
        </div>
        <div>
          <h1 className="text-2xl font-bold text-slate-900 tracking-tight">Your Profile</h1>
          <p className="text-slate-500 text-sm">Manage your personal information and preferences.</p>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {/* Sidebar */}
        <div className="col-span-1 flex flex-col gap-2">
          <button className="flex items-center gap-3 px-4 py-3 rounded-xl bg-white border border-[#00685b] text-[#00685b] font-bold shadow-sm">
            <User size={18} />
            Personal Info
          </button>
          <button className="flex items-center gap-3 px-4 py-3 rounded-xl bg-transparent border border-transparent text-slate-600 font-medium hover:bg-slate-100 transition-colors">
            <Shield size={18} />
            Password & Security
          </button>
        </div>

        {/* Content */}
        <div className="col-span-2 bg-white rounded-2xl border border-slate-200 p-6 shadow-sm">
          <h2 className="text-lg font-bold text-slate-900 border-b border-slate-100 pb-4 mb-4">Personal Information</h2>
          
          <form className="flex flex-col gap-5">
            <div className="flex items-center gap-4 mb-2">
              <div className="w-20 h-20 rounded-full bg-[#e6f4f1] border-2 border-[#b2e2d8] flex items-center justify-center text-[#00685b] text-2xl font-black shadow-inner">
                {userName.charAt(0)}
              </div>
              <Button type="button" variant="outline" size="sm" className="rounded-full">Change Photo</Button>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <Input id="firstName" label="Full Name" defaultValue={userName} leftIcon={<User size={14} />} />
              <Input id="phone" label="Phone Number" defaultValue="+234 800 000 0000" leftIcon={<Smartphone size={14} />} />
            </div>
            
            <Input id="email" label="Email Address" type="email" defaultValue={userEmail} leftIcon={<Mail size={14} />} />
            
            <div className="pt-4 border-t border-slate-100 flex justify-end">
              <Button type="button" className="bg-[#00685b] text-white rounded-full px-6">Save Changes</Button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}
