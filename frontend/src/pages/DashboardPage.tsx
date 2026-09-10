import { Link } from 'react-router-dom';
import { Plus, Swords, Shield, Trophy, Users } from 'lucide-react';
import { useState, useEffect, useRef } from 'react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { ClanBadge } from '@/components/coc';
import { useTrackedClans, useTrackClan } from '@/hooks/queries';

/* ── Animated number counter ── */
function AnimatedNumber({ value }: { value: number }) {
  const [display, setDisplay] = useState(0);
  const ref = useRef<ReturnType<typeof setInterval> | null>(null);

  useEffect(() => {
    if (ref.current !== null) clearInterval(ref.current);
    const steps = 40;
    const increment = value / steps;
    let current = 0;
    ref.current = setInterval(() => {
      current = Math.min(current + increment, value);
      setDisplay(Math.round(current));
      if (current >= value && ref.current !== null) clearInterval(ref.current);
    }, 1000 / steps);
    return () => { if (ref.current !== null) clearInterval(ref.current); };
  }, [value]);

  return <span>{display}</span>;
}

/* ── Stat Pill ── */
function StatPill({ icon: Icon, label, value }: { icon: React.ElementType; label: string; value: React.ReactNode }) {
  return (
    <div className="flex flex-col items-center gap-1.5 bg-coc-navy-200/60 border border-coc-navy-50/40 rounded-xl py-4 px-3">
      <Icon className="h-4 w-4 text-coc-gold-400 mb-0.5" />
      <span className="text-xl font-extrabold text-coc-gold-300">{value}</span>
      <span className="text-[10px] font-bold text-muted-foreground uppercase tracking-widest text-center">{label}</span>
    </div>
  );
}

/* ── How It Works Card ── */
function HowItWorksCard({ icon: Icon, step, title, description }: {
  icon: React.ElementType; step: number; title: string; description: string;
}) {
  return (
    <div className="relative flex flex-col items-center text-center p-6 bg-coc-navy-200/60 border border-coc-navy-50/40 rounded-2xl overflow-hidden group hover:border-coc-gold-400/30 transition-colors">
      <div className="absolute top-0 left-0 right-0 h-[1px] bg-gradient-to-r from-transparent via-coc-gold-500/40 to-transparent" />
      <div className="flex items-center justify-center w-14 h-14 rounded-xl bg-coc-navy-300/80 border border-coc-gold-500/30 mb-4 group-hover:border-coc-gold-400/60 transition-colors">
        <Icon className="h-7 w-7 text-coc-gold-400" />
      </div>
      <span className="text-xs font-bold text-coc-gold-500 uppercase tracking-widest mb-1">Step {step}</span>
      <h3 className="font-display text-lg text-foreground mb-2">{title}</h3>
      <p className="text-sm text-muted-foreground leading-relaxed">{description}</p>
    </div>
  );
}

/* ── Track Clan Input ── */
function TrackClanInput() {
  const [tag, setTag] = useState('');
  const trackClan = useTrackClan();

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!tag.trim()) return;
    const normalizedTag = tag.startsWith('#') ? tag : `#${tag}`;
    trackClan.mutate(normalizedTag, { onSuccess: () => setTag('') });
  };

  return (
    <form onSubmit={handleSubmit} className="flex flex-col sm:flex-row gap-3 w-full max-w-lg mx-auto">
      <Input
        placeholder="#CLAN TAG"
        value={tag}
        onChange={(e) => setTag(e.target.value.toUpperCase())}
        className="flex-1 bg-coc-navy-300/80 border-2 border-coc-gold-400/40 text-foreground placeholder:text-muted-foreground focus:border-coc-gold-300 font-mono text-lg h-12 px-4 focus:shadow-[0_0_16px_rgba(245,197,24,0.2)] transition-all"
      />
      <Button
        type="submit"
        disabled={trackClan.isPending}
        className="h-12 px-6 bg-gradient-to-b from-[#FFD700] via-[#F5C518] to-[#DAA520] text-[#0A1628] font-display uppercase tracking-wide text-base border-2 border-[#B8860B] shadow-[0_3px_0_#8B6914,0_5px_10px_rgba(0,0,0,0.35)] hover:from-[#FFE44D] hover:via-[#FFD700] hover:to-[#F5C518] hover:shadow-[0_3px_0_#8B6914,0_8px_16px_rgba(0,0,0,0.4),0_0_24px_rgba(245,197,24,0.3)] active:shadow-[inset_0_2px_4px_rgba(0,0,0,0.3)] transition-all"
      >
        <Plus className="h-5 w-5 mr-1.5" />
        {trackClan.isPending ? 'Tracking…' : 'Track Clan'}
      </Button>
    </form>
  );
}

/* ── Clan Card ── */
function ClanCard({ clan }: { clan: { tag: string; name: string; clanLevel: number; badgeUrl: string | null; warWins: number; warTies: number; warLosses: number } }) {
  return (
    <Link to={`/clans/${encodeURIComponent(clan.tag)}`}>
      <div className="group relative bg-gradient-to-b from-coc-navy-100 to-coc-navy-200 border-2 border-coc-navy-50/50 rounded-xl overflow-hidden shadow-coc transition-all duration-200 hover:border-coc-gold-400/70 hover:shadow-[0_6px_20px_rgba(0,0,0,0.5),0_0_20px_rgba(218,165,32,0.2)]">
        <div className="h-[2px] bg-gradient-to-r from-transparent via-coc-gold-400 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-300" />
        <div className="p-4 flex items-center gap-4">
          <ClanBadge badgeUrl={clan.badgeUrl} size="md" />
          <div className="flex-1 min-w-0">
            <h3 className="font-bold text-foreground truncate group-hover:text-coc-gold-300 transition-colors">
              {clan.name}
            </h3>
            <p className="text-xs text-muted-foreground font-mono mt-0.5">{clan.tag} · Lv {clan.clanLevel}</p>
          </div>
        </div>
        <div className="px-4 pb-3 flex items-center justify-between">
          <div className="flex gap-3 text-sm font-bold">
            <span className="text-coc-win">{clan.warWins}W</span>
            <span className="text-coc-draw">{clan.warTies}T</span>
            <span className="text-coc-loss">{clan.warLosses}L</span>
          </div>
          <span className="text-xs text-coc-gold-400 font-display opacity-0 group-hover:opacity-100 transition-opacity">
            View →
          </span>
        </div>
      </div>
    </Link>
  );
}

/* ── Empty State ── */
function EmptyState() {
  return (
    <div className="flex flex-col items-center justify-center py-20 text-center">
      <div className="relative mb-6">
        <div className="flex items-center justify-center w-24 h-24 rounded-2xl bg-coc-navy-100 border-2 border-coc-navy-50/40">
          <Swords className="h-12 w-12 text-coc-gold-400" />
        </div>
        <div className="absolute -top-1 -right-1 w-5 h-5 rounded-full bg-coc-gold-400 flex items-center justify-center">
          <Plus className="h-3 w-3 text-[#0A1628]" />
        </div>
      </div>
      <h2 className="font-display text-2xl text-coc-gold-300 mb-2">No Clans Yet</h2>
      <p className="text-muted-foreground max-w-sm text-sm leading-relaxed">
        Enter a clan tag above to start tracking CWL performance.
        Find out who earns their spot — and who belongs on the bench.
      </p>
    </div>
  );
}

/* ══════════════════════════════════════════
   MAIN DASHBOARD PAGE
══════════════════════════════════════════ */
export function DashboardPage() {
  const { data: clans, isLoading } = useTrackedClans();

  const totalClans = clans?.length ?? 0;
  const totalWins = clans?.reduce((sum, c) => sum + (c.warWins ?? 0), 0) ?? 0;
  const totalWars = clans?.reduce((sum, c) => sum + (c.warWins ?? 0) + (c.warTies ?? 0) + (c.warLosses ?? 0), 0) ?? 0;

  return (
    <div className="space-y-0">

      {/* ═══ HERO SECTION ═══ */}
      <section className="relative -mx-4 md:-mx-6 px-4 md:px-6 py-16 md:py-24 overflow-hidden">
        {/* Faint gold grid texture */}
        <div
          className="absolute inset-0 opacity-[0.05] pointer-events-none"
          style={{
            backgroundImage:
              'linear-gradient(rgba(245,197,24,0.8) 1px, transparent 1px), linear-gradient(90deg, rgba(245,197,24,0.8) 1px, transparent 1px)',
            backgroundSize: '48px 48px',
          }}
        />
        {/* Gold radial glow at top-center */}
        <div
          className="absolute top-0 left-1/2 -translate-x-1/2 w-[700px] h-[280px] pointer-events-none opacity-15"
          style={{ background: 'radial-gradient(ellipse, rgba(245,197,24,0.5) 0%, transparent 70%)' }}
        />
        {/* Decorative crossed swords */}
        <div className="absolute left-6 top-1/2 -translate-y-1/2 opacity-[0.07] select-none pointer-events-none hidden lg:block">
          <Swords className="h-48 w-48 text-coc-gold-400 -rotate-45" />
        </div>
        <div className="absolute right-6 top-1/2 -translate-y-1/2 opacity-[0.07] select-none pointer-events-none hidden lg:block">
          <Swords className="h-48 w-48 text-coc-gold-400 rotate-45" />
        </div>

        <div className="relative flex flex-col items-center text-center gap-6">
          {/* Eyebrow label */}
          <div className="flex items-center gap-3">
            <div className="hidden sm:flex items-center justify-center w-9 h-9 rounded-lg bg-coc-gold-400/10 border border-coc-gold-500/30">
              <Shield className="h-4 w-4 text-coc-gold-400" />
            </div>
            <span className="text-xs font-bold uppercase tracking-[0.3em] text-coc-gold-500">
              Clash Bench Detection System
            </span>
            <div className="hidden sm:flex items-center justify-center w-9 h-9 rounded-lg bg-coc-gold-400/10 border border-coc-gold-500/30">
              <Shield className="h-4 w-4 text-coc-gold-400" />
            </div>
          </div>

          <h1 className="coc-heading text-4xl sm:text-5xl md:text-6xl leading-tight max-w-3xl">
            Track. Score.<br className="hidden sm:block" /> Dominate.
          </h1>

          <p className="text-muted-foreground text-base sm:text-lg max-w-xl leading-relaxed">
            Know exactly who earned their stars — and who belongs on the bench.
            Advanced CWL performance analytics for competitive clans.
          </p>

          <div className="gold-divider w-48 my-1" />

          <TrackClanInput />
        </div>
      </section>

      {/* ═══ STATS BAR ═══ */}
      {totalClans > 0 && (
        <section className="py-6">
          <div className="grid grid-cols-3 gap-3 sm:gap-4">
            <StatPill icon={Users} label="Tracked Clans" value={<AnimatedNumber value={totalClans} />} />
            <StatPill icon={Trophy} label="Total War Wins" value={<AnimatedNumber value={totalWins} />} />
            <StatPill icon={Swords} label="Wars Analyzed" value={<AnimatedNumber value={totalWars} />} />
          </div>
        </section>
      )}

      {/* ═══ HOW IT WORKS ═══ */}
      {totalClans === 0 && !isLoading && (
        <section className="py-8 space-y-6">
          <div className="text-center">
            <h2 className="font-display text-xl text-coc-gold-300 mb-2">Get Started in 3 Steps</h2>
            <div className="gold-divider w-32 mx-auto" />
          </div>
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
            <HowItWorksCard
              icon={Shield}
              step={1}
              title="Track Your Clan"
              description="Enter your clan tag above. We'll pull your current roster and all CWL history from the Clash API."
            />
            <HowItWorksCard
              icon={Swords}
              step={2}
              title="Sync CWL Data"
              description="Hit Sync CWL on your clan page to import each war day — members, attacks, stars, and destruction."
            />
            <HowItWorksCard
              icon={Trophy}
              step={3}
              title="See Your CBDS Score"
              description="Every attack is scored on stars, destruction, map position, TH matchup, and difficulty. No excuses."
            />
          </div>
        </section>
      )}

      {/* ═══ CLAN GRID ═══ */}
      <section className="py-4 space-y-4">
        {totalClans > 0 && (
          <div className="flex items-center gap-4">
            <h2 className="font-display text-xl text-coc-gold-300">Your Clans</h2>
            <div className="flex-1 h-px bg-coc-navy-50/30" />
            <span className="text-xs text-muted-foreground">{totalClans} tracked</span>
          </div>
        )}

        {isLoading ? (
          <div className="grid gap-4 grid-cols-1 sm:grid-cols-2 lg:grid-cols-3">
            {[1, 2, 3].map((i) => (
              <div key={i} className="loading-shimmer h-28 rounded-xl" />
            ))}
          </div>
        ) : clans && clans.length > 0 ? (
          <div className="grid gap-4 grid-cols-1 sm:grid-cols-2 lg:grid-cols-3">
            {clans.map((clan) => (
              <ClanCard key={clan.tag} clan={clan} />
            ))}
          </div>
        ) : (
          <EmptyState />
        )}
      </section>

    </div>
  );
}
