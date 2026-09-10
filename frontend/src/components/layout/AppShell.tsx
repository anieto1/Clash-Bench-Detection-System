import { Link, Outlet, useLocation } from 'react-router-dom';
import { Home, Menu, Swords, User, LayoutDashboard } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Sheet, SheetContent, SheetTrigger } from '@/components/ui/sheet';
import { ClanBadge } from '@/components/coc';
import { useTrackedClans } from '@/hooks/queries';
import { useState } from 'react';

function Sidebar({ onNavigate }: { onNavigate?: () => void }) {
  const { data: clans } = useTrackedClans();
  const location = useLocation();

  return (
    <div className="flex h-full flex-col bg-sidebar">
      {/* Logo */}
      <div className="p-5">
        <Link
          to="/"
          onClick={onNavigate}
          className="flex items-center gap-3"
        >
          <div className="flex items-center justify-center w-10 h-10 rounded-lg bg-gradient-to-br from-[#FFD700] to-[#DAA520] shadow-gold-glow">
            <Swords className="h-5 w-5 text-[#0A1628]" />
          </div>
          <div>
            <span className="font-display text-xl text-coc-gold-300 tracking-wide">CBDS</span>
            <p className="text-[10px] text-muted-foreground leading-none mt-0.5">Clash Bench Detection</p>
          </div>
        </Link>
      </div>

      {/* Gold divider */}
      <div className="gold-divider mx-4" />

      {/* Navigation */}
      <nav className="flex-1 p-3 space-y-1 overflow-y-auto">
        <Link
          to="/"
          onClick={onNavigate}
          className={`flex items-center gap-3 px-3 py-2.5 rounded-lg transition-all text-sm font-semibold ${
            location.pathname === '/'
              ? 'bg-sidebar-accent text-coc-gold-300 shadow-[inset_3px_0_0_#F5C518]'
              : 'text-sidebar-foreground hover:bg-white/5 hover:text-coc-gold-100'
          }`}
        >
          <Home className="h-4 w-4 shrink-0" />
          <span>Dashboard</span>
        </Link>

        {clans && clans.length > 0 && (
          <>
            <div className="pt-4 pb-2 px-3">
              <p className="text-[10px] font-bold text-muted-foreground uppercase tracking-[0.15em]">
                Tracked Clans
              </p>
            </div>
            {clans.map((clan) => {
              const isActive = location.pathname === `/clans/${encodeURIComponent(clan.tag)}`;
              return (
                <Link
                  key={clan.tag}
                  to={`/clans/${encodeURIComponent(clan.tag)}`}
                  onClick={onNavigate}
                  className={`flex items-center gap-3 px-3 py-2 rounded-lg transition-all text-sm ${
                    isActive
                      ? 'bg-sidebar-accent text-coc-gold-300 shadow-[inset_3px_0_0_#F5C518]'
                      : 'text-sidebar-foreground hover:bg-white/5 hover:text-white'
                  }`}
                >
                  <ClanBadge badgeUrl={clan.badgeUrl} size="sm" />
                  <span className="truncate font-medium">{clan.name}</span>
                </Link>
              );
            })}
          </>
        )}
      </nav>

      {/* Footer */}
      <div className="p-4 border-t border-sidebar-border">
        <p className="text-[10px] text-muted-foreground text-center">
          CWL Performance Tracker
        </p>
      </div>
    </div>
  );
}

const mobileNavItems = [
  { path: '/', icon: LayoutDashboard, label: 'Home' },
  { path: '/clans', icon: Swords, label: 'Clans' },
  { path: '/players', icon: User, label: 'Player' },
];

function MobileBottomNav() {
  const location = useLocation();

  return (
    <nav className="fixed bottom-0 left-0 right-0 z-50 md:hidden border-t-2 border-coc-gold-500/30 bg-[#0F1D32]/95 backdrop-blur-lg">
      <div className="flex items-center justify-around h-16 pb-[env(safe-area-inset-bottom)]">
        {mobileNavItems.map((item) => {
          const isActive = item.path === '/'
            ? location.pathname === '/'
            : location.pathname.startsWith(item.path);
          const Icon = item.icon;
          return (
            <Link
              key={item.path}
              to={item.path}
              className={`flex flex-col items-center justify-center gap-1 flex-1 h-full transition-colors ${
                isActive
                  ? 'text-coc-gold-300'
                  : 'text-muted-foreground'
              }`}
            >
              <Icon className={`h-5 w-5 ${isActive ? 'drop-shadow-[0_0_6px_rgba(245,197,24,0.5)]' : ''}`} />
              <span className="text-[10px] font-bold tracking-wide">{item.label}</span>
            </Link>
          );
        })}
      </div>
    </nav>
  );
}

export function AppShell() {
  const [open, setOpen] = useState(false);

  return (
    <div className="min-h-screen flex">
      {/* Desktop sidebar */}
      <aside className="hidden md:flex w-64 flex-col border-r border-sidebar-border bg-sidebar shrink-0 sticky top-0 h-screen">
        <Sidebar />
      </aside>

      {/* Main content area */}
      <div className="flex-1 flex flex-col min-w-0">
        {/* Top header bar */}
        <header className="sticky top-0 z-40 flex h-14 items-center gap-4 border-b border-border/50 bg-[#0A1628]/90 backdrop-blur-md px-4">
          {/* Mobile hamburger for sidebar access */}
          <Sheet open={open} onOpenChange={setOpen}>
            <SheetTrigger asChild>
              <Button variant="ghost" size="icon" className="md:hidden text-coc-gold-400 hover:text-coc-gold-200 hover:bg-coc-gold-400/10">
                <Menu className="h-5 w-5" />
              </Button>
            </SheetTrigger>
            <SheetContent side="left" className="w-72 p-0 border-r-coc-gold-500/30">
              <Sidebar onNavigate={() => setOpen(false)} />
            </SheetContent>
          </Sheet>

          {/* Mobile logo (centered) */}
          <div className="md:hidden flex-1 flex justify-center">
            <Link to="/" className="font-display text-lg text-coc-gold-300 tracking-wide">CBDS</Link>
          </div>

          {/* Desktop spacer */}
          <div className="hidden md:block flex-1" />

          {/* Right side placeholder */}
          <div className="w-10" />
        </header>

        {/* Page content */}
        <main className="flex-1 p-4 md:p-6 pb-mobile-nav">
          <Outlet />
        </main>
      </div>

      {/* Mobile bottom tab bar */}
      <MobileBottomNav />
    </div>
  );
}
