package com.pm.clashbenchdetectionsystem.player;


import com.pm.clashbenchdetectionsystem.cocAPI.CocApiClient;
import com.pm.clashbenchdetectionsystem.common.exception.*;
import com.pm.clashbenchdetectionsystem.cocAPI.dto.CocPlayerResponse;

import com.pm.clashbenchdetectionsystem.player.playerDTO.PlayerResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlayerService {

    private static final Duration STALE_THRESHOLD = Duration.ofHours(1);

    private final PlayerRepository playerRepository;
    private final PlayerMapper playerMapper;
    private final CocApiClient cocApiClient;


    @Transactional(readOnly = true)
    public PlayerResponse getPlayer(String playerTag) {
        Player player = playerRepository.findById(playerTag).orElseThrow(()->ResourceNotFoundException.player(playerTag));

        if(isStale(player)){
            player = refreshFromApi(playerTag);
        }

        return playerMapper.toResponse(player);
    }

    @Transactional
    public PlayerResponse refreshPlayer(String playerTag) {
        Player player = refreshFromApi(playerTag);
        return playerMapper.toResponse(player);
    }

    @Transactional(readOnly = true)
    public List<PlayerResponse> getPlayersByClan(String clanTag){
        return playerRepository.findByClanTag(clanTag).stream()
                .map(playerMapper::toResponse)
                .toList();
    }

    public void syncClanMembers(String clanTag, List<CocPlayerResponse> apiResponse){
        for(CocPlayerResponse player : apiResponse){
            syncPlayer(player);
        }
        log.info("Synced {} players from clan {}", apiResponse.size(), clanTag);
    }


    private boolean isStale(Player player){
        return player.getUpdatedAt()
                .plus(STALE_THRESHOLD)
                .isBefore(Instant.now());
    }

    private Player refreshFromApi(String tag){
        log.debug("Refreshing player {} from CoC API", tag);
        CocPlayerResponse apiResponse = cocApiClient.getPlayer(tag);
        return syncPlayer(apiResponse);
    }

    private Player syncPlayer(CocPlayerResponse apiResponse) {
        PlayerUpdateData updateData = new PlayerUpdateData(
                apiResponse.name(),
                (short) apiResponse.townHallLevel(),
                apiResponse.clan() != null ? apiResponse.clan().tag() : null,
                apiResponse.clan() != null ? apiResponse.clan().role() : null,
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
        apiResponse.heroes().forEach(h ->
                player.addHero(new PlayerHero(
                        player.getTag(),
                        h.heroName(),
                        (short) h.level(),
                        (short) h.maxLevel()
                ))
        );
    }
    private void syncTroops(Player player, CocPlayerResponse apiResponse) {
        player.getTroops().clear();
        apiResponse.troops().forEach(t ->
                player.addTroop(new PlayerTroop(
                        player.getTag(),
                        t.name(),
                        (short) t.level(),
                        (short) t.maxLevel()
                ))
        );
    }

    private void syncPets(Player player, CocPlayerResponse apiResponse){
        player.getPets().clear();
        apiResponse.pets().forEach(p ->
                player.addPet(new PlayerPet(
                        player.getTag(),
                        p.name(),
                        (short) p.level(),
                        (short) p.maxLevel()
                ))
        );
    }

    private void syncSpells(Player player, CocPlayerResponse apiResponse){
        player.getSpells().clear();
        apiResponse.spells().forEach(s ->
                player.addSpell(new PlayerSpell(
                        player.getTag(),
                        s.name(),
                        (short) s.level(),
                        (short) s.maxLevel()
                ))
        );
    }

    private void syncEquipment(Player player, CocPlayerResponse apiResponse){
        player.getEquipment().clear();
        apiResponse.heroEquipment().forEach(e ->
                player.addEquipment(new PlayerEquipment(
                        player.getTag(),
                        e.name(),
                        (short) e.level(),
                        (short) e.maxLevel()
                ))
        );
    }
}
