import { cn } from '@/lib/utils';

interface THBadgeProps {
  level: number;
  size?: 'sm' | 'md' | 'lg';
  className?: string;
}

function getTHColor(level: number) {
  if (level >= 16) return 'bg-th-legendary/20 text-violet-400 border-th-legendary/40';
  if (level >= 14) return 'bg-th-high/20 text-blue-400 border-th-high/40';
  if (level >= 12) return 'bg-th-mid-high/20 text-teal-300 border-th-mid-high/40';
  if (level >= 10) return 'bg-th-mid/20 text-green-400 border-th-mid/40';
  if (level >= 8) return 'bg-th-low-mid/20 text-yellow-300 border-th-low-mid/40';
  return 'bg-th-low/20 text-gray-300 border-th-low/40';
}

const sizeMap = {
  sm: 'w-6 h-6 text-[10px]',
  md: 'w-7 h-7 text-xs',
  lg: 'w-9 h-9 text-sm',
};

export function THBadge({ level, size = 'md', className }: THBadgeProps) {
  return (
    <span
      className={cn(
        'inline-flex items-center justify-center rounded font-extrabold border shrink-0',
        sizeMap[size],
        getTHColor(level),
        className,
      )}
    >
      {level}
    </span>
  );
}
