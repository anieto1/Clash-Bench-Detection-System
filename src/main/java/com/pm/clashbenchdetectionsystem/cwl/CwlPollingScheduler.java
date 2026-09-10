package com.pm.clashbenchdetectionsystem.cwl;

import com.pm.clashbenchdetectionsystem.clan.TrackedClan;
import com.pm.clashbenchdetectionsystem.clan.TrackedClanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CwlPollingScheduler {

    private final TrackedClanRepository trackedClanRepository;
    private final CwlService cwlService;

    /**
     * Poll CWL data for all active tracked clans every 5 minutes.
     * During active CWL, this captures attacks before they disappear.
     */
    @Scheduled(fixedRateString = "${polling.cwl.interval:300000}")
    public void pollCwlData() {
        List<TrackedClan> activeClans = trackedClanRepository.findByActiveTrue();

        if (activeClans.isEmpty()) {
            return;
        }

        log.debug("Polling CWL data for {} tracked clans", activeClans.size());

        for (TrackedClan tracked : activeClans) {
            try {
                cwlService.syncLeagueGroup(tracked.getClanTag());
                tracked.markPolled();
                trackedClanRepository.save(tracked);
            } catch (Exception e) {
                log.warn("Failed to poll CWL for clan {}: {}", tracked.getClanTag(), e.getMessage());
            }
        }
    }
}
