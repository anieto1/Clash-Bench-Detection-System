import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import * as api from '@/api/client';

// Query keys
export const queryKeys = {
  trackedClans: ['trackedClans'] as const,
  clan: (tag: string) => ['clan', tag] as const,
  clanMembers: (tag: string) => ['clanMembers', tag] as const,
  seasonHistory: (clanTag: string) => ['seasonHistory', clanTag] as const,
  season: (clanTag: string, season: string) => ['season', clanTag, season] as const,
  war: (warTag: string) => ['war', warTag] as const,
  leaderboard: (clanTag: string, season: string) => ['leaderboard', clanTag, season] as const,
  player: (tag: string) => ['player', tag] as const,
};

// L1 staleTime constants — matched to data volatility
// Within staleTime, TanStack Query serves from memory without refetching
const STALE_TIMES = {
  trackedClans: 2 * 60 * 1000,   // 2 min — dashboard list, must feel fresh
  clan: 5 * 60 * 1000,           // 5 min — profile data changes infrequently
  clanMembers: 5 * 60 * 1000,    // 5 min — member list
  seasonHistory: 2 * 60 * 1000,  // 2 min — changes on CWL sync
  season: 10 * 60 * 1000,        // 10 min — rarely changes mid-season
  war: 10 * 60 * 1000,           // 10 min — immutable once ended
  leaderboard: 10 * 60 * 1000,   // 10 min — only changes on sync
  player: 5 * 60 * 1000,         // 5 min — trophies/donations change moderately
} as const;

// Clan hooks
export function useTrackedClans() {
  return useQuery({
    queryKey: queryKeys.trackedClans,
    queryFn: api.getTrackedClans,
    staleTime: STALE_TIMES.trackedClans,
  });
}

export function useClan(tag: string) {
  return useQuery({
    queryKey: queryKeys.clan(tag),
    queryFn: () => api.getClan(tag),
    enabled: !!tag,
    staleTime: STALE_TIMES.clan,
  });
}

export function useClanMembers(tag: string) {
  return useQuery({
    queryKey: queryKeys.clanMembers(tag),
    queryFn: () => api.getClanMembers(tag),
    enabled: !!tag,
    staleTime: STALE_TIMES.clanMembers,
  });
}

export function useTrackClan() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: api.trackClan,
    onSuccess: (_data, tag) => {
      queryClient.invalidateQueries({ queryKey: queryKeys.trackedClans });
      queryClient.invalidateQueries({ queryKey: queryKeys.clan(tag) });
      queryClient.invalidateQueries({ queryKey: queryKeys.clanMembers(tag) });
    },
  });
}

export function useUntrackClan() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: api.untrackClan,
    onSuccess: (_data, tag) => {
      queryClient.invalidateQueries({ queryKey: queryKeys.trackedClans });
      queryClient.invalidateQueries({ queryKey: queryKeys.clan(tag) });
    },
  });
}

// CWL hooks
export function useSeasonHistory(clanTag: string) {
  return useQuery({
    queryKey: queryKeys.seasonHistory(clanTag),
    queryFn: () => api.getSeasonHistory(clanTag),
    enabled: !!clanTag,
    staleTime: STALE_TIMES.seasonHistory,
  });
}

export function useSeason(clanTag: string, season: string) {
  return useQuery({
    queryKey: queryKeys.season(clanTag, season),
    queryFn: () => api.getSeason(clanTag, season),
    enabled: !!clanTag && !!season,
    staleTime: STALE_TIMES.season,
  });
}

export function useWar(warTag: string) {
  return useQuery({
    queryKey: queryKeys.war(warTag),
    queryFn: () => api.getWar(warTag),
    enabled: !!warTag,
    staleTime: STALE_TIMES.war,
  });
}

export function useLeaderboard(clanTag: string, season: string) {
  return useQuery({
    queryKey: queryKeys.leaderboard(clanTag, season),
    queryFn: () => api.getLeaderboard(clanTag, season),
    enabled: !!clanTag && !!season,
    staleTime: STALE_TIMES.leaderboard,
  });
}

export function useSyncCwl(clanTag: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: () => api.syncCwl(clanTag),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.seasonHistory(clanTag) });
      queryClient.invalidateQueries({ queryKey: ['leaderboard', clanTag] });
      queryClient.invalidateQueries({ queryKey: ['season', clanTag] });
      queryClient.invalidateQueries({ queryKey: ['war'] });
      queryClient.invalidateQueries({ queryKey: ['player'] });
    },
  });
}

// Player hooks
export function usePlayer(tag: string) {
  return useQuery({
    queryKey: queryKeys.player(tag),
    queryFn: () => api.getPlayer(tag),
    enabled: !!tag,
    staleTime: STALE_TIMES.player,
  });
}
