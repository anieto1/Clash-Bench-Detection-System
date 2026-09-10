import { useParams, Link } from 'react-router-dom';
import { ChevronDown, ChevronRight, Trophy, Calendar } from 'lucide-react';
import { useState } from 'react';
import { RankBadge, THBadge, ScoreBadge, ResultBadge, StarDisplay } from '@/components/coc';
import { useSeason, useLeaderboard } from '@/hooks/queries';
import { formatScore } from '@/lib/scoring';
import type { LeaderBoardEntry, AttackScoreDto } from '@/types/api';

function AttackScoreBreakdown({ scores }: { scores: AttackScoreDto[] }) {
  if (scores.length === 0) {
    return <p className="text-sm text-muted-foreground py-2">No attacks recorded</p>;
  }

  return (
    <div className="space-y-2">
      {scores.map((score, i) => {
        const isAbsent = score.attackerMapPosition === 0 && score.defenderMapPosition === 0 && score.totalScore === -100;
        if (isAbsent) {
          return (
            <div key={i} className="flex items-center gap-3 px-3 py-2.5 rounded-lg bg-coc-loss/8 border border-coc-loss/20">
              <span className="text-xs font-bold text-muted-foreground w-5">D{i + 1}</span>
              <span className="text-sm font-semibold text-coc-loss">Missed Attack</span>
              <span className="ml-auto"><ScoreBadge score={score.totalScore} size="sm" /></span>
            </div>
          );
        }

        return (
          <div key={i} className="px-3 py-2.5 rounded-lg bg-coc-navy-400/50 border border-coc-navy-50/20">
            {/* Attack summary row */}
            <div className="flex items-center gap-2 flex-wrap">
              <span className="text-xs font-bold text-muted-foreground w-5 shrink-0">D{i + 1}</span>
              <THBadge level={score.attackerTh} size="sm" />
              <span className="text-sm font-bold">#{score.attackerMapPosition}</span>
              <span className="text-coc-gold-400 text-xs">&rarr;</span>
              <span className="text-sm">#{score.defenderMapPosition}</span>
              <THBadge level={score.defenderTh} size="sm" />
              <span className="mx-1 text-muted-foreground">|</span>
              <StarDisplay stars={score.stars} size="sm" />
              <span className="text-xs font-mono text-muted-foreground">{score.destructionPercentage.toFixed(0)}%</span>
              <span className="ml-auto"><ScoreBadge score={score.totalScore} size="sm" /></span>
            </div>
            {/* Score modifiers row */}
            <div className="flex gap-3 mt-1.5 pl-7 text-[10px] text-muted-foreground font-mono">
              <span>base:{formatScore(score.baseScore)}</span>
              {score.destructionModifier !== 0 && <span>dest:{formatScore(score.destructionModifier)}</span>}
              {score.positionModifier !== 0 && <span>pos:{formatScore(score.positionModifier)}</span>}
              {score.thModifier !== 0 && <span>th:{formatScore(score.thModifier)}</span>}
              {score.topBaseBonus !== 0 && <span className="text-coc-gold-300">top:{formatScore(score.topBaseBonus)}</span>}
              {score.gimmePenalty !== 0 && <span className="text-coc-loss">gimme:{formatScore(score.gimmePenalty)}</span>}
            </div>
          </div>
        );
      })}
    </div>
  );
}

function LeaderboardRow({ entry, rank }: { entry: LeaderBoardEntry; rank: number }) {
  const [open, setOpen] = useState(false);

  const rankBgClass = rank === 1
    ? 'bg-[rgba(255,215,0,0.06)]'
    : rank === 2
      ? 'bg-[rgba(192,192,192,0.04)]'
      : rank === 3
        ? 'bg-[rgba(205,127,50,0.04)]'
        : '';

  return (
    <>
      <button
        onClick={() => setOpen(!open)}
        className={`w-full grid grid-cols-[40px_1fr_auto] sm:grid-cols-[40px_40px_1fr_80px_60px_60px_70px_70px] gap-2 px-3 py-3 items-center border-b border-coc-navy-50/20 hover:bg-coc-navy-50/8 transition-colors text-left ${rankBgClass}`}
      >
        {/* Rank */}
        <div className="flex justify-center">
          <RankBadge rank={rank} />
        </div>

        {/* TH Badge - desktop only */}
        <div className="hidden sm:flex justify-center">
          <THBadge level={entry.townHallLevel} />
        </div>

        {/* Player name + mobile TH */}
        <div className="flex items-center gap-2 min-w-0">
          <THBadge level={entry.townHallLevel} size="sm" className="sm:hidden" />
          {open ? <ChevronDown className="h-3.5 w-3.5 text-coc-gold-400 shrink-0" /> : <ChevronRight className="h-3.5 w-3.5 text-muted-foreground shrink-0" />}
          <Link
            to={`/players/${encodeURIComponent(entry.playerTag)}`}
            className="font-semibold text-sm truncate hover:text-coc-gold-300 transition-colors"
            onClick={(e) => e.stopPropagation()}
          >
            {entry.playerName}
          </Link>
        </div>

        {/* Score */}
        <div className="flex justify-end sm:justify-center">
          <ScoreBadge score={entry.totalScore} />
        </div>

        {/* Attacks - desktop only */}
        <span className="text-center text-sm hidden sm:block">{entry.attacksMade}</span>
        <span className={`text-center text-sm font-bold hidden sm:block ${entry.attacksMissed > 0 ? 'text-coc-loss' : 'text-muted-foreground'}`}>
          {entry.attacksMissed > 0 ? entry.attacksMissed : '-'}
        </span>

        {/* Avg Stats - desktop only */}
        <span className="text-right text-sm text-coc-star font-bold hidden sm:block">{entry.averageStars.toFixed(1)}</span>
        <span className="text-right text-sm text-muted-foreground hidden sm:block">{entry.averageDestruction.toFixed(0)}%</span>
      </button>

      {/* Expanded score breakdown */}
      {open && (
        <div className="bg-coc-navy-300/50 border-b border-coc-navy-50/20 p-4">
          <p className="text-xs font-bold text-muted-foreground uppercase tracking-wider mb-3">Attack Score Breakdown</p>
          {/* Mobile stats row */}
          <div className="flex gap-4 mb-3 sm:hidden text-xs text-muted-foreground">
            <span>Attacks: <strong className="text-foreground">{entry.attacksMade}</strong></span>
            {entry.attacksMissed > 0 && <span>Missed: <strong className="text-coc-loss">{entry.attacksMissed}</strong></span>}
            <span>Avg Stars: <strong className="text-coc-star">{entry.averageStars.toFixed(1)}</strong></span>
            <span>{entry.averageDestruction.toFixed(0)}%</span>
          </div>
          <AttackScoreBreakdown scores={entry.attackScores} />
        </div>
      )}
    </>
  );
}

type TabId = 'leaderboard' | 'wars';

export function SeasonDetailPage() {
  const { clanTag, season } = useParams<{ clanTag: string; season: string }>();
  const decodedClanTag = clanTag ? decodeURIComponent(clanTag) : '';
  const decodedSeason = season ? decodeURIComponent(season) : '';
  const [activeTab, setActiveTab] = useState<TabId>('leaderboard');

  const { data: seasonData, isLoading: seasonLoading } = useSeason(decodedClanTag, decodedSeason);
  const { data: leaderboard, isLoading: leaderboardLoading } = useLeaderboard(decodedClanTag, decodedSeason);

  if (seasonLoading) {
    return (
      <div className="space-y-4">
        <div className="loading-shimmer h-10 w-48 rounded" />
        <div className="loading-shimmer h-6 w-32 rounded" />
        <div className="loading-shimmer h-64 rounded-xl" />
      </div>
    );
  }

  if (!seasonData) {
    return <div className="text-center py-20 text-muted-foreground">Season not found</div>;
  }

  return (
    <div className="space-y-6">
      {/* Season Header */}
      <div>
        <h1 className="coc-heading text-2xl md:text-3xl">{decodedSeason}</h1>
        <p className="text-muted-foreground text-sm mt-1">{seasonData.leagueName}</p>
        <div className="flex flex-wrap items-center gap-3 mt-3">
          {seasonData.finalPlacement && (
            <span className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-coc-gold-400/15 text-coc-gold-300 font-extrabold text-sm border border-coc-gold-400/30">
              <Trophy className="h-4 w-4" />
              #{seasonData.finalPlacement}
            </span>
          )}
          <span className="text-sm text-coc-star font-bold">{seasonData.totalStars} stars</span>
          <span className="text-sm text-muted-foreground">{seasonData.totalDestruction.toFixed(1)}%</span>
          {(() => {
            const now = new Date();
            const [y, m] = decodedSeason.split('-').map(Number);
            const isOver = seasonData.completed || now.getFullYear() > y || (now.getFullYear() === y && now.getMonth() + 1 > m);
            return (
              <span className={`text-xs font-semibold px-2.5 py-1 rounded-full border ${
                isOver
                  ? 'bg-coc-win/15 text-green-400 border-coc-win/30'
                  : 'bg-coc-draw/15 text-yellow-400 border-coc-draw/30'
              }`}>
                {isOver ? 'Completed' : 'In Progress'}
              </span>
            );
          })()}
        </div>
      </div>

      <div className="gold-divider" />

      {/* Tabs */}
      <div className="flex gap-1 p-1 bg-coc-navy-300/80 rounded-xl w-fit">
        <button
          onClick={() => setActiveTab('leaderboard')}
          className={`flex items-center gap-2 px-4 py-2 rounded-lg text-sm font-semibold transition-all ${
            activeTab === 'leaderboard'
              ? 'bg-coc-navy-100 text-coc-gold-300 shadow-[0_2px_4px_rgba(0,0,0,0.3)]'
              : 'text-muted-foreground hover:text-foreground'
          }`}
        >
          <Trophy className="h-4 w-4" />
          Leaderboard
        </button>
        <button
          onClick={() => setActiveTab('wars')}
          className={`flex items-center gap-2 px-4 py-2 rounded-lg text-sm font-semibold transition-all ${
            activeTab === 'wars'
              ? 'bg-coc-navy-100 text-coc-gold-300 shadow-[0_2px_4px_rgba(0,0,0,0.3)]'
              : 'text-muted-foreground hover:text-foreground'
          }`}
        >
          <Calendar className="h-4 w-4" />
          Wars
        </button>
      </div>

      {/* Leaderboard */}
      {activeTab === 'leaderboard' && (
        <>
          {leaderboardLoading ? (
            <div className="space-y-2">
              {Array.from({ length: 8 }, (_, i) => (
                <div key={i} className="loading-shimmer h-14 rounded-lg" />
              ))}
            </div>
          ) : leaderboard && leaderboard.entries.length > 0 ? (
            <div className="bg-coc-navy-200/60 border border-coc-navy-50/40 rounded-xl overflow-hidden">
              {/* Header - desktop only */}
              <div className="hidden sm:grid grid-cols-[40px_40px_1fr_80px_60px_60px_70px_70px] gap-2 px-3 py-3 text-xs font-bold text-muted-foreground uppercase tracking-wider border-b border-coc-navy-50/30 bg-coc-navy-300/40">
                <span className="text-center">#</span>
                <span className="text-center">TH</span>
                <span>Player</span>
                <span className="text-center">Score</span>
                <span className="text-center">Atk</span>
                <span className="text-center">Miss</span>
                <span className="text-right">Stars</span>
                <span className="text-right">Dest</span>
              </div>
              {leaderboard.entries.map((entry, i) => (
                <LeaderboardRow key={entry.playerTag} entry={entry} rank={i + 1} />
              ))}
            </div>
          ) : (
            <div className="text-center py-12 text-muted-foreground">No leaderboard data available</div>
          )}
        </>
      )}

      {/* Wars */}
      {activeTab === 'wars' && (
        <>
          {seasonData.wars.length > 0 ? (
            <div className="grid gap-4 grid-cols-1 sm:grid-cols-2 lg:grid-cols-3">
              {[...seasonData.wars].sort((a, b) => a.dayNumber - b.dayNumber).map((war) => (
                <Link key={war.warTag} to={`/cwl/wars/${encodeURIComponent(war.warTag)}`}>
                  <div className="group bg-gradient-to-b from-coc-navy-100 to-coc-navy-200 border border-coc-navy-50/40 rounded-xl overflow-hidden hover:border-coc-gold-400/50 transition-all shadow-coc">
                    {/* Day header with result color */}
                    <div className={`px-4 py-3 border-b border-coc-navy-50/30 ${
                      war.result === 'WIN' ? 'bg-coc-win/8' : war.result === 'LOSE' ? 'bg-coc-loss/8' : 'bg-coc-navy-300/40'
                    }`}>
                      <div className="flex items-center justify-between">
                        <span className="font-display text-lg text-coc-gold-300">Day {war.dayNumber}</span>
                        <ResultBadge result={war.result} />
                      </div>
                      <p className="text-xs text-muted-foreground mt-0.5">vs {war.opponentClanName}</p>
                    </div>
                    {/* Score */}
                    <div className="p-4 flex items-center justify-center gap-6">
                      <span className="text-3xl font-extrabold text-foreground">{war.ourStars}</span>
                      <span className="text-lg text-muted-foreground font-display">VS</span>
                      <span className="text-3xl font-extrabold text-muted-foreground">{war.opponentStars}</span>
                    </div>
                  </div>
                </Link>
              ))}
            </div>
          ) : (
            <div className="text-center py-12 text-muted-foreground">No war data available</div>
          )}
        </>
      )}
    </div>
  );
}
