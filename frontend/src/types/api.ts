// Clan DTOs
export interface ClanResponse {
  tag: string;
  name: string;
  clanLevel: number;
  clanPoints: number;
  warWins: number;
  warTies: number;
  warLosses: number;
  description: string | null;
  badgeUrl: string | null;
  updatedAt: string;
}

// Player DTOs
export interface PlayerResponse {
  tag: string;
  name: string;
  townHallLevel: number;
  clanTag: string | null;
  clanRole: string | null;
  warStars: number;
  donations: number;
  donationsReceived: number;
  expLevel: number;
  trophies: number;
  heroes: HeroDto[];
  troops: TroopDto[];
  spells: SpellDto[];
  pets: PetDto[];
  equipment: EquipmentDto[];
  updatedAt: string;
}

export interface HeroDto {
  name: string;
  level: number;
  maxLevel: number;
}

export interface TroopDto {
  name: string;
  level: number;
  maxLevel: number;
}

export interface SpellDto {
  name: string;
  level: number;
  maxLevel: number;
}

export interface PetDto {
  name: string;
  level: number;
  maxLevel: number;
}

export interface EquipmentDto {
  name: string;
  heroName: string;
  level: number;
  maxLevel: number;
}

// CWL DTOs
export interface CwlSeasonResponse {
  clanTag: string;
  season: string;
  leagueName: string | null;
  finalPlacement: number | null;
  totalStars: number;
  totalDestruction: number;
  completed: boolean;
  participants: ParticipantDto[];
  wars: WarSummaryDto[];
  updatedAt: string;
}

export interface ParticipantDto {
  playerTag: string;
  playerName: string;
  townHallLevel: number;
}

export interface WarSummaryDto {
  warTag: string;
  dayNumber: number;
  opponentClanName: string;
  ourStars: number;
  opponentStars: number;
  result: string | null;
}

export interface CwlWarResponse {
  warTag: string;
  dayNumber: number;
  opponentClanTag: string;
  opponentClanName: string;
  opponentClanLevel: number | null;
  ourStars: number;
  ourDestruction: number;
  opponentStars: number;
  opponentDestruction: number;
  result: string | null;
  startTime: string | null;
  endTime: string | null;
  members: WarMemberDto[];
  attacks: AttackDto[];
}

export interface WarMemberDto {
  playerTag: string;
  mapPosition: number;
  attacked: boolean;
}

export interface AttackDto {
  attackerTag: string;
  attackerMapPosition: number;
  defenderTag: string;
  defenderMapPosition: number;
  defenderThLevel: number;
  stars: number;
  destructionPercentage: number;
  attackOrder: number | null;
}

// Leaderboard DTOs
export interface LeaderBoardResponse {
  season: string;
  clanTag: string;
  entries: LeaderBoardEntry[];
}

export interface LeaderBoardEntry {
  playerTag: string;
  playerName: string;
  townHallLevel: number;
  totalScore: number;
  attacksMade: number;
  attacksMissed: number;
  averageStars: number;
  averageDestruction: number;
  attackScores: AttackScoreDto[];
}

export interface AttackScoreDto {
  attackerMapPosition: number;
  defenderMapPosition: number;
  attackerTh: number;
  defenderTh: number;
  stars: number;
  destructionPercentage: number;
  baseScore: number;
  destructionModifier: number;
  positionModifier: number;
  thModifier: number;
  topBaseBonus: number;
  gimmePenalty: number;
  totalScore: number;
}
