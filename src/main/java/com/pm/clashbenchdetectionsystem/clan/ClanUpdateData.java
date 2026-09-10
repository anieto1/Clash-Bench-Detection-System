package com.pm.clashbenchdetectionsystem.clan;

public record ClanUpdateData(
        String name,
        Integer clanLevel,
        Integer clanPoints,
        Integer warWins,
        Integer warTies,
        Integer warLosses,
        String description,
        String badgeUrl
) {
}
