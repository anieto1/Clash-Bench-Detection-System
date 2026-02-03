package com.pm.clashbenchdetectionsystem.cocAPI.dto;


import lombok.Getter;

import java.util.List;
public record CocPlayerResponse(
        String tag,
        String name,
        int townHallLevel,
        int expLevel,
        int trophies,
        int warStars,
        int donations,
        int donationsReceived,
        CocClan clan,
        List<CocHero> heroes,
        List<CocTroop> troops,
        List<CocSpell> spells,
        List<CocPet> pets,
        List<CocHeroEquipment> heroEquipment
) {


    public record CocClan(
            String tag,
            String name,
            String role
    ) {}

    public record CocHero(
            String heroName,
            int level,
            int maxLevel,
            String village
    ) {}

    public record CocTroop(
            String name,
            int level,
            int maxLevel,
            String village
    ) {}

    public record CocSpell(
            String name,
            int level,
            int maxLevel,
            String village
    ) {}

    public record CocPet(
            String name,
            int level,
            int maxLevel
    ) {}

    public record CocHeroEquipment(
            String name,
            int level,
            int maxLevel,
            String hero
    ) {}
}
