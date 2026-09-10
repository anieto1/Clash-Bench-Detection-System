package com.pm.clashbenchdetectionsystem.cwl;

import com.pm.clashbenchdetectionsystem.cwl.cwlDTO.CwlSeasonResponse;
import com.pm.clashbenchdetectionsystem.cwl.cwlDTO.CwlWarResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CwlMapper {

    @Mapping(target = "participants", source = "participants")
    @Mapping(target = "wars", source = "wars")
    CwlSeasonResponse toSeasonResponse(CwlSeason season);

    @Mapping(target = "members", source = "warMembers")
    @Mapping(target = "attacks", source = "attacks")
    CwlWarResponse toWarResponse(CwlWar war);

    @Mapping(target = "ourStars", source = "ourStars")
    @Mapping(target = "opponentStars", source = "opponentStars")
    CwlSeasonResponse.WarSummaryDto toWarSummaryDto(CwlWar war);

    CwlWarResponse.WarMemberDto toWarMemberDto(CwlWarMember member);

    CwlWarResponse.AttackDto toAttackDto(CwlAttack attack);

    default CwlSeasonResponse.ParticipantDto toParticipantDto(CwlParticipant participant) {
        if (participant == null) return null;

        String snapshot = participant.getStatsSnapshot();
        String playerName = StatsSnapshotHelper.extractName(snapshot);
        if (playerName == null) {
            playerName = participant.getPlayerTag();
        }
        int townHallLevel = StatsSnapshotHelper.extractTownHallLevel(snapshot);

        return new CwlSeasonResponse.ParticipantDto(
                participant.getPlayerTag(),
                playerName,
                townHallLevel
        );
    }
}
