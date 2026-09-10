import { useParams } from 'react-router-dom';
import { AlertTriangle } from 'lucide-react';
import { StarDisplay, THBadge, ResultBadge } from '@/components/coc';
import { useWar } from '@/hooks/queries';

export function WarDetailPage() {
  const { warTag } = useParams<{ warTag: string }>();
  const decodedWarTag = warTag ? decodeURIComponent(warTag) : '';

  const { data: war, isLoading } = useWar(decodedWarTag);

  if (isLoading) {
    return (
      <div className="space-y-4 max-w-4xl mx-auto">
        <div className="loading-shimmer h-10 w-48 rounded mx-auto" />
        <div className="loading-shimmer h-32 rounded-xl" />
        <div className="loading-shimmer h-64 rounded-xl" />
      </div>
    );
  }

  if (!war) {
    return <div className="text-center py-20 text-muted-foreground">War not found</div>;
  }

  const missingAttacks = war.members.filter((m) => !m.attacked);

  return (
    <div className="space-y-6 max-w-4xl mx-auto">
      {/* War Matchup Header */}
      <div className="bg-gradient-to-b from-coc-navy-100 to-coc-navy-200 border-2 border-coc-navy-50/60 rounded-2xl overflow-hidden shadow-coc-lg">
        {/* Top bar */}
        <div className={`px-4 py-2 text-center border-b border-coc-navy-50/30 ${
          war.result === 'WIN' ? 'bg-gradient-to-r from-transparent via-coc-win/15 to-transparent' :
          war.result === 'LOSE' ? 'bg-gradient-to-r from-transparent via-coc-loss/15 to-transparent' :
          'bg-coc-navy-300/40'
        }`}>
          <span className="font-display text-sm text-coc-gold-300 tracking-wider">DAY {war.dayNumber}</span>
        </div>

        {/* VS Display */}
        <div className="p-6 flex items-center justify-center gap-6 sm:gap-10">
          {/* Our side */}
          <div className="text-center flex-1">
            <p className="text-4xl sm:text-5xl font-extrabold text-foreground">{war.ourStars}</p>
            <p className="text-sm text-muted-foreground mt-1">{war.ourDestruction.toFixed(1)}%</p>
          </div>

          {/* VS divider */}
          <div className="flex flex-col items-center gap-2">
            <span className="font-display text-2xl text-coc-gold-400/60">VS</span>
            <ResultBadge result={war.result} />
          </div>

          {/* Opponent side */}
          <div className="text-center flex-1">
            <p className="text-4xl sm:text-5xl font-extrabold text-muted-foreground">{war.opponentStars}</p>
            <p className="text-sm text-muted-foreground mt-1">{war.opponentDestruction.toFixed(1)}%</p>
          </div>
        </div>

        <div className="text-center pb-4">
          <p className="text-sm text-muted-foreground">vs <strong className="text-foreground">{war.opponentClanName}</strong></p>
        </div>
      </div>

      {/* Attacks Table */}
      <div className="bg-coc-navy-200/60 border border-coc-navy-50/40 rounded-xl overflow-hidden">
        <div className="px-4 py-3 border-b border-coc-navy-50/30 bg-coc-navy-300/40">
          <h2 className="font-display text-lg text-coc-gold-300">Attacks</h2>
        </div>

        {war.attacks.length > 0 ? (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="text-xs text-muted-foreground uppercase tracking-wider border-b border-coc-navy-50/20">
                  <th className="py-3 px-3 text-center w-12">#</th>
                  <th className="py-3 px-2 text-center">Atk Pos</th>
                  <th className="py-3 px-1 text-center w-6"></th>
                  <th className="py-3 px-2 text-center">Def Pos</th>
                  <th className="py-3 px-2 text-center">Def TH</th>
                  <th className="py-3 px-2 text-center">Stars</th>
                  <th className="py-3 px-3 text-right">Dest</th>
                </tr>
              </thead>
              <tbody>
                {war.attacks.map((attack, i) => (
                  <tr
                    key={i}
                    className="border-b border-coc-navy-50/15 last:border-0 hover:bg-coc-navy-50/8 transition-colors"
                  >
                    <td className="py-2.5 px-3 text-center text-muted-foreground font-medium">
                      {attack.attackOrder ?? '-'}
                    </td>
                    <td className="py-2.5 px-2 text-center font-bold">#{attack.attackerMapPosition}</td>
                    <td className="py-2.5 px-1 text-center text-coc-gold-400">→</td>
                    <td className="py-2.5 px-2 text-center">#{attack.defenderMapPosition}</td>
                    <td className="py-2.5 px-2">
                      <div className="flex justify-center">
                        <THBadge level={attack.defenderThLevel} size="sm" />
                      </div>
                    </td>
                    <td className="py-2.5 px-2">
                      <div className="flex justify-center">
                        <StarDisplay stars={attack.stars} size="sm" />
                      </div>
                    </td>
                    <td className="py-2.5 px-3 text-right font-mono">
                      {attack.destructionPercentage.toFixed(0)}%
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <p className="text-muted-foreground text-center py-8">No attacks recorded</p>
        )}
      </div>

      {/* Missing Attacks */}
      {missingAttacks.length > 0 && (
        <div className="bg-coc-loss/8 border-2 border-coc-loss/30 rounded-xl overflow-hidden">
          <div className="px-4 py-3 border-b border-coc-loss/20 flex items-center gap-2">
            <AlertTriangle className="h-4 w-4 text-coc-loss" />
            <h2 className="font-display text-lg text-coc-loss">
              Missing Attacks ({missingAttacks.length})
            </h2>
          </div>
          <div className="p-4 flex flex-wrap gap-2">
            {missingAttacks.map((member) => (
              <span
                key={member.playerTag}
                className="inline-flex items-center px-3 py-1.5 rounded-lg bg-coc-loss/15 text-red-300 border border-coc-loss/30 text-sm font-bold"
              >
                #{member.mapPosition}
              </span>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
