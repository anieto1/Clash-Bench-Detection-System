package com.pm.clashbenchdetectionsystem.cwl.cwlDTO;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record CwlWarResponse(
        String warTag,
        int dayNumber,
        String opponentClanTag,
        String opponentClanName,
        Integer opponentClanLevel,
        int ourStars,
        BigDecimal ourDestruction,
        int opponentStars,
        BigDecimal opponentDestruction,
        String result,
        Instant startTime,
        Instant endTime,
        List<WarMemberDto> members,
        List<AttackDto> attacks
) {
    public record WarMemberDto(
            String playerTag,
            int mapPosition,
            int townHallLevel,
            boolean attacked
    ) {}

    public record AttackDto(
            String attackerTag,
            int attackerMapPosition,
            String defenderTag,
            int defenderMapPosition,
            int defenderThLevel,
            int stars,
            BigDecimal destructionPercentage,
            Short attackOrder
    ) {}
}
