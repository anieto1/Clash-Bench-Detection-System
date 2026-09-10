package com.pm.clashbenchdetectionsystem.cocAPI.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CocCwlWarResponse(
        String state,
        Integer teamSize,
        Integer attacksPerMember,
        String preparationStartTime,
        String startTime,
        String endTime,
        WarClan clan,
        WarClan opponent
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record WarClan(
            String tag,
            String name,
            Integer clanLevel,
            Integer attacks,
            Integer stars,
            Double destructionPercentage,
            BadgeUrls badgeUrls,
            List<WarMember> members
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record WarMember(
            String tag,
            String name,
            Integer townhallLevel,
            Integer mapPosition,
            List<WarAttack> attacks,
            Integer opponentAttacks
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record WarAttack(
            String attackerTag,
            String defenderTag,
            Integer stars,
            Integer destructionPercentage,
            Integer order,
            Integer duration
    ) {}
}
