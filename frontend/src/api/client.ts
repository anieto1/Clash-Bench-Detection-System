import type {
  ClanResponse,
  PlayerResponse,
  CwlSeasonResponse,
  CwlWarResponse,
  LeaderBoardResponse,
} from '@/types/api';

class ApiError extends Error {
  status: number;

  constructor(status: number, message: string) {
    super(message);
    this.status = status;
    this.name = 'ApiError';
  }
}

async function fetchApi<T>(path: string, options?: RequestInit): Promise<T> {
  const response = await fetch(path, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...options?.headers,
    },
  });

  if (!response.ok) {
    throw new ApiError(response.status, `API error: ${response.status}`);
  }

  // Handle 204 No Content
  if (response.status === 204) {
    return null as T;
  }

  return response.json();
}

// Clan endpoints
export async function getTrackedClans(): Promise<ClanResponse[]> {
  return fetchApi<ClanResponse[]>('/api/tracking/clans');
}

export async function trackClan(tag: string): Promise<void> {
  const response = await fetch('/api/tracking/clans', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ clanTag: tag }),
  });

  if (!response.ok) {
    throw new ApiError(response.status, `Failed to track clan: ${response.status}`);
  }
}

export async function untrackClan(tag: string): Promise<void> {
  await fetchApi<void>(`/api/tracking/clans/${encodeURIComponent(tag)}`, {
    method: 'DELETE',
  });
}

export async function getClan(tag: string): Promise<ClanResponse> {
  return fetchApi<ClanResponse>(`/api/clans/${encodeURIComponent(tag)}`);
}

export async function getClanMembers(tag: string): Promise<PlayerResponse[]> {
  return fetchApi<PlayerResponse[]>(`/api/clans/${encodeURIComponent(tag)}/members`);
}

// CWL endpoints
export async function syncCwl(clanTag: string): Promise<CwlSeasonResponse | null> {
  return fetchApi<CwlSeasonResponse | null>(
    `/api/clans/${encodeURIComponent(clanTag)}/cwl/sync`,
    { method: 'POST' }
  );
}

export async function getSeasonHistory(clanTag: string): Promise<CwlSeasonResponse[]> {
  return fetchApi<CwlSeasonResponse[]>(`/api/clans/${encodeURIComponent(clanTag)}/cwl`);
}

export async function getSeason(clanTag: string, season: string): Promise<CwlSeasonResponse> {
  return fetchApi<CwlSeasonResponse>(
    `/api/clans/${encodeURIComponent(clanTag)}/cwl/${encodeURIComponent(season)}`
  );
}

export async function getWar(warTag: string): Promise<CwlWarResponse> {
  return fetchApi<CwlWarResponse>(`/api/cwl/wars/${encodeURIComponent(warTag)}`);
}

export async function getLeaderboard(clanTag: string, season: string): Promise<LeaderBoardResponse> {
  return fetchApi<LeaderBoardResponse>(
    `/api/clans/${encodeURIComponent(clanTag)}/cwl/${encodeURIComponent(season)}/leaderboard`
  );
}

// Player endpoints
export async function getPlayer(tag: string): Promise<PlayerResponse> {
  return fetchApi<PlayerResponse>(`/api/players/${encodeURIComponent(tag)}`);
}
