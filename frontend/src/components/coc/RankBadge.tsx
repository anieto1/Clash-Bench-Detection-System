import { Trophy } from 'lucide-react';
import { cn } from '@/lib/utils';

interface RankBadgeProps {
  rank: number;
  className?: string;
}

export function RankBadge({ rank, className }: RankBadgeProps) {
  if (rank === 1) {
    return (
      <span className={cn('inline-flex items-center justify-center w-8 h-8 rounded-full bg-gradient-to-br from-[#FFD700] to-[#F59E0B] shadow-gold-glow', className)}>
        <Trophy className="h-4 w-4 text-[#0A1628]" />
      </span>
    );
  }
  if (rank === 2) {
    return (
      <span className={cn('inline-flex items-center justify-center w-8 h-8 rounded-full bg-gradient-to-br from-[#E5E7EB] to-[#9CA3AF]', className)}>
        <span className="text-xs font-extrabold text-[#0A1628]">{rank}</span>
      </span>
    );
  }
  if (rank === 3) {
    return (
      <span className={cn('inline-flex items-center justify-center w-8 h-8 rounded-full bg-gradient-to-br from-[#D97706] to-[#92400E]', className)}>
        <span className="text-xs font-extrabold text-white">{rank}</span>
      </span>
    );
  }
  return (
    <span className={cn('inline-flex items-center justify-center w-8 h-8 text-sm font-bold text-muted-foreground', className)}>
      {rank}
    </span>
  );
}
