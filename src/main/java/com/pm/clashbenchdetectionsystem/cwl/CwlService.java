package com.pm.clashbenchdetectionsystem.cwl;

import com.pm.clashbenchdetectionsystem.cocAPI.CocApiClient;
import com.pm.clashbenchdetectionsystem.cocAPI.dto.CocCwlWarResponse;
import com.pm.clashbenchdetectionsystem.cocAPI.dto.CocPlayerResponse;
import com.pm.clashbenchdetectionsystem.cocAPI.dto.LeagueGroupResponse;
import com.pm.clashbenchdetectionsystem.common.exception.ResourceNotFoundException;
import com.pm.clashbenchdetectionsystem.config.RedisCacheConfig;
import com.pm.clashbenchdetectionsystem.cwl.cwlDTO.CwlSeasonResponse;
import com.pm.clashbenchdetectionsystem.cwl.cwlDTO.CwlWarResponse;
import com.pm.clashbenchdetectionsystem.cwl.cwlDTO.LeaderBoardResponse;
import com.pm.clashbenchdetectionsystem.player.PlayerService;
import com.pm.clashbenchdetectionsystem.scoring.CbdsScoreService;
import com.pm.clashbenchdetectionsystem.scoring.SeasonScore;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Slf4j
public class CwlService {

    private final CocApiClient cocApiClient;
    private final CwlSeasonRepository cwlSeasonRepository;
    private final CwlWarRepository cwlWarRepository;
    private final CwlParticipantRepository cwlParticipantRepository;
    private final CwlWarMemberRepository cwlWarMemberRepository;
    private final CwlAttackRepository cwlAttackRepository;
    private final CwlMapper cwlMapper;
    private final CbdsScoreService cbdsScoreService;
    private final PlayerService playerService;
    private final CacheManager cacheManager;

    private final Timer syncTimer;
    private final Timer warFetchTimer;
    private final Counter participantsSynced;
    private final Counter warsSynced;

    public CwlService(CocApiClient cocApiClient,
                      CwlSeasonRepository cwlSeasonRepository,
                      CwlWarRepository cwlWarRepository,
                      CwlParticipantRepository cwlParticipantRepository,
                      CwlWarMemberRepository cwlWarMemberRepository,
                      CwlAttackRepository cwlAttackRepository,
                      CwlMapper cwlMapper,
                      CbdsScoreService cbdsScoreService,
                      PlayerService playerService,
                      CacheManager cacheManager,
                      MeterRegistry meterRegistry) {
        this.cocApiClient = cocApiClient;
        this.cwlSeasonRepository = cwlSeasonRepository;
        this.cwlWarRepository = cwlWarRepository;
        this.cwlParticipantRepository = cwlParticipantRepository;
        this.cwlWarMemberRepository = cwlWarMemberRepository;
        this.cwlAttackRepository = cwlAttackRepository;
        this.cwlMapper = cwlMapper;
        this.cbdsScoreService = cbdsScoreService;
        this.playerService = playerService;
        this.cacheManager = cacheManager;
        this.syncTimer = Timer.builder("cwl.sync.duration")
                .description("Time taken to complete a full CWL league group sync")
                .register(meterRegistry);
        this.warFetchTimer = Timer.builder("coc.api.request")
                .description("CoC API request duration")
                .tag("endpoint", "getCwlWar")
                .register(meterRegistry);
        this.participantsSynced = Counter.builder("cwl.participants.synced")
                .description("Number of CWL participants synced")
                .register(meterRegistry);
        this.warsSynced = Counter.builder("cwl.wars.synced")
                .description("Number of CWL wars synced")
                .register(meterRegistry);
    }


    @Transactional
    public CwlSeasonResponse syncLeagueGroup(String clanTag) {
        return syncTimer.record(() -> doSyncLeagueGroup(clanTag));
    }

    private CwlSeasonResponse doSyncLeagueGroup(String clanTag) {
        log.info("Syncing CWL league group for clan {}", clanTag);

        LeagueGroupResponse leagueGroup = cocApiClient.getLeagueGroup(clanTag);

        if (leagueGroup == null || leagueGroup.state() == null) {
            log.info("No CWL data available for clan {}", clanTag);
            return null;
        }

        String state = leagueGroup.state();
        if ("notInWar".equals(state) || "groupNotFound".equals(state)) {
            log.info("Clan {} is not in CWL (state: {})", clanTag, state);
            return null;
        }

        String season = leagueGroup.season();

        CwlSeason cwlSeason = cwlSeasonRepository.findByClanTagAndSeason(clanTag, season)
                .orElseGet(() -> {
                    CwlSeason newSeason = CwlSeason.builder()
                            .clanTag(clanTag)
                            .season(season)
                            .build();
                    return cwlSeasonRepository.save(newSeason);
                });

        LeagueGroupResponse.LeagueGroupClan ourClan = leagueGroup.clans().stream()
                .filter(c -> c.tag().equals(clanTag))
                .findFirst()
                .orElse(null);

        if (ourClan == null) {
            log.warn("Clan {} not found in league group response", clanTag);
            return cwlMapper.toSeasonResponse(cwlSeason);
        }

        // Collect participant tags for targeted cache eviction
        List<String> participantTags = syncParticipants(clanTag, season, ourClan);

        // Collect war tags for targeted cache eviction
        List<String> syncedWarTags = new ArrayList<>();
        List<LeagueGroupResponse.LeagueRound> rounds = leagueGroup.rounds();
        for (int i = 0; i < rounds.size(); i++) {
            short dayNumber = (short) (i + 1);
            List<String> warTags = rounds.get(i).warTags();

            for (String warTag : warTags) {
                if ("#0".equals(warTag)) {
                    continue;
                }
                syncWar(warTag, clanTag, season, dayNumber);
                syncedWarTags.add(warTag);
            }
        }

        cwlSeason = cwlSeasonRepository.findByClanTagAndSeason(clanTag, season)
                .orElseThrow(() -> ResourceNotFoundException.cwlSeason(clanTag, season));

        // Persist CBDS scores after syncing all war data
        try {
            cbdsScoreService.calculateAndPersistScores(clanTag, season);
        } catch (Exception ex) {
            log.warn("Failed to persist CBDS scores for {}/{}: {}", clanTag, season, ex.getMessage());
        }

        // Targeted cache eviction — only evict keys affected by THIS sync
        evictCwlCaches(clanTag, season, syncedWarTags, participantTags);

        return cwlMapper.toSeasonResponse(cwlSeason);
    }

    @Transactional(readOnly = true)
    public List<CwlSeasonResponse> getSeasonHistory(String clanTag) {
        return cwlSeasonRepository.findByClanTagOrderBySeasonDesc(clanTag).stream()
                .map(cwlMapper::toSeasonResponse)
                .toList();
    }

    @Cacheable(value = RedisCacheConfig.CACHE_CWL_SEASON, key = "#clanTag + ':' + #season")
    @Transactional(readOnly = true)
    public CwlSeasonResponse getSeason(String clanTag, String season) {
        CwlSeason cwlSeason = cwlSeasonRepository.findByClanTagAndSeason(clanTag, season)
                .orElseThrow(() -> ResourceNotFoundException.cwlSeason(clanTag, season));
        return cwlMapper.toSeasonResponse(cwlSeason);
    }

    @Cacheable(value = RedisCacheConfig.CACHE_CWL_WAR, key = "#warTag")
    @Transactional(readOnly = true)
    public CwlWarResponse getWar(String warTag) {
        CwlWar war = cwlWarRepository.findById(warTag)
                .orElseThrow(() -> ResourceNotFoundException.cwlWar(warTag));
        return cwlMapper.toWarResponse(war);
    }

    @Cacheable(value = RedisCacheConfig.CACHE_LEADERBOARD, key = "#clanTag + ':' + #season")
    @Transactional(readOnly = true)
    public LeaderBoardResponse getLeaderboard(String clanTag, String season) {
        List<SeasonScore> scores = cbdsScoreService.calculateLeaderboard(clanTag, season);

        List<LeaderBoardResponse.LeaderBoardEntry> entries = scores.stream()
                .map(s -> new LeaderBoardResponse.LeaderBoardEntry(
                        s.playerTag(),
                        s.playerName(),
                        s.townHallLevel(),
                        s.totalScore(),
                        s.attacksMade(),
                        s.attacksMissed(),
                        s.averageStars(),
                        s.averageDestruction(),
                        s.scoredAttacks().stream()
                                .map(sa -> new LeaderBoardResponse.AttackScoreDto(
                                        sa.attackerMapPosition(),
                                        sa.defenderMapPosition(),
                                        sa.attackerTh(),
                                        sa.defenderTh(),
                                        sa.stars(),
                                        sa.destructionPercentage(),
                                        sa.score().baseScore(),
                                        sa.score().destructionModifier(),
                                        sa.score().positionModifier(),
                                        sa.score().thModifier(),
                                        sa.score().topBaseBonus(),
                                        sa.score().gimmePenalty(),
                                        sa.score().totalScore()
                                ))
                                .toList()
                ))
                .toList();

        return new LeaderBoardResponse(season, clanTag, entries);
    }

    /* ==================== Private sync helpers ==================== */

    private List<String> syncParticipants(String clanTag, String season,
                                          LeagueGroupResponse.LeagueGroupClan ourClan) {
        if (ourClan.members() == null) return List.of();

        List<String> participantTags = new ArrayList<>();
        for (LeagueGroupResponse.LeagueGroupMember member : ourClan.members()) {
            participantTags.add(member.tag());
            CwlParticipantId id = new CwlParticipantId(clanTag, season, member.tag());

            if (cwlParticipantRepository.existsById(id)) {
                continue;
            }

            String snapshot = buildFullSnapshot(member);

            try {
                playerService.syncPlayerFromApi(member.tag());
            } catch (Exception ex) {
                log.warn("Failed to sync player {} during CWL participant sync: {}",
                        member.tag(), ex.getMessage());
            }

            CwlParticipant participant = new CwlParticipant(clanTag, season, member.tag(), snapshot);
            cwlParticipantRepository.save(participant);
            participantsSynced.increment();
        }

        log.debug("Synced {} participants for {} season {}", ourClan.members().size(), clanTag, season);
        return participantTags;
    }

    private void syncWar(String warTag, String clanTag, String season, short dayNumber) {
        CocCwlWarResponse warResponse;
        try {
            warResponse = warFetchTimer.recordCallable(() -> cocApiClient.getCwlWar(warTag));
        } catch (Exception e) {
            log.warn("Failed to fetch CWL war {}: {}", warTag, e.getMessage());
            return;
        }
        warsSynced.increment();

        if (warResponse == null || warResponse.clan() == null || warResponse.opponent() == null) {
            return;
        }

        CocCwlWarResponse.WarClan ourSide;
        CocCwlWarResponse.WarClan opponentSide;

        if (clanTag.equals(warResponse.clan().tag())) {
            ourSide = warResponse.clan();
            opponentSide = warResponse.opponent();
        } else if (clanTag.equals(warResponse.opponent().tag())) {
            ourSide = warResponse.opponent();
            opponentSide = warResponse.clan();
        } else {
            return;
        }

        CwlWar war = cwlWarRepository.findById(warTag)
                .orElseGet(() -> {
                    CwlWar newWar = CwlWar.builder()
                            .warTag(warTag)
                            .clanTag(clanTag)
                            .season(season)
                            .dayNumber(dayNumber)
                            .opponentClanTag(opponentSide.tag())
                            .opponentClanName(opponentSide.name())
                            .opponentClanLevel(opponentSide.clanLevel())
                            .startTime(parseApiTime(warResponse.startTime()))
                            .endTime(parseApiTime(warResponse.endTime()))
                            .build();
                    return cwlWarRepository.save(newWar);
                });

        if (ourSide.members() != null) {
            for (CocCwlWarResponse.WarMember apiMember : ourSide.members()) {
                syncWarMember(war, apiMember, opponentSide);
            }
        }

        if ("warEnded".equals(warResponse.state())) {
            war.updateScores(
                    (short)(int) ourSide.stars(),
                    BigDecimal.valueOf(ourSide.destructionPercentage()),
                    (short)(int) opponentSide.stars(),
                    BigDecimal.valueOf(opponentSide.destructionPercentage())
            );

            String result = determineResult(ourSide, opponentSide);
            war.updateResult(result);

            cwlWarRepository.save(war);
        }
    }

    private void syncWarMember(CwlWar war, CocCwlWarResponse.WarMember apiMember,
                               CocCwlWarResponse.WarClan opponentSide) {
        String warTag = war.getWarTag();
        String playerTag = apiMember.tag();

        CwlWarMember warMember = cwlWarMemberRepository
                .findByWarTagAndPlayerTag(warTag, playerTag)
                .orElseGet(() -> {
                    CwlWarMember newMember = new CwlWarMember(
                            warTag, playerTag,
                            (short)(int) apiMember.mapPosition(),
                            (short)(int) apiMember.townhallLevel()
                    );
                    return cwlWarMemberRepository.save(newMember);
                });

        if (apiMember.attacks() != null && !apiMember.attacks().isEmpty()) {
            warMember.markAttacked();
            cwlWarMemberRepository.save(warMember);

            CocCwlWarResponse.WarAttack apiAttack = apiMember.attacks().getFirst();

            CwlAttackId attackId = new CwlAttackId(warTag, playerTag);
            if (!cwlAttackRepository.existsById(attackId)) {
                short defenderMapPos = 0;
                String defenderName = null;
                short defenderThLevel = 1;

                if (opponentSide.members() != null) {
                    for (CocCwlWarResponse.WarMember oppMember : opponentSide.members()) {
                        if (oppMember.tag().equals(apiAttack.defenderTag())) {
                            defenderMapPos = (short)(int) oppMember.mapPosition();
                            defenderName = oppMember.name();
                            defenderThLevel = (short)(int) oppMember.townhallLevel();
                            break;
                        }
                    }
                }

                CwlAttack attack = new CwlAttack(
                        warTag,
                        playerTag,
                        (short)(int) apiMember.mapPosition(),
                        apiAttack.defenderTag(),
                        defenderName,
                        defenderThLevel,
                        defenderMapPos,
                        (short)(int) apiAttack.stars(),
                        BigDecimal.valueOf(apiAttack.destructionPercentage()),
                        (short)(int) apiAttack.order()
                );
                cwlAttackRepository.save(attack);
            }
        }
    }


    private String determineResult(CocCwlWarResponse.WarClan ourSide,
                                   CocCwlWarResponse.WarClan opponentSide) {
        if (ourSide.stars() > opponentSide.stars()) return "WIN";
        if (ourSide.stars() < opponentSide.stars()) return "LOSE";
        if (ourSide.destructionPercentage() > opponentSide.destructionPercentage()) return "WIN";
        if (ourSide.destructionPercentage() < opponentSide.destructionPercentage()) return "LOSE";
        return "TIE";
    }

    private void evictCwlCaches(String clanTag, String season,
                                List<String> warTags, List<String> participantTags) {
        evict(RedisCacheConfig.CACHE_CWL_SEASON, clanTag + ":" + season);
        evict(RedisCacheConfig.CACHE_LEADERBOARD, clanTag + ":" + season);
        for (String warTag : warTags) {
            evict(RedisCacheConfig.CACHE_CWL_WAR, warTag);
        }
        for (String tag : participantTags) {
            evict(RedisCacheConfig.CACHE_PLAYER_CWL_HISTORY, tag);
            evict(RedisCacheConfig.CACHE_PLAYER_STATS, tag);
        }
        log.debug("Evicted caches for clan {} season {} ({} wars, {} participants)",
                clanTag, season, warTags.size(), participantTags.size());
    }

    private void evict(String cacheName, String key) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.evict(key);
        }
    }

    private String buildFullSnapshot(LeagueGroupResponse.LeagueGroupMember member) {
        try {
            CocPlayerResponse playerData = cocApiClient.getPlayer(member.tag());
            return StatsSnapshotHelper.buildFullSnapshot(playerData);
        } catch (Exception ex) {
            log.warn("Failed to fetch full player profile for snapshot {}, using minimal: {}",
                    member.tag(), ex.getMessage());
            return StatsSnapshotHelper.buildSnapshot(member.name(), member.townHallLevel());
        }
    }

    private Instant parseApiTime(String apiTime) {
        if (apiTime == null || apiTime.isBlank()) return null;
        try {
            // CoC API format: "20250201T120000.000Z"
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss.SSS'Z'")
                    .withZone(java.time.ZoneOffset.UTC);
            return Instant.from(formatter.parse(apiTime));
        } catch (Exception e) {
            log.warn("Failed to parse API time '{}': {}", apiTime, e.getMessage());
            return null;
        }
    }
}
