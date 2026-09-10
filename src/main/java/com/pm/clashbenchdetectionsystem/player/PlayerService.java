package com.pm.clashbenchdetectionsystem.player;


import com.pm.clashbenchdetectionsystem.cocAPI.CocApiClient;
import com.pm.clashbenchdetectionsystem.common.exception.*;
import com.pm.clashbenchdetectionsystem.cocAPI.dto.CocPlayerResponse;
import com.pm.clashbenchdetectionsystem.config.RedisCacheConfig;
import com.pm.clashbenchdetectionsystem.cwl.CwlParticipant;
import com.pm.clashbenchdetectionsystem.cwl.CwlParticipantRepository;
import com.pm.clashbenchdetectionsystem.cwl.StatsSnapshotHelper;

import com.pm.clashbenchdetectionsystem.player.playerDTO.PlayerCwlHistoryResponse;
import com.pm.clashbenchdetectionsystem.player.playerDTO.PlayerResponse;
import com.pm.clashbenchdetectionsystem.player.playerDTO.PlayerStatsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final PlayerMapper playerMapper;
    private final CocApiClient cocApiClient;
    private final CwlParticipantRepository cwlParticipantRepository;


    @Cacheable(value = RedisCacheConfig.CACHE_PLAYER, key = "#playerTag")
    @Transactional(readOnly = true)
    public PlayerResponse getPlayer(String playerTag) {
        Player player = playerRepository.findById(playerTag)
                .orElseThrow(() -> ResourceNotFoundException.player(playerTag));
        return playerMapper.toResponse(player);
    }

    @CacheEvict(value = RedisCacheConfig.CACHE_PLAYER, key = "#playerTag")
    @Transactional
    public PlayerResponse refreshPlayer(String playerTag) {
        Player player = refreshFromApi(playerTag);
        return playerMapper.toResponse(player);
    }

    @Transactional(readOnly = true)
    public List<PlayerResponse> getPlayersByClan(String clanTag) {
        return playerRepository.findByClanTag(clanTag).stream()
                .map(playerMapper::toResponse)
                .toList();
    }

    @Cacheable(value = RedisCacheConfig.CACHE_PLAYER_CWL_HISTORY, key = "#playerTag")
    @Transactional(readOnly = true)
    public PlayerCwlHistoryResponse getPlayerCwlHistory(String playerTag) {
        Player player = playerRepository.findById(playerTag)
                .orElseThrow(() -> ResourceNotFoundException.player(playerTag));

        List<CwlParticipant> participations =
                cwlParticipantRepository.findByPlayerTagAndTotalScoreIsNotNullOrderBySeasonDesc(playerTag);

        List<PlayerCwlHistoryResponse.SeasonEntry> seasons = participations.stream()
                .map(p -> new PlayerCwlHistoryResponse.SeasonEntry(
                        p.getClanTag(),
                        p.getSeason(),
                        p.getTotalScore() != null ? p.getTotalScore() : 0,
                        p.getAttacksMade() != null ? p.getAttacksMade() : 0,
                        p.getAttacksMissed() != null ? p.getAttacksMissed() : 0,
                        p.getAverageStars() != null ? p.getAverageStars() : BigDecimal.ZERO,
                        p.getAverageDestruction() != null ? p.getAverageDestruction() : BigDecimal.ZERO,
                        p.getTownHallLevel() != null ? p.getTownHallLevel() : 0
                ))
                .toList();

        return new PlayerCwlHistoryResponse(playerTag, player.getName(), seasons);
    }

    @Cacheable(value = RedisCacheConfig.CACHE_PLAYER_STATS, key = "#playerTag")
    @Transactional(readOnly = true)
    public PlayerStatsResponse getPlayerStats(String playerTag) {
        Player player = playerRepository.findById(playerTag)
                .orElseThrow(() -> ResourceNotFoundException.player(playerTag));

        List<CwlParticipant> participations =
                cwlParticipantRepository.findByPlayerTagAndTotalScoreIsNotNullOrderBySeasonDesc(playerTag);

        int totalScore = 0;
        int totalAttacks = 0;
        int totalMissed = 0;
        double totalStarsSum = 0;
        double totalDestructionSum = 0;
        int seasonsWithAttacks = 0;

        for (CwlParticipant p : participations) {
            totalScore += p.getTotalScore() != null ? p.getTotalScore() : 0;
            int made = p.getAttacksMade() != null ? p.getAttacksMade() : 0;
            int missed = p.getAttacksMissed() != null ? p.getAttacksMissed() : 0;
            totalAttacks += made;
            totalMissed += missed;

            int seasonTotal = made + missed;
            if (seasonTotal > 0) {
                seasonsWithAttacks++;
                totalStarsSum += p.getAverageStars() != null
                        ? p.getAverageStars().doubleValue() * seasonTotal : 0;
                totalDestructionSum += p.getAverageDestruction() != null
                        ? p.getAverageDestruction().doubleValue() * seasonTotal : 0;
            }
        }

        int allAttacks = totalAttacks + totalMissed;
        BigDecimal careerAvgStars = allAttacks > 0
                ? BigDecimal.valueOf(totalStarsSum / allAttacks).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        BigDecimal careerAvgDestruction = allAttacks > 0
                ? BigDecimal.valueOf(totalDestructionSum / allAttacks).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return new PlayerStatsResponse(
                playerTag,
                player.getName(),
                player.getTownHallLevel(),
                totalScore,
                totalAttacks,
                totalMissed,
                careerAvgStars,
                careerAvgDestruction,
                participations.size()
        );
    }

    @CacheEvict(value = RedisCacheConfig.CACHE_PLAYER, key = "#playerTag")
    @Transactional
    public Player syncPlayerFromApi(String playerTag) {
        CocPlayerResponse apiResponse = cocApiClient.getPlayer(playerTag);
        return syncPlayer(apiResponse);
    }

    private Player refreshFromApi(String tag) {
        log.debug("Refreshing player {} from CoC API", tag);
        CocPlayerResponse apiResponse = cocApiClient.getPlayer(tag);
        return syncPlayer(apiResponse);
    }

    private Player syncPlayer(CocPlayerResponse apiResponse) {
        PlayerUpdateData updateData = new PlayerUpdateData(
                apiResponse.name(),
                (short)(int) apiResponse.townHallLevel(),
                apiResponse.clan() != null ? apiResponse.clan().tag() : null,
                apiResponse.role(),
                apiResponse.warStars(),
                apiResponse.donations(),
                apiResponse.donationsReceived(),
                apiResponse.expLevel(),
                apiResponse.trophies()
        );

        Player player = playerRepository.findById(apiResponse.tag())
                .orElseGet(() -> Player.builder()
                        .tag(apiResponse.tag())
                        .name(updateData.name())
                        .townHallLevel(updateData.townHallLevel())
                        .clanTag(updateData.clanTag())
                        .clanRole(updateData.clanRole())
                        .warStars(updateData.warStars())
                        .donations(updateData.donations())
                        .donationsReceived(updateData.donationsReceived())
                        .expLevel(updateData.expLevel())
                        .trophies(updateData.trophies())
                        .build());

        player.updateFrom(updateData);

        syncHeroes(player, apiResponse);
        syncTroops(player, apiResponse);
        syncSpells(player, apiResponse);
        syncPets(player, apiResponse);
        syncEquipment(player, apiResponse);

        return playerRepository.save(player);
    }

    private void syncHeroes(Player player, CocPlayerResponse apiResponse) {
        player.getHeroes().clear();
        apiResponse.homeHeroes().forEach(h ->
                player.addHero(new PlayerHero(
                        player.getTag(),
                        h.name(),
                        (short)(int) h.level(),
                        (short)(int) h.maxLevel()
                ))
        );
    }

    private void syncTroops(Player player, CocPlayerResponse apiResponse) {
        player.getTroops().clear();
        apiResponse.homeTroops().forEach(t ->
                player.addTroop(new PlayerTroop(
                        player.getTag(),
                        t.name(),
                        (short)(int) t.level(),
                        (short)(int) t.maxLevel()
                ))
        );
    }

    private void syncPets(Player player, CocPlayerResponse apiResponse) {
        player.getPets().clear();
        apiResponse.pets().forEach(p ->
                player.addPet(new PlayerPet(
                        player.getTag(),
                        p.name(),
                        (short)(int) p.level(),
                        (short)(int) p.maxLevel()
                ))
        );
    }

    private void syncSpells(Player player, CocPlayerResponse apiResponse) {
        player.getSpells().clear();
        apiResponse.homeSpells().forEach(s ->
                player.addSpell(new PlayerSpell(
                        player.getTag(),
                        s.name(),
                        (short)(int) s.level(),
                        (short)(int) s.maxLevel()
                ))
        );
    }

    private void syncEquipment(Player player, CocPlayerResponse apiResponse) {
        player.getEquipment().clear();
        // Use nested hero equipment (what's actively equipped) per the docs
        apiResponse.homeHeroes().forEach(hero -> {
            if (hero.equipment() != null) {
                hero.equipment().forEach(eq ->
                        player.addEquipment(new PlayerEquipment(
                                eq.name(),
                                hero.name(),
                                (short)(int) eq.level(),
                                (short)(int) eq.maxLevel()
                        ))
                );
            }
        });
    }
}
