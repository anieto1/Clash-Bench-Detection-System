import { Star } from 'lucide-react';

interface StarDisplayProps {
  stars: number;
  maxStars?: number;
  size?: 'sm' | 'md' | 'lg';
  animate?: boolean;
}

const sizeMap = {
  sm: 'h-3.5 w-3.5',
  md: 'h-5 w-5',
  lg: 'h-6 w-6',
};

export function StarDisplay({ stars, maxStars = 3, size = 'md', animate = false }: StarDisplayProps) {
  return (
    <div className="flex gap-0.5">
      {Array.from({ length: maxStars }, (_, i) => (
        <Star
          key={i}
          className={`${sizeMap[size]} ${
            i < stars
              ? 'fill-coc-star text-coc-star drop-shadow-[0_1px_2px_rgba(0,0,0,0.3)]'
              : 'fill-transparent text-[#374151]'
          } ${animate && i < stars ? 'star-animate' : ''}`}
        />
      ))}
    </div>
  );
}
