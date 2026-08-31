import { useState } from 'react';
import { MapPin, Navigation, Building2, ArrowRight, Compass, Search, Clock, Phone, Star } from 'lucide-react';
import { Button } from '../../components/ui/Button';
import { Badge } from '../../components/ui/Badge';
import { useGeolocation } from '../../hooks/useGeolocation';
import { useClosestBranches } from '../../hooks/useBranches';
import { SAMPLE_CLOSEST_BRANCHES } from '../../data/sampleData';
import { Link } from 'react-router-dom';

/* ── Skeleton card ─────────────────────────────────────────────── */
function BranchSkeleton() {
  return (
    <div className="bg-white border border-slate-200 rounded-2xl p-6 flex items-start gap-4">
      <div className="skeleton w-12 h-12 rounded-2xl shrink-0" />
      <div className="flex-1 flex flex-col gap-2">
        <div className="skeleton h-4 w-48 rounded" />
        <div className="skeleton h-3 w-64 rounded" />
        <div className="skeleton h-3 w-40 rounded" />
      </div>
      <div className="flex flex-col gap-2 items-end">
        <div className="skeleton h-6 w-20 rounded-full" />
        <div className="skeleton h-8 w-28 rounded-full" />
      </div>
    </div>
  );
}

export default function FindBranch() {
  const geo = useGeolocation();
  const { data: realBranches, isLoading } = useClosestBranches(geo.latitude, geo.longitude);
  const [searchQuery, setSearchQuery]   = useState('');
  const [maxDistance, setMaxDistance]   = useState<number | null>(null);

  const rawList = (realBranches && realBranches.length > 0) ? realBranches : SAMPLE_CLOSEST_BRANCHES;

  const filteredBranches = rawList.filter((item) => {
    const matchesSearch   = item.branch.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
                            item.branch.address.toLowerCase().includes(searchQuery.toLowerCase());
    const matchesDistance = maxDistance === null || item.distanceInKilometers <= maxDistance;
    return matchesSearch && matchesDistance;
  });

  const DISTANCE_FILTERS: { label: string; value: number | null }[] = [
    { label: 'All',    value: null },
    { label: '< 2 km', value: 2 },
    { label: '< 5 km', value: 5 },
  ];

  return (
    <div className="flex flex-col gap-6 animate-fade-in-up">

      {/* ── Header ─────────────────────────────────────────────── */}
      <div>
        <div className="flex items-center gap-2 mb-1">
          <Badge variant="green">CLINIC DISCOVERY</Badge>
          <span className="text-xs text-slate-400">· {rawList.length} verified branches</span>
        </div>
        <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight">Find Medical Centre Facilities</h1>
        <p className="text-slate-500 text-xs sm:text-sm mt-1">
          Detect your GPS position to view closest UNILAG Medical Centre locations sorted by distance.
        </p>
      </div>

      {/* ── GPS Location bar ────────────────────────────────────── */}
      <div className="bg-white border border-slate-200 rounded-2xl p-4 sm:p-5 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 shadow-sm">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-[#e6f4f1] border border-[#b2e2d8] flex items-center justify-center text-[#00685b] shrink-0">
            <Compass size={20} />
          </div>
          <div>
            <p className="text-xs font-semibold text-slate-500 uppercase tracking-wider">Your location</p>
            {geo.latitude && geo.longitude ? (
              <p className="text-sm font-bold text-[#00685b] mt-0.5 flex items-center gap-1.5">
                <MapPin size={13} />
                {geo.latitude.toFixed(4)}, {geo.longitude.toFixed(4)}
              </p>
            ) : (
              <p className="text-sm text-slate-500 mt-0.5">Lagos, Nigeria (default preview)</p>
            )}
            {geo.error && <p className="text-xs text-rose-500 mt-0.5">{geo.error}</p>}
          </div>
        </div>

        <Button
          onClick={geo.request}
          isLoading={geo.isLoading}
          variant="secondary"
          size="sm"
          leftIcon={<Navigation size={14} />}
          className="w-full sm:w-auto rounded-full"
        >
          {geo.latitude ? 'Refresh location' : 'Detect my location'}
        </Button>
      </div>

      {/* ── Search + Distance filters ───────────────────────────── */}
      <div className="flex flex-col sm:flex-row items-center gap-3">
        {/* Search input */}
        <div className="relative w-full sm:w-80">
          <Search size={15} className="absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400" />
          <input
            type="text"
            placeholder="Search clinic name or address…"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="w-full bg-white border border-slate-200 rounded-xl pl-9 pr-4 py-2.5 text-xs text-slate-800 placeholder:text-slate-400 focus:outline-none focus:border-[#00685b]/50 focus:ring-2 focus:ring-[#00685b]/10 transition shadow-sm"
          />
        </div>

        {/* Distance pills */}
        <div className="flex items-center gap-1.5 w-full sm:w-auto">
          {DISTANCE_FILTERS.map(({ label, value }) => (
            <button
              key={label}
              onClick={() => setMaxDistance(value)}
              className={[
                'px-3.5 py-1.5 rounded-full text-xs font-semibold border transition-all',
                maxDistance === value
                  ? 'bg-[#00685b] text-white border-[#00685b] shadow-sm'
                  : 'bg-white text-slate-500 border-slate-200 hover:border-[#00685b]/40 hover:text-[#00685b]',
              ].join(' ')}
            >
              {label}
            </button>
          ))}
        </div>
      </div>

      {/* ── Loading skeletons ───────────────────────────────────── */}
      {isLoading && (
        <div className="flex flex-col gap-4">
          {[1, 2, 3].map((i) => <BranchSkeleton key={i} />)}
        </div>
      )}

      {/* ── Results ─────────────────────────────────────────────── */}
      {!isLoading && filteredBranches.length > 0 && (
        <div className="grid gap-4">
          {filteredBranches.map(({ branch, distanceInKilometers }) => (
            <div key={branch.id} className="bg-white border border-slate-200 rounded-2xl p-6 hover:border-[#b2e2d8] hover:shadow-md transition-all duration-200">
              <div className="flex flex-col md:flex-row md:items-center justify-between gap-5">
                <div className="flex items-start gap-4">
                  <div className="w-12 h-12 rounded-2xl bg-[#e6f4f1] border border-[#b2e2d8] flex items-center justify-center shrink-0 text-[#00685b] mt-0.5">
                    <Building2 size={22} />
                  </div>
                  <div>
                    <div className="flex flex-wrap items-center gap-2 mb-1">
                      <h3 className="font-bold text-slate-900 text-base sm:text-lg">{branch.name}</h3>
                      <Badge variant="green" pulse>OPEN NOW</Badge>
                      <span className="inline-flex items-center gap-1 text-[11px] font-semibold text-amber-600 bg-amber-50 px-2 py-0.5 rounded-full border border-amber-200">
                        <Star size={10} fill="currentColor" />
                        4.9
                      </span>
                    </div>
                    <p className="text-xs text-slate-500 flex items-center gap-1.5">
                      <MapPin size={12} className="text-[#00685b] shrink-0" />
                      {branch.address}
                    </p>
                    <div className="flex items-center gap-4 text-[11px] text-slate-400 mt-2">
                      <span className="flex items-center gap-1">
                        <Clock size={11} className="text-slate-400" />
                        Mon–Sat 8:00 AM – 7:00 PM
                      </span>
                      <span className="flex items-center gap-1">
                        <Phone size={11} className="text-slate-400" />
                        +234 801 234 5678
                      </span>
                    </div>
                  </div>
                </div>

                <div className="flex md:flex-col items-center md:items-end justify-between md:justify-center border-t md:border-t-0 pt-4 md:pt-0 border-slate-100 shrink-0 gap-3">
                  <span className="text-[#00685b] font-extrabold text-xs bg-[#e6f4f1] px-3 py-1.5 rounded-full border border-[#b2e2d8]">
                    {distanceInKilometers.toFixed(1)} km away
                  </span>
                  <Link to={`/patient/book?branchId=${branch.id}`}>
                    <Button size="sm" className="bg-[#00685b] text-white rounded-full shadow-sm" rightIcon={<ArrowRight size={14} />}>
                      Book here
                    </Button>
                  </Link>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* ── Empty state ─────────────────────────────────────────── */}
      {!isLoading && filteredBranches.length === 0 && (
        <div className="bg-white border border-dashed border-slate-300 rounded-2xl p-16 flex flex-col items-center text-center gap-4">
          <div className="w-14 h-14 rounded-2xl bg-slate-100 flex items-center justify-center">
            <Building2 size={28} className="text-slate-400" />
          </div>
          <div>
            <p className="text-sm font-bold text-slate-900">No clinics match your filter</p>
            <p className="text-xs text-slate-400 mt-1 max-w-xs">Try clearing your search query or selecting "All" distance.</p>
          </div>
          <Button
            size="sm"
            variant="secondary"
            className="rounded-full"
            onClick={() => { setSearchQuery(''); setMaxDistance(null); }}
          >
            Reset filters
          </Button>
        </div>
      )}
    </div>
  );
}
