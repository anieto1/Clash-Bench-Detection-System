package com.pm.clashbenchdetectionsystem.player.playerDTO;


import java.time.Instant;
import java.util.Set;

public record PlayerResponse(
        String tag,
        String name,
        short townHallLevel,
        String clanTag,
        String clanRole,
        int warStars,
        int donations,
        int donationsReceived,
        int expLevel,
        int trophies,
        Set<HeroDto> heroes,
        Set<TroopDto> troops,
        Set<SpellDto> spells,
        Set<PetDto> pets,
        Set<EquipmentDto> equipment,
        Instant updatedAt
) {
    public record HeroDto(String name, short level, short maxLevel) {}
    public record TroopDto(String name, short level, short maxLevel) {}
    public record SpellDto(String name, short level, short maxLevel) {}
    public record PetDto(String name, short level, short maxLevel) {}
    public record EquipmentDto(String name, String heroName, short level, short maxLevel) {}
}
