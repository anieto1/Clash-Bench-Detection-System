package com.pm.clashbenchdetectionsystem.clan;

import com.pm.clashbenchdetectionsystem.clan.clanDTO.ClanResponse;
import com.pm.clashbenchdetectionsystem.cocAPI.CocApiClient;
import com.pm.clashbenchdetectionsystem.cocAPI.dto.CocClanResponse;
import com.pm.clashbenchdetectionsystem.common.exception.ResourceNotFoundException;
import com.pm.clashbenchdetectionsystem.config.RedisCacheConfig;
import com.pm.clashbenchdetectionsystem.player.PlayerService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class ClanService {

    private final ClanRepository clanRepository;
    private final TrackedClanRepository trackedClanRepository;
    private final ClanMapper clanMapper;
    private final CocApiClient cocApiClient;
    private final PlayerService playerService;
    private final Timer getClanTimer;

    public ClanService(ClanRepository clanRepository,
                       TrackedClanRepository trackedClanRepository,
                       ClanMapper clanMapper,
                       CocApiClient cocApiClient,
                       PlayerService playerService,
                       MeterRegistry meterRegistry) {
        this.clanRepository = clanRepository;
        this.trackedClanRepository = trackedClanRepository;
        this.clanMapper = clanMapper;
        this.cocApiClient = cocApiClient;
        this.playerService = playerService;
        this.getClanTimer = Timer.builder("coc.api.request")
                .description("CoC API request duration")
                .tag("endpoint", "getClan")
                .register(meterRegistry);
    }

    @Cacheable(value = RedisCacheConfig.CACHE_CLAN, key = "#clanTag")
    @Transactional(readOnly = true)
    public ClanResponse getClan(String clanTag) {
        Clan clan = clanRepository.findById(clanTag)
                .orElseThrow(() -> ResourceNotFoundException.clan(clanTag));
        return clanMapper.toResponse(clan);
    }

    @CacheEvict(value = RedisCacheConfig.CACHE_CLAN, key = "#clanTag")
    @Transactional
    public ClanResponse refreshClan(String clanTag) {
        Clan clan = refreshFromApi(clanTag);
        return clanMapper.toResponse(clan);
    }

    @CacheEvict(value = RedisCacheConfig.CACHE_CLAN, key = "#clanTag")
    @Transactional
    public void startTracking(String clanTag) {
        CocClanResponse apiResponse = null;
        if (!clanRepository.existsById(clanTag)) {
            apiResponse = getClanTimer.record(() -> cocApiClient.getClan(clanTag));
            syncClan(apiResponse);
        }

        TrackedClan tracked = trackedClanRepository.findById(clanTag)
                .orElse(new TrackedClan(clanTag));
        tracked.activate();
        trackedClanRepository.save(tracked);
        log.info("Started tracking clan {}", clanTag);

        // Sync members in the background — fetch each member's profile
        if (apiResponse == null) {
            apiResponse = getClanTimer.record(() -> cocApiClient.getClan(clanTag));
        }
        if (apiResponse.memberList() != null) {
            for (CocClanResponse.CocClanMember member : apiResponse.memberList()) {
                try {
                    playerService.syncPlayerFromApi(member.tag());
                } catch (Exception ex) {
                    log.warn("Failed to sync member {} during tracking setup: {}",
                            member.tag(), ex.getMessage());
                }
            }
            log.info("Synced {} members for tracked clan {}", apiResponse.memberList().size(), clanTag);
        }
    }

    @Transactional
    public void stopTracking(String clanTag) {
        TrackedClan tracked = trackedClanRepository.findById(clanTag)
                .orElseThrow(() -> ResourceNotFoundException.clan(clanTag));
        tracked.deactivate();
        trackedClanRepository.save(tracked);
        log.info("Stopped tracking clan {}", clanTag);
    }

    @Transactional(readOnly = true)
    public List<ClanResponse> getTrackedClans() {
        return trackedClanRepository.findByActiveTrue().stream()
                .map(tc -> clanRepository.findById(tc.getClanTag()).orElse(null))
                .filter(clan -> clan != null)
                .map(clanMapper::toResponse)
                .toList();
    }

    /* ==================== Private helpers ==================== */

    private Clan refreshFromApi(String clanTag) {
        log.debug("Refreshing clan {} from CoC API", clanTag);
        CocClanResponse apiResponse = getClanTimer.record(() -> cocApiClient.getClan(clanTag));
        return syncClan(apiResponse);
    }

    private Clan syncClan(CocClanResponse apiResponse) {
        ClanUpdateData updateData = new ClanUpdateData(
                apiResponse.name(),
                apiResponse.clanLevel(),
                apiResponse.clanPoints(),
                apiResponse.warWins(),
                apiResponse.warTies(),
                apiResponse.warLosses(),
                apiResponse.description(),
                apiResponse.badgeUrls() != null ? apiResponse.badgeUrls().medium() : null
        );

        Clan clan = clanRepository.findById(apiResponse.tag())
                .orElseGet(() -> Clan.builder()
                        .tag(apiResponse.tag())
                        .name(updateData.name())
                        .clanLevel(updateData.clanLevel())
                        .clanPoints(updateData.clanPoints())
                        .warWins(updateData.warWins())
                        .warTies(updateData.warTies())
                        .warLosses(updateData.warLosses())
                        .description(updateData.description())
                        .badgeUrl(updateData.badgeUrl())
                        .build());

        clan.updateFrom(updateData);
        return clanRepository.save(clan);
    }
}
