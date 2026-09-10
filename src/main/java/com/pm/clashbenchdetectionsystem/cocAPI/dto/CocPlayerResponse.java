package com.pm.clashbenchdetectionsystem.cocAPI.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CocPlayerResponse(
        String tag,
        String name,
        Integer townHallLevel,
        Integer expLevel,
        Integer trophies,
        Integer warStars,
        Integer donations,
        Integer donationsReceived,
        String role,
        CocClan clan,
        List<CocHero> heroes,
        List<CocTroop> troops,
        List<CocSpell> spells,
        List<CocHeroEquipment> heroEquipment
) {

    private static final Set<String> PET_NAMES = Set.of(
            "L.A.S.S.I", "Mighty Yak", "Electro Owl", "Unicorn",
            "Phoenix", "Poison Lizard", "Diggy", "Frosty",
            "Spirit Fox", "Angry Jelly", "Sneezy"
    );

    public List<CocHero> homeHeroes() {
        if (heroes == null) return List.of();
        return heroes.stream()
                .filter(h -> "home".equals(h.village()))
                .toList();
    }

    public List<CocTroop> homeTroops() {
        if (troops == null) return List.of();
        return troops.stream()
                .filter(t -> "home".equals(t.village()))
                .filter(t -> !PET_NAMES.contains(t.name()))
                .toList();
    }

    public List<CocTroop> pets() {
        if (troops == null) return List.of();
        return troops.stream()
                .filter(t -> "home".equals(t.village()))
                .filter(t -> PET_NAMES.contains(t.name()))
                .toList();
    }

    public List<CocSpell> homeSpells() {
        if (spells == null) return List.of();
        return spells.stream()
                .filter(s -> "home".equals(s.village()))
                .toList();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CocClan(
            String tag,
            String name,
            Integer clanLevel,
            BadgeUrls badgeUrls
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CocHero(
            String name,
            Integer level,
            Integer maxLevel,
            String village,
            List<CocEquipmentSlot> equipment
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CocEquipmentSlot(
            String name,
            Integer level,
            Integer maxLevel
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CocTroop(
            String name,
            Integer level,
            Integer maxLevel,
            String village,
            @JsonProperty("superTroopIsActive") Boolean superTroopIsActive
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CocSpell(
            String name,
            Integer level,
            Integer maxLevel,
            String village
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CocHeroEquipment(
            String name,
            Integer level,
            Integer maxLevel,
            String village
    ) {}
}
