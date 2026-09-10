import { Shield } from 'lucide-react';
import { cn } from '@/lib/utils';

interface ClanBadgeProps {
  badgeUrl: string | null | undefined;
  size?: 'sm' | 'md' | 'lg';
  className?: string;
}

const sizeMap = {
  sm: { img: 'h-8 w-8', icon: 'h-5 w-5', ring: 'ring-1' },
  md: { img: 'h-12 w-12', icon: 'h-8 w-8', ring: 'ring-2' },
  lg: { img: 'h-16 w-16', icon: 'h-10 w-10', ring: 'ring-2' },
};

export function ClanBadge({ badgeUrl, size = 'md', className }: ClanBadgeProps) {
  const s = sizeMap[size];

  if (badgeUrl) {
    return (
      <img
        src={badgeUrl}
        alt=""
        className={cn(
          s.img,
          'rounded-full ring-coc-gold-400 object-cover bg-coc-navy-200 shrink-0',
          s.ring,
          className,
        )}
      />
    );
  }

  return (
    <div
      className={cn(
        s.img,
        'rounded-full ring-coc-gold-400 bg-coc-navy-100 flex items-center justify-center shrink-0',
        s.ring,
        className,
      )}
    >
      <Shield className={cn(s.icon, 'text-coc-gold-400')} />
    </div>
  );
}
