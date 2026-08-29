import { useState } from 'react';
import { ClipboardList, ArrowRight, Calendar, User, FileText, Download, Pill, Search } from 'lucide-react';
import { Badge } from '../../components/ui/Badge';
import { Button } from '../../components/ui/Button';
import { SAMPLE_VISITS, SAMPLE_DOCTORS } from '../../data/sampleData';
import { usePatientVisitHistory } from '../../hooks/useVisits';
import { Link } from 'react-router-dom';

export default function VisitHistory() {
  const [searchTerm, setSearchTerm] = useState('');
  const { data: liveVisits, isLoading } = usePatientVisitHistory();
  // Use real data from backend; fall back to sample data if offline
  const visits = (liveVisits && liveVisits.length > 0) ? liveVisits : SAMPLE_VISITS;

  const filteredVisits = visits.filter((v) =>
    v.visitNotes?.toLowerCase().includes(searchTerm.toLowerCase()) ||
    v.prescriptionDetails?.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <div className="flex flex-col gap-6 animate-fade-in-up">

      {/* ── Header ─────────────────────────────────────────────── */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-2 mb-1">
            <Badge variant="purple">MEDICAL RECORDS</Badge>
            <span className="text-xs text-slate-400">· {visits.length} archived visits</span>
          </div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight">Visit Records &amp; Prescriptions</h1>
          <p className="text-slate-500 text-xs sm:text-sm mt-1">
            Review completed consultation logs, doctor notes, and prescriptions.
          </p>
        </div>
        <Link to="/patient/find">
          <Button variant="secondary" size="sm" className="rounded-full" rightIcon={<ArrowRight size={14} />}>
            Book follow-up
          </Button>
        </Link>
      </div>

      {/* ── Search ─────────────────────────────────────────────── */}
      <div className="relative w-full sm:w-80">
        <Search size={15} className="absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400" />
        <input
          type="text"
          placeholder="Filter by notes or prescriptions…"
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          className="w-full bg-white border border-slate-200 rounded-xl pl-9 pr-4 py-2.5 text-xs text-slate-800 placeholder:text-slate-400 focus:outline-none focus:border-[#00685b]/50 focus:ring-2 focus:ring-[#00685b]/10 transition shadow-sm"
        />
      </div>

      {/* ── Records list ───────────────────────────────────────── */}
      {isLoading ? (
        <div className="grid gap-4">
          {[1, 2].map((i) => (
            <div key={i} className="bg-white border border-slate-200 rounded-2xl p-6 flex flex-col gap-4">
              <div className="flex items-center justify-between pb-4 border-b border-slate-100">
                <div className="flex items-center gap-3">
                  <div className="skeleton w-9 h-9 rounded-xl shrink-0" />
                  <div className="flex flex-col gap-2">
                    <div className="skeleton h-4 w-36 rounded" />
                    <div className="skeleton h-3 w-24 rounded" />
                  </div>
                </div>
                <div className="skeleton h-6 w-20 rounded-full" />
              </div>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                <div className="skeleton h-16 rounded-xl" />
                <div className="skeleton h-16 rounded-xl" />
              </div>
            </div>
          ))}
        </div>
      ) : filteredVisits.length > 0 ? (
        <div className="grid gap-4">
          {filteredVisits.map((visit) => {
            const doctor = SAMPLE_DOCTORS.find((d) => d.id === visit.doctorId);
            return (
              <div key={visit.id} className="bg-white border border-slate-200 rounded-2xl p-6 hover:border-slate-300 hover:shadow-sm transition-all duration-200">
                {/* Card header */}
                <div className="flex flex-wrap items-center justify-between gap-3 border-b border-slate-100 pb-4 mb-4">
                  <div className="flex items-center gap-2.5">
                    <div className="w-9 h-9 rounded-xl bg-violet-50 border border-violet-100 flex items-center justify-center text-violet-500">
                      <FileText size={16} />
                    </div>
                    <div>
                      <h3 className="font-bold text-slate-900 text-sm">Consultation Record #{visit.id}</h3>
                      <p className="text-[11px] text-slate-400 flex items-center gap-1.5 mt-0.5">
                        <Calendar size={11} />
                        {new Date(visit.recordedAt).toLocaleDateString('en-GB', {
                          weekday: 'short', year: 'numeric', month: 'short', day: 'numeric',
                        })}
                      </p>
                    </div>
                  </div>
                  <div className="flex items-center gap-2">
                    <Badge variant="green">COMPLETED</Badge>
                    <button
                      onClick={() => alert(`Downloading PDF for Visit #${visit.id}`)}
                      className="p-2 rounded-lg bg-slate-50 border border-slate-200 text-slate-400 hover:text-slate-700 hover:border-slate-300 transition-colors"
                      title="Download PDF"
                    >
                      <Download size={13} />
                    </button>
                  </div>
                </div>

                {/* Info grid */}
                <div className="grid grid-cols-1 md:grid-cols-2 gap-3 text-xs">
                  {/* Doctor */}
                  <div className="flex items-center gap-3 bg-slate-50 border border-slate-100 rounded-xl p-3.5">
                    <User size={15} className="text-[#00685b] shrink-0" />
                    <div>
                      <span className="text-[10px] text-slate-400 uppercase font-semibold block">Consulting Doctor</span>
                      <span className="font-bold text-slate-900">{doctor?.firstName} {doctor?.lastName}</span>
                      <span className="text-slate-400 text-[11px] block">{doctor?.specialty}</span>
                    </div>
                  </div>

                  {/* Prescription */}
                  <div className="flex items-start gap-3 bg-amber-50 border border-amber-100 rounded-xl p-3.5">
                    <Pill size={15} className="text-amber-500 shrink-0 mt-0.5" />
                    <div>
                      <span className="text-[10px] text-slate-400 uppercase font-semibold block">Rx Prescription</span>
                      <span className="text-slate-700 leading-relaxed">{visit.prescriptionDetails ?? 'No medications prescribed.'}</span>
                    </div>
                  </div>
                </div>

                {/* Clinical notes */}
                {visit.visitNotes && (
                  <div className="mt-3 bg-slate-50 border border-slate-100 rounded-xl p-4 text-xs">
                    <span className="text-[10px] text-slate-400 uppercase font-semibold block mb-1.5">Clinical Notes</span>
                    <p className="text-slate-600 leading-relaxed italic">"{visit.visitNotes}"</p>
                  </div>
                )}
              </div>
            );
          })}
        </div>
      ) : (
        <div className="bg-white border border-dashed border-slate-300 rounded-2xl p-16 flex flex-col items-center text-center gap-4">
          <div className="w-14 h-14 rounded-2xl bg-slate-100 flex items-center justify-center">
            <ClipboardList size={28} className="text-slate-400" />
          </div>
          <div>
            <p className="text-sm font-bold text-slate-900">No medical records found</p>
            <p className="text-xs text-slate-400 mt-1 max-w-xs leading-relaxed">
              {searchTerm
                ? 'Try a different search term.'
                : 'Once you complete clinic visits, your records will appear here.'}
            </p>
          </div>
          {!searchTerm && (
            <Link to="/patient/find">
              <Button size="sm" className="bg-[#00685b] text-white rounded-full" rightIcon={<ArrowRight size={14} />}>
                Find clinics near me
              </Button>
            </Link>
          )}
        </div>
      )}
    </div>
  );
}
