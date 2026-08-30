import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import {
  Plus,
  MapPin,
  ShieldCheck,
  Ticket,
  Bell,
  Navigation,
  Stethoscope,
  Check,
  Menu,
  X,
  ChevronRight,
} from 'lucide-react';
import { Button } from '../components/ui/Button';

const NAV_LINKS = [
  { href: '#features', label: 'Features' },
  { href: '#workflow', label: 'Workflow' },
  { href: '#pricing',  label: 'Pricing' },
];

const FEATURE_PILLS = [
  { icon: Navigation,   label: 'GPS proximity search' },
  { icon: Ticket,       label: 'Real-time position counter' },
  { icon: Stethoscope,  label: 'Specialist registry' },
  { icon: ShieldCheck,  label: 'Privacy-first records' },
];

const PRICING = [
  {
    title: 'Patient',
    price: 'Free',
    sub: 'forever',
    perks: ['Find nearby clinics', 'Reserve queue tickets', 'Live position tracking', 'Visit history & prescriptions'],
    cta: 'Create patient account',
    href: '/auth/patient/register',
    highlight: false,
  },
  {
    title: 'Clinic',
    price: '₦9,999',
    sub: 'per month',
    perks: ['Unlimited branches', 'Doctor registry', 'Live queue console', 'Priority support'],
    cta: 'Register your clinic',
    href: '/auth/org/register',
    highlight: true,
  },
];

export default function Landing() {
  const [simulatedQueue, setSimulatedQueue] = useState(2);
  const [simulatedWait, setSimulatedWait] = useState(12);
  const [mobileOpen, setMobileOpen] = useState(false);
  const [scrolled, setScrolled] = useState(false);

  useEffect(() => {
    const interval = setInterval(() => {
      setSimulatedQueue((prev) => (prev > 1 ? prev - 1 : 3));
      setSimulatedWait((prev)  => (prev > 5 ? prev - 5 : 15));
    }, 4500);
    return () => clearInterval(interval);
  }, []);

  useEffect(() => {
    const onScroll = () => setScrolled(window.scrollY > 10);
    window.addEventListener('scroll', onScroll, { passive: true });
    return () => window.removeEventListener('scroll', onScroll);
  }, []);

  return (
    <div className="min-h-screen bg-[#f7f9f8] text-slate-800 flex flex-col relative font-sans">

      {/* ── Header ──────────────────────────────────────────────── */}
      <header className={`bg-white sticky top-0 z-50 transition-shadow duration-200 ${scrolled ? 'shadow-sm border-b border-slate-100' : 'border-b border-transparent'}`}>
        <div className="container mx-auto px-6 max-w-6xl flex items-center justify-between h-20">
          {/* Logo */}
          <Link to="/" className="flex items-center gap-2.5 shrink-0">
            <div className="w-8 h-8 rounded-lg bg-[#00685b] flex items-center justify-center text-white shadow-sm">
              <Plus size={20} strokeWidth={3} />
            </div>
            <span className="font-bold text-slate-900 text-xl tracking-tight">HealthQueue</span>
          </Link>

          {/* Desktop nav */}
          <nav className="hidden md:flex items-center gap-8 text-xs font-semibold text-slate-500">
            {NAV_LINKS.map((l) => (
              <a key={l.href} href={l.href} className="hover:text-[#00685b] transition-colors">
                {l.label}
              </a>
            ))}
          </nav>

          {/* Desktop CTAs */}
          <div className="hidden md:flex items-center gap-3">
            <Link to="/auth/patient/login">
              <Button variant="outline" size="sm" className="rounded-full text-slate-700 border-slate-200">
                Patient login
              </Button>
            </Link>
            <Link to="/auth/org/login">
              <Button variant="outline" size="sm" className="rounded-full text-slate-700 border-slate-200">
                Clinic login
              </Button>
            </Link>
            <Link to="/auth/patient/register">
              <Button variant="primary" size="sm" className="rounded-full bg-[#00685b] text-white">
                Get started
              </Button>
            </Link>
          </div>

          {/* Mobile hamburger */}
          <button
            onClick={() => setMobileOpen(!mobileOpen)}
            className="md:hidden p-2 rounded-xl text-slate-600 hover:bg-slate-100 transition-colors"
            aria-label="Toggle menu"
          >
            {mobileOpen ? <X size={22} /> : <Menu size={22} />}
          </button>
        </div>

        {/* Mobile menu */}
        {mobileOpen && (
          <div className="md:hidden border-t border-slate-100 bg-white px-6 py-5 flex flex-col gap-4 shadow-lg animate-fade-in-up">
            {NAV_LINKS.map((l) => (
              <a
                key={l.href}
                href={l.href}
                onClick={() => setMobileOpen(false)}
                className="text-sm font-semibold text-slate-700 hover:text-[#00685b] transition-colors"
              >
                {l.label}
              </a>
            ))}
            <div className="border-t border-slate-100 pt-4 flex flex-col gap-2">
              <Link to="/auth/patient/login" onClick={() => setMobileOpen(false)}>
                <Button variant="outline" size="sm" className="w-full rounded-full">Patient login</Button>
              </Link>
              <Link to="/auth/org/login" onClick={() => setMobileOpen(false)}>
                <Button variant="outline" size="sm" className="w-full rounded-full">Clinic login</Button>
              </Link>
              <Link to="/auth/patient/register" onClick={() => setMobileOpen(false)}>
                <Button variant="primary" size="sm" className="w-full rounded-full bg-[#00685b] text-white">
                  Get started — it's free
                </Button>
              </Link>
            </div>
          </div>
        )}
      </header>

      {/* ── Hero ────────────────────────────────────────────────── */}
      <section className="container mx-auto px-6 max-w-6xl pt-16 pb-20 grid lg:grid-cols-12 gap-12 items-center">
        {/* Left column */}
        <div className="lg:col-span-7 flex flex-col items-start text-left animate-fade-in-up">
          <div className="mb-6 inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-[#e6f4f1] text-[#00685b] text-[11px] font-extrabold tracking-wider uppercase">
            <span className="w-1.5 h-1.5 rounded-full bg-[#00685b] animate-pulse" />
            Care, without the waiting room
          </div>

          <h1 className="text-4xl sm:text-5xl lg:text-6xl font-extrabold tracking-tight text-slate-900 leading-[1.1] mb-6">
            Skip the<br />waiting room.<br />
            <span className="text-[#00685b]">Keep your place.</span>
          </h1>

          <p className="text-slate-500 text-base sm:text-lg leading-relaxed mb-8 max-w-lg font-normal">
            Reserve a clinic ticket remotely, follow your live position, and arrive exactly when care is ready for you.
          </p>

          <div className="flex flex-wrap gap-3 items-center">
            <Link to="/auth/patient/register">
              <Button size="lg" className="bg-[#00685b] hover:bg-[#005247] text-white rounded-full px-7 shadow-lg shadow-teal-900/15">
                Book as patient
              </Button>
            </Link>
            <Link to="/auth/org/register">
              <Button size="lg" variant="outline" className="bg-white text-slate-700 border-slate-200 hover:bg-slate-50 rounded-full px-7">
                Register clinic
                <ChevronRight size={16} className="ml-1" />
              </Button>
            </Link>
          </div>

          {/* Social proof row */}
          <div className="mt-10 flex items-center gap-6 text-xs text-slate-400">
            <span className="flex items-center gap-1.5"><span className="font-bold text-slate-700">4.9/5</span> rating</span>
            <span className="w-px h-4 bg-slate-200" />
            <span className="flex items-center gap-1.5"><span className="font-bold text-slate-700">85%</span> wait time saved</span>
            <span className="w-px h-4 bg-slate-200" />
            <span className="flex items-center gap-1.5"><span className="font-bold text-slate-700">120+</span> partner clinics</span>
          </div>
        </div>

        {/* Right column — widget */}
        <div className="lg:col-span-5 relative">
          <div className="bg-white border border-slate-200 rounded-3xl p-6 shadow-2xl relative overflow-hidden">
            {/* Decorative circle */}
            <div className="absolute -top-10 -right-10 w-40 h-40 rounded-full bg-[#e6f4f1] opacity-60 pointer-events-none" />

            {/* Widget header */}
            <div className="flex items-center justify-between mb-5 relative">
              <span className="font-bold text-slate-900 text-sm">Your live visit</span>
              <span className="inline-flex items-center gap-1.5 text-[11px] font-bold text-amber-700 bg-amber-50 px-2.5 py-1 rounded-full border border-amber-200">
                <span className="w-1.5 h-1.5 rounded-full bg-amber-500 animate-pulse" />
                IN QUEUE
              </span>
            </div>

            {/* Dark teal inner card */}
            <div className="bg-[#0b3b36] rounded-2xl p-5 text-white mb-5 relative overflow-hidden">
              <div className="absolute inset-0 bg-grid-pattern-light opacity-10 pointer-events-none" />
              <div className="flex items-center gap-5 relative">
                <div className="w-16 h-16 rounded-full bg-[#00685b] border-2 border-teal-400/30 flex items-center justify-center text-white text-3xl font-extrabold shrink-0 animate-pulse-glow">
                  {simulatedQueue}
                </div>
                <div>
                  <h3 className="text-xl font-extrabold text-white tracking-tight leading-none mb-1">in line</h3>
                  <p className="text-xs text-teal-200 font-medium">About {simulatedWait} minutes</p>
                  <p className="text-[11px] text-teal-300/80 mt-1">Ticket #1042 · Room 3</p>
                </div>
              </div>
            </div>

            {/* Clinic info */}
            <div className="text-xs font-semibold text-slate-600 mb-4 px-1">
              City General Hospital · Cardiology
            </div>

            {/* Progress bar */}
            <div className="grid grid-cols-4 gap-2 mb-4">
              {[0,1,2,3].map((i) => (
                <div
                  key={i}
                  className={`h-2 rounded-full transition-all duration-700 ${i < 3 ? 'bg-[#00685b]' : 'bg-slate-200'}`}
                />
              ))}
            </div>

            {/* "Doctor ready" floating notification */}
            <div className="bg-[#e6f4f1] border border-[#b2e2d8] rounded-xl px-4 py-3 flex items-center gap-3">
              <div className="w-7 h-7 rounded-full bg-[#00685b] flex items-center justify-center text-white shrink-0">
                <Bell size={13} />
              </div>
              <div>
                <p className="text-xs font-bold text-[#0b3b36]">You're next!</p>
                <p className="text-[11px] text-slate-500">Head to the reception desk now.</p>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* ── Workflow steps ──────────────────────────────────────── */}
      <section id="workflow" className="container mx-auto px-6 max-w-6xl py-24 text-center">
        <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-slate-100 text-slate-500 text-[11px] font-bold tracking-wider uppercase mb-5">
          How it works
        </div>
        <h2 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight mb-16">
          From search to seen in three steps
        </h2>

        <div className="grid md:grid-cols-3 gap-8 text-left relative">
          {[
            { num: 1, icon: MapPin,  title: 'Locate a clinic',      desc: 'Use GPS to see open branches sorted by live distance from you.' },
            { num: 2, icon: Ticket,  title: 'Reserve your ticket',  desc: 'Choose a specialist and get a confirmed digital queue number.' },
            { num: 3, icon: Bell,    title: 'Arrive on time',       desc: 'Track your live position and get a notification when you\'re next.' },
          ].map(({ num, icon: Icon, title, desc }) => (
            <div key={num} className="bg-white border border-slate-200/90 rounded-2xl p-8 shadow-sm hover:shadow-md hover:border-[#b2e2d8] transition-all duration-200 group relative">
              {/* Step number + connector */}
              <div className="flex items-center gap-3 mb-5">
                <div className="w-8 h-8 rounded-full bg-[#00685b] text-white text-sm font-extrabold flex items-center justify-center shrink-0">
                  {num}
                </div>
                {num < 3 && (
                  <div className="hidden md:block absolute top-9 left-full w-8 h-px bg-gradient-to-r from-[#b2e2d8] to-transparent z-10" />
                )}
              </div>
              <div className="w-10 h-10 rounded-xl bg-[#e6f4f1] flex items-center justify-center mb-4 group-hover:bg-[#d1f2eb] transition-colors">
                <Icon size={20} className="text-[#00685b]" />
              </div>
              <h3 className="text-base font-bold text-slate-900 mb-2">{title}</h3>
              <p className="text-xs text-slate-500 leading-relaxed">{desc}</p>
            </div>
          ))}
        </div>
      </section>

      {/* ── Features ────────────────────────────────────────────── */}
      <section id="features" className="container mx-auto px-6 max-w-6xl pb-24">
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          {FEATURE_PILLS.map(({ icon: Icon, label }) => (
            <div key={label} className="bg-white border border-slate-200/90 rounded-2xl p-5 flex items-center gap-3 hover:border-[#b2e2d8] hover:shadow-sm transition-all duration-200">
              <div className="w-9 h-9 rounded-xl bg-[#e6f4f1] flex items-center justify-center shrink-0">
                <Icon size={18} className="text-[#00685b]" />
              </div>
              <span className="font-bold text-slate-800 text-xs leading-snug">{label}</span>
            </div>
          ))}
        </div>
      </section>

      {/* ── Pricing ─────────────────────────────────────────────── */}
      <section id="pricing" className="bg-white border-y border-slate-100 py-24">
        <div className="container mx-auto px-6 max-w-4xl">
          <div className="text-center mb-14">
            <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-slate-100 text-slate-500 text-[11px] font-bold tracking-wider uppercase mb-5">
              Pricing
            </div>
            <h2 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight">
              Simple, transparent pricing
            </h2>
            <p className="text-slate-500 text-sm mt-3 max-w-md mx-auto">
              Patients always access HealthQueue free of charge. Clinics pay a flat monthly fee with no per-booking commissions.
            </p>
          </div>

          <div className="grid md:grid-cols-2 gap-6 max-w-2xl mx-auto">
            {PRICING.map(({ title, price, sub, perks, cta, href, highlight }) => (
              <div
                key={title}
                className={[
                  'rounded-3xl p-8 flex flex-col gap-6 relative overflow-hidden',
                  highlight
                    ? 'bg-[#0b3b36] text-white shadow-2xl'
                    : 'bg-white border border-slate-200 shadow-sm',
                ].join(' ')}
              >
                {highlight && (
                  <div className="absolute top-4 right-4 text-[10px] font-extrabold bg-[#00685b] text-white px-2.5 py-1 rounded-full tracking-wider uppercase">
                    Most popular
                  </div>
                )}
                <div>
                  <span className={`text-xs font-bold uppercase tracking-widest ${highlight ? 'text-teal-300' : 'text-[#00685b]'}`}>
                    {title}
                  </span>
                  <div className="flex items-end gap-1.5 mt-2">
                    <span className={`text-4xl font-extrabold tracking-tight ${highlight ? 'text-white' : 'text-slate-900'}`}>
                      {price}
                    </span>
                    <span className={`text-sm pb-1 ${highlight ? 'text-teal-300' : 'text-slate-400'}`}>
                      / {sub}
                    </span>
                  </div>
                </div>

                <ul className="flex flex-col gap-3">
                  {perks.map((p) => (
                    <li key={p} className="flex items-center gap-2.5 text-xs">
                      <div className={`w-4 h-4 rounded-full flex items-center justify-center shrink-0 ${highlight ? 'bg-[#00685b]' : 'bg-[#e6f4f1]'}`}>
                        <Check size={10} className={highlight ? 'text-white' : 'text-[#00685b]'} />
                      </div>
                      <span className={highlight ? 'text-teal-100' : 'text-slate-600'}>{p}</span>
                    </li>
                  ))}
                </ul>

                <Link to={href} className="mt-auto">
                  <Button
                    size="md"
                    className={[
                      'w-full rounded-full',
                      highlight
                        ? 'bg-[#00685b] hover:bg-[#005247] text-white'
                        : 'bg-[#00685b] border border-slate-200 text-slate-800',
                    ].join(' ')}
                  >
                    {cta}
                  </Button>
                </Link>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* ── Footer ──────────────────────────────────────────────── */}
      <footer className="border-t border-slate-200 bg-white py-8 mt-auto text-xs text-slate-500">
        <div className="container mx-auto px-6 max-w-6xl flex flex-col md:flex-row items-center justify-between gap-4">
          <div className="flex items-center gap-2">
            <div className="w-6 h-6 rounded-md bg-[#00685b] flex items-center justify-center text-white">
              <Plus size={14} strokeWidth={3} />
            </div>
            <span className="font-bold text-slate-900 text-sm">HealthQueue</span>
            <span className="text-slate-400 ml-2">© {new Date().getFullYear()} HealthQueue Inc.</span>
          </div>
          <div className="flex items-center gap-6">
            <Link to="/auth/patient/login" className="hover:text-slate-900 transition-colors">Patient portal</Link>
            <Link to="/auth/org/login"     className="hover:text-slate-900 transition-colors">Clinic portal</Link>
            <span className="text-[#00685b] font-semibold">HIPAA Compliant</span>
          </div>
        </div>
      </footer>
    </div>
  );
}
