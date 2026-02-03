package com.pm.clashbenchdetectionsystem.player;

public record PlayerUpdateData(
        String name,
        short townHallLevel,
        String clanTag,
        String clanRole,
        int warStars,
        int donations,
        int donationsReceived,
        int expLevel,
        int trophies
) {}
