import { Settings, Shield, Bell, Building } from 'lucide-react';
import { Input } from '../../components/ui/Input';
import { Button } from '../../components/ui/Button';

export default function OrgSettings() {
  return (
    <div className="flex flex-col gap-6 animate-fade-in-up max-w-4xl mx-auto w-full">
      <div className="flex items-center gap-3">
        <div className="w-10 h-10 rounded-xl bg-[#e6f4f1] text-[#00685b] flex items-center justify-center">
          <Settings size={20} />
        </div>
        <div>
          <h1 className="text-2xl font-bold text-slate-900 tracking-tight">Organization Settings</h1>
          <p className="text-slate-500 text-sm">Manage your clinic's preferences and profile.</p>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {/* Sidebar */}
        <div className="col-span-1 flex flex-col gap-2">
          <button className="flex items-center gap-3 px-4 py-3 rounded-xl bg-white border border-[#00685b] text-[#00685b] font-bold shadow-sm">
            <Building size={18} />
            Clinic Profile
          </button>
          <button className="flex items-center gap-3 px-4 py-3 rounded-xl bg-transparent border border-transparent text-slate-600 font-medium hover:bg-slate-100 transition-colors">
            <Shield size={18} />
            Security
          </button>
          <button className="flex items-center gap-3 px-4 py-3 rounded-xl bg-transparent border border-transparent text-slate-600 font-medium hover:bg-slate-100 transition-colors">
            <Bell size={18} />
            Notifications
          </button>
        </div>

        {/* Content */}
        <div className="col-span-2 bg-white rounded-2xl border border-slate-200 p-6 shadow-sm">
          <h2 className="text-lg font-bold text-slate-900 border-b border-slate-100 pb-4 mb-4">Clinic Profile</h2>
          
          <form className="flex flex-col gap-5">
            <Input id="orgName" label="Organization Name" defaultValue="UNILAG Medical Centre" />
            <Input id="orgEmail" label="Contact Email" type="email" defaultValue="admin@unilagmed.edu.ng" />
            
            <div>
              <label className="block text-xs font-bold uppercase tracking-wider text-slate-600 mb-1.5 ml-1">
                Description
              </label>
              <textarea 
                className="w-full bg-white border border-slate-200 rounded-xl px-4 py-2.5 text-sm text-slate-800 placeholder:text-slate-400 focus:outline-none focus:border-[#00685b]/50 focus:ring-2 focus:ring-[#00685b]/10 transition min-h-[100px]"
                defaultValue="Main medical facility serving the University of Lagos community."
              />
            </div>
            
            <div className="pt-4 border-t border-slate-100 flex justify-end">
              <Button type="button" className="bg-[#00685b] text-white">Save Changes</Button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}
