import { useParams } from 'react-router-dom';
import { THBadge } from '@/components/coc';
import { usePlayer } from '@/hooks/queries';

interface UnitGridProps {
  title: string;
  units: { name: string; level: number; maxLevel: number }[];
}

function UnitGrid({ title, units }: UnitGridProps) {
  if (units.length === 0) return null;

  return (
    <div className="bg-coc-navy-200/60 border border-coc-navy-50/40 rounded-xl overflow-hidden">
      <div className="px-4 py-3 border-b border-coc-navy-50/30 bg-coc-navy-300/40">
        <h3 className="font-display text-lg text-coc-gold-300">{title}</h3>
      </div>
      <div className="p-4 grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-3">
        {units.map((unit) => {
          const progress = (unit.level / unit.maxLevel) * 100;
          const isMaxed = unit.level === unit.maxLevel;

          return (
            <div
              key={unit.name}
              className={`p-3 rounded-lg border transition-colors ${
                isMaxed
                  ? 'border-coc-win/40 bg-coc-win/8'
                  : 'border-coc-navy-50/30 bg-coc-navy-300/30'
              }`}
            >
              <p className="font-semibold text-sm truncate">{unit.name}</p>
              <div className="flex items-center gap-2 mt-2">
                <div className="flex-1 h-1.5 bg-coc-navy-500/80 rounded-full overflow-hidden">
                  <div
                    className={`h-full rounded-full transition-all ${
                      isMaxed
                        ? 'bg-gradient-to-r from-coc-win to-green-400'
                        : 'bg-gradient-to-r from-coc-gold-400 to-coc-gold-200'
                    }`}
                    style={{ width: `${progress}%` }}
                  />
                </div>
                <span className={`text-[10px] font-bold shrink-0 ${isMaxed ? 'text-coc-win' : 'text-muted-foreground'}`}>
                  {unit.level}/{unit.maxLevel}
                </span>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}

export function PlayerProfilePage() {
  const { tag } = useParams<{ tag: string }>();
  const decodedTag = tag ? decodeURIComponent(tag) : '';

  const { data: player, isLoading } = usePlayer(decodedTag);

  if (isLoading) {
    return (
      <div className="space-y-6 max-w-4xl mx-auto">
        <div className="loading-shimmer h-10 w-48 rounded" />
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
          {[1, 2, 3, 4].map((i) => (
            <div key={i} className="loading-shimmer h-24 rounded-xl" />
          ))}
        </div>
      </div>
    );
  }

  if (!player) {
    return <div className="text-center py-20 text-muted-foreground">Player not found</div>;
  }

  return (
    <div className="space-y-6 max-w-4xl mx-auto">
      {/* Player Header */}
      <div className="flex items-center gap-4">
        <THBadge level={player.townHallLevel} size="lg" />
        <div>
          <h1 className="coc-heading text-2xl md:text-3xl">{player.name}</h1>
          <div className="flex items-center gap-3 mt-1">
            <span className="text-xs font-bold px-2 py-0.5 rounded-full bg-coc-navy-100 border border-coc-navy-50/40 text-muted-foreground">
              TH {player.townHallLevel}
            </span>
            {player.clanRole && (
              <span className="text-xs font-bold px-2 py-0.5 rounded-full bg-coc-gold-400/10 border border-coc-gold-400/30 text-coc-gold-300 capitalize">
                {player.clanRole.replace('admin', 'elder')}
              </span>
            )}
            <span className="text-xs text-muted-foreground">Lvl {player.expLevel}</span>
          </div>
          <p className="text-xs font-mono text-muted-foreground mt-1">{decodedTag}</p>
        </div>
      </div>

      <div className="gold-divider" />

      {/* Stats Grid */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
        {[
          { label: 'Trophies', value: player.trophies.toLocaleString(), color: 'text-coc-gold-300' },
          { label: 'War Stars', value: player.warStars.toLocaleString(), color: 'text-coc-star' },
          { label: 'Donated', value: player.donations.toLocaleString(), color: 'text-coc-win' },
          { label: 'Received', value: player.donationsReceived.toLocaleString(), color: 'text-blue-400' },
        ].map((stat) => (
          <div
            key={stat.label}
            className="bg-coc-navy-200/60 border border-coc-navy-50/40 rounded-xl p-4 text-center"
          >
            <p className={`text-2xl font-extrabold ${stat.color}`}>{stat.value}</p>
            <p className="text-[10px] font-bold text-muted-foreground uppercase tracking-widest mt-1">{stat.label}</p>
          </div>
        ))}
      </div>

      {/* Unit Grids */}
      <UnitGrid title="Heroes" units={player.heroes} />
      <UnitGrid
        title="Equipment"
        units={player.equipment.map((e) => ({ ...e, name: `${e.name} (${e.heroName})` }))}
      />
      <UnitGrid title="Troops" units={player.troops} />
      <UnitGrid title="Spells" units={player.spells} />
      <UnitGrid title="Pets" units={player.pets} />
    </div>
  );
}
