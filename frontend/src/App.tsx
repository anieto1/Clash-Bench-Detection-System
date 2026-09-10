import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { AppShell } from '@/components/layout/AppShell';
import { DashboardPage } from '@/pages/DashboardPage';
import { ClanDetailPage } from '@/pages/ClanDetailPage';
import { SeasonDetailPage } from '@/pages/SeasonDetailPage';
import { WarDetailPage } from '@/pages/WarDetailPage';
import { PlayerProfilePage } from '@/pages/PlayerProfilePage';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 1000 * 60 * 5, // 5 minutes
      retry: 1,
    },
  },
});

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <Routes>
          <Route element={<AppShell />}>
            <Route path="/" element={<DashboardPage />} />
            <Route path="/clans/:tag" element={<ClanDetailPage />} />
            <Route path="/clans/:clanTag/cwl/:season" element={<SeasonDetailPage />} />
            <Route path="/cwl/wars/:warTag" element={<WarDetailPage />} />
            <Route path="/players/:tag" element={<PlayerProfilePage />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </QueryClientProvider>
  );
}

export default App;
