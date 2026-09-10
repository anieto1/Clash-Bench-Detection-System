package com.pm.clashbenchdetectionsystem.clan.clanDTO;

import java.time.Instant;

public record ClanResponse(
        String tag,
        String name,
        int clanLevel,
        int clanPoints,
        int warWins,
        int warTies,
        int warLosses,
        String description,
        String badgeUrl,
        Instant updatedAt
) {
}
