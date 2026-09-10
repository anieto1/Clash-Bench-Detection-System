import { cn } from '@/lib/utils';
import { formatScore } from '@/lib/scoring';

interface ScoreBadgeProps {
  score: number;
  size?: 'sm' | 'md' | 'lg';
  className?: string;
}

function getScoreStyle(score: number) {
  if (score >= 100) return 'bg-emerald-500/15 text-emerald-400 border-emerald-500/30';
  if (score >= 60) return 'bg-green-500/15 text-green-400 border-green-500/30';
  if (score >= 30) return 'bg-blue-500/15 text-blue-400 border-blue-500/30';
  if (score >= 0) return 'bg-gray-500/15 text-gray-300 border-gray-500/30';
  if (score >= -30) return 'bg-yellow-500/15 text-yellow-300 border-yellow-500/30';
  if (score >= -60) return 'bg-orange-500/15 text-orange-400 border-orange-500/30';
  return 'bg-red-500/25 text-red-300 border-red-500/50';
}

const sizeMap = {
  sm: 'min-w-10 h-6 px-1.5 text-xs',
  md: 'min-w-14 h-8 px-2 text-sm',
  lg: 'min-w-18 h-10 px-3 text-base',
};

export function ScoreBadge({ score, size = 'md', className }: ScoreBadgeProps) {
  return (
    <span
      className={cn(
        'inline-flex items-center justify-center rounded-md font-extrabold border',
        sizeMap[size],
        getScoreStyle(score),
        className,
      )}
    >
      {formatScore(score)}
    </span>
  );
}
