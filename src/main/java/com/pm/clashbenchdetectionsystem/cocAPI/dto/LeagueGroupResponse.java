package com.pm.clashbenchdetectionsystem.cocAPI.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LeagueGroupResponse(
        String state,
        String season,
        List<LeagueGroupClan> clans,
        List<LeagueRound> rounds
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record LeagueGroupClan(
            String tag,
            String name,
            Integer clanLevel,
            BadgeUrls badgeUrls,
            List<LeagueGroupMember> members
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record LeagueGroupMember(
            String tag,
            String name,
            Integer townHallLevel
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record LeagueRound(
            List<String> warTags
    ) {}
}
