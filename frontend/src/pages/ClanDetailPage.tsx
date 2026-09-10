import { useParams, Link } from 'react-router-dom';
import { RefreshCw, Users, Calendar } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { ClanBadge, THBadge } from '@/components/coc';
import { useClan, useClanMembers, useSeasonHistory, useSyncCwl } from '@/hooks/queries';
import { useState } from 'react';

type TabId = 'members' | 'cwl';

export function ClanDetailPage() {
  const { tag } = useParams<{ tag: string }>();
  const decodedTag = tag ? decodeURIComponent(tag) : '';
  const [activeTab, setActiveTab] = useState<TabId>('members');

  const { data: clan, isLoading: clanLoading } = useClan(decodedTag);
  const { data: members, isLoading: membersLoading } = useClanMembers(decodedTag);
  const { data: seasons, isLoading: seasonsLoading } = useSeasonHistory(decodedTag);
  const syncCwl = useSyncCwl(decodedTag);

  if (clanLoading) {
    return (
      <div className="space-y-6">
        <div className="flex items-center gap-4">
          <div className="loading-shimmer h-16 w-16 rounded-full" />
          <div className="space-y-2">
            <div className="loading-shimmer h-8 w-48 rounded" />
            <div className="loading-shimmer h-4 w-32 rounded" />
          </div>
        </div>
      </div>
    );
  }

  if (!clan) {
    return (
      <div className="flex flex-col items-center justify-center py-20 text-center">
        <p className="text-muted-foreground">Clan not found</p>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Clan Header */}
      <div className="flex flex-col sm:flex-row items-start gap-4">
        <ClanBadge badgeUrl={clan.badgeUrl} size="lg" />
        <div className="flex-1 min-w-0">
          <h1 className="coc-heading text-2xl md:text-3xl truncate">{clan.name}</h1>
          <p className="text-muted-foreground text-sm mt-1">
            Level {clan.clanLevel} &middot; {clan.clanPoints} points
          </p>
          <div className="flex gap-4 mt-2 text-sm font-bold">
            <span className="text-coc-win">{clan.warWins} Wins</span>
            <span className="text-coc-draw">{clan.warTies} Ties</span>
            <span className="text-coc-loss">{clan.warLosses} Losses</span>
          </div>
        </div>
        <Button
          onClick={() => syncCwl.mutate()}
          disabled={syncCwl.isPending}
          className="shrink-0 bg-gradient-to-b from-[#60A5FA] via-[#3B82F6] to-[#2563EB] text-white border-2 border-[#1D4ED8] shadow-[0_2px_0_#1D4ED8,0_4px_8px_rgba(0,0,0,0.3)] hover:from-[#93C5FD] hover:to-[#3B82F6] font-bold"
        >
          <RefreshCw className={`h-4 w-4 mr-2 ${syncCwl.isPending ? 'animate-spin' : ''}`} />
          Sync CWL
        </Button>
      </div>

      {clan.description && (
        <p className="text-sm text-muted-foreground border-l-2 border-coc-gold-500/30 pl-3">{clan.description}</p>
      )}

      <div className="gold-divider" />

      {/* Tab Navigation */}
      <div className="flex gap-1 p-1 bg-coc-navy-300/80 rounded-xl w-fit">
        <button
          onClick={() => setActiveTab('members')}
          className={`flex items-center gap-2 px-4 py-2 rounded-lg text-sm font-semibold transition-all ${
            activeTab === 'members'
              ? 'bg-coc-navy-100 text-coc-gold-300 shadow-[0_2px_4px_rgba(0,0,0,0.3)]'
              : 'text-muted-foreground hover:text-foreground'
          }`}
        >
          <Users className="h-4 w-4" />
          Members
        </button>
        <button
          onClick={() => setActiveTab('cwl')}
          className={`flex items-center gap-2 px-4 py-2 rounded-lg text-sm font-semibold transition-all ${
            activeTab === 'cwl'
              ? 'bg-coc-navy-100 text-coc-gold-300 shadow-[0_2px_4px_rgba(0,0,0,0.3)]'
              : 'text-muted-foreground hover:text-foreground'
          }`}
        >
          <Calendar className="h-4 w-4" />
          CWL Seasons
        </button>
      </div>

      {/* Members Tab */}
      {activeTab === 'members' && (
        <>
          {membersLoading ? (
            <div className="space-y-2">
              {[1, 2, 3, 4, 5].map((i) => (
                <div key={i} className="loading-shimmer h-12 rounded-lg" />
              ))}
            </div>
          ) : members && members.length > 0 ? (
            <div className="bg-coc-navy-200/60 border border-coc-navy-50/40 rounded-xl overflow-hidden">
              {/* Table header */}
              <div className="hidden sm:grid grid-cols-[40px_1fr_80px_56px_80px_80px] gap-2 px-4 py-3 text-xs font-bold text-muted-foreground uppercase tracking-wider border-b border-coc-navy-50/30 bg-coc-navy-300/40">
                <span className="text-center">#</span>
                <span>Name</span>
                <span>Role</span>
                <span className="text-center">TH</span>
                <span className="text-right">Trophies</span>
                <span className="text-right">Donations</span>
              </div>
              {/* Table rows */}
              {members.map((member, index) => (
                <Link
                  key={member.tag}
                  to={`/players/${encodeURIComponent(member.tag)}`}
                  className="grid grid-cols-[1fr_auto] sm:grid-cols-[40px_1fr_80px_56px_80px_80px] gap-2 px-4 py-3 items-center border-b border-coc-navy-50/20 last:border-0 hover:bg-coc-navy-50/8 transition-colors"
                >
                  <span className="text-center text-sm font-bold text-muted-foreground hidden sm:block">{index + 1}</span>
                  <div className="flex items-center gap-3 min-w-0">
                    <THBadge level={member.townHallLevel} size="sm" />
                    <span className="font-semibold text-sm truncate">{member.name}</span>
                  </div>
                  <span className="text-xs text-muted-foreground capitalize hidden sm:block">{member.clanRole?.replace('admin', 'elder')}</span>
                  <span className="text-center text-sm font-bold hidden sm:block">{member.townHallLevel}</span>
                  <span className="text-right text-sm hidden sm:block">{member.trophies.toLocaleString()}</span>
                  <span className="text-right text-sm text-coc-win hidden sm:block">{member.donations}</span>
                  {/* Mobile compact info */}
                  <div className="flex items-center gap-3 sm:hidden">
                    <span className="text-xs text-muted-foreground">{member.trophies.toLocaleString()}</span>
                  </div>
                </Link>
              ))}
            </div>
          ) : (
            <p className="text-muted-foreground text-center py-8">No members found</p>
          )}
        </>
      )}

      {/* CWL Seasons Tab */}
      {activeTab === 'cwl' && (
        <>
          {seasonsLoading ? (
            <div className="grid gap-4 grid-cols-1 sm:grid-cols-2 lg:grid-cols-3">
              {[1, 2, 3].map((i) => (
                <div key={i} className="loading-shimmer h-32 rounded-xl" />
              ))}
            </div>
          ) : seasons && seasons.length > 0 ? (
            <div className="grid gap-4 grid-cols-1 sm:grid-cols-2 lg:grid-cols-3">
              {seasons.map((season) => (
                <Link
                  key={season.season}
                  to={`/clans/${encodeURIComponent(decodedTag)}/cwl/${encodeURIComponent(season.season)}`}
                >
                  <div className="group bg-gradient-to-b from-coc-navy-100 to-coc-navy-200 border border-coc-navy-50/40 rounded-xl p-4 hover:border-coc-gold-400/50 transition-all shadow-coc">
                    <div className="flex items-center justify-between mb-2">
                      <h3 className="font-display text-lg text-coc-gold-300">{season.season}</h3>
                      {season.finalPlacement && (
                        <span className="flex items-center justify-center w-8 h-8 rounded-lg bg-coc-gold-400/15 text-coc-gold-300 font-extrabold text-sm border border-coc-gold-400/30">
                          #{season.finalPlacement}
                        </span>
                      )}
                    </div>
                    <p className="text-xs text-muted-foreground mb-3">{season.leagueName}</p>
                    <div className="flex items-center gap-3 text-sm">
                      <span className="text-coc-star font-bold">{season.totalStars} stars</span>
                      {(() => {
                        const now = new Date();
                        const [y, m] = season.season.split('-').map(Number);
                        const isOver = season.completed || now.getFullYear() > y || (now.getFullYear() === y && now.getMonth() + 1 > m);
                        return (
                          <span className={`text-xs font-semibold px-2 py-0.5 rounded-full ${
                            isOver
                              ? 'bg-coc-win/15 text-green-400 border border-coc-win/30'
                              : 'bg-coc-draw/15 text-yellow-400 border border-coc-draw/30'
                          }`}>
                            {isOver ? 'Done' : 'Active'}
                          </span>
                        );
                      })()}
                    </div>
                  </div>
                </Link>
              ))}
            </div>
          ) : (
            <div className="text-center py-12">
              <p className="text-muted-foreground mb-3">No CWL data yet.</p>
              <p className="text-sm text-muted-foreground">
                Click <strong className="text-foreground">"Sync CWL"</strong> above to fetch the current league group.
              </p>
            </div>
          )}
        </>
      )}
    </div>
  );
}
