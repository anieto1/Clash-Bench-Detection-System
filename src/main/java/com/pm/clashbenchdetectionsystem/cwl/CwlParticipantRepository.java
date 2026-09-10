package com.pm.clashbenchdetectionsystem.cwl;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CwlParticipantRepository extends JpaRepository<CwlParticipant, CwlParticipantId> {

    List<CwlParticipant> findByClanTagAndSeason(String clanTag, String season);

    /**
     * Find all CWL participations for a player across all seasons (with scores computed).
     */
    List<CwlParticipant> findByPlayerTagAndTotalScoreIsNotNullOrderBySeasonDesc(String playerTag);

    /**
     * Find all CWL participations for a player, including those without scores.
     */
    List<CwlParticipant> findByPlayerTagOrderBySeasonDesc(String playerTag);

    /**
     * Find all participants who have computed scores, for overall leaderboard aggregation.
     */
    @Query("SELECT p FROM CwlParticipant p WHERE p.totalScore IS NOT NULL ORDER BY p.playerTag, p.season DESC")
    List<CwlParticipant> findAllWithScores();
}
