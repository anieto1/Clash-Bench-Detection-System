package com.pm.clashbenchdetectionsystem.cocAPI.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CocClanResponse(
        String tag,
        String name,
        Integer clanLevel,
        Integer clanPoints,
        Integer warWins,
        Integer warTies,
        Integer warLosses,
        Integer warWinStreak,
        Integer members,
        String type,
        String description,
        String warFrequency,
        Boolean isWarLogPublic,
        Boolean isFamilyFriendly,
        Integer requiredTrophies,
        BadgeUrls badgeUrls,
        List<CocClanMember> memberList
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CocClanMember(
            String tag,
            String name,
            String role,
            Integer townHallLevel,
            Integer expLevel,
            Integer trophies,
            Integer clanRank,
            Integer donations,
            Integer donationsReceived
    ) {}
}
