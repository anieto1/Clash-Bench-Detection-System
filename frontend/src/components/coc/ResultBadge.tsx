import { cn } from '@/lib/utils';

interface ResultBadgeProps {
  result: string | null;
  className?: string;
}

export function ResultBadge({ result, className }: ResultBadgeProps) {
  if (!result) return null;

  const styles: Record<string, string> = {
    WIN: 'bg-coc-win/15 text-green-400 border-coc-win/30',
    LOSE: 'bg-coc-loss/15 text-red-400 border-coc-loss/30',
    TIE: 'bg-coc-draw/15 text-yellow-400 border-coc-draw/30',
  };

  return (
    <span
      className={cn(
        'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-bold border uppercase tracking-wider',
        styles[result] ?? 'bg-muted text-muted-foreground border-border',
        className,
      )}
    >
      {result}
    </span>
  );
}
