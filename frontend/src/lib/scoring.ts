export type ScoreTier =
  | 'elite'
  | 'strong'
  | 'solid'
  | 'average'
  | 'belowAvg'
  | 'struggling'
  | 'bench';

export interface TierInfo {
  tier: ScoreTier;
  label: string;
  className: string;
}

export function getScoreTier(score: number): TierInfo {
  if (score >= 100) {
    return { tier: 'elite', label: 'Elite', className: 'bg-emerald-500 text-white' };
  }
  if (score >= 60) {
    return { tier: 'strong', label: 'Strong', className: 'bg-green-500 text-white' };
  }
  if (score >= 30) {
    return { tier: 'solid', label: 'Solid', className: 'bg-blue-500 text-white' };
  }
  if (score >= 0) {
    return { tier: 'average', label: 'Average', className: 'bg-gray-500 text-white' };
  }
  if (score >= -30) {
    return { tier: 'belowAvg', label: 'Below Avg', className: 'bg-yellow-500 text-black' };
  }
  if (score >= -60) {
    return { tier: 'struggling', label: 'Struggling', className: 'bg-orange-500 text-white' };
  }
  return { tier: 'bench', label: 'Bench', className: 'bg-red-500 text-white' };
}

export function formatScore(score: number): string {
  return score >= 0 ? `+${score}` : `${score}`;
}

export function getResultColor(result: string | null): string {
  switch (result) {
    case 'WIN':
      return 'text-green-500';
    case 'LOSE':
      return 'text-red-500';
    case 'TIE':
      return 'text-yellow-500';
    default:
      return 'text-muted-foreground';
  }
}
