package com.pm.clashbenchdetectionsystem.scoring;

import com.pm.clashbenchdetectionsystem.common.exception.ResourceNotFoundException;
import com.pm.clashbenchdetectionsystem.cwl.*;
import com.pm.clashbenchdetectionsystem.cwl.StatsSnapshotHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class CbdsScoreService {

    private final CwlSeasonRepository cwlSeasonRepository;
    private final CwlWarRepository cwlWarRepository;
    private final CwlWarMemberRepository cwlWarMemberRepository;
    private final CwlAttackRepository cwlAttackRepository;
    private final CwlParticipantRepository cwlParticipantRepository;

    @Transactional(readOnly = true)
    public List<SeasonScore> calculateLeaderboard(String clanTag, String season) {
        CwlSeason cwlSeason = cwlSeasonRepository.findByClanTagAndSeason(clanTag, season)
                .orElseThrow(() -> ResourceNotFoundException.cwlSeason(clanTag, season));

        List<CwlParticipant> participants = cwlParticipantRepository.findByClanTagAndSeason(clanTag, season);
        List<CwlWar> wars = cwlWarRepository.findByClanTagAndSeasonOrderByDayNumber(clanTag, season);

        Map<String, List<CwlWarMember>> membersByWar = new HashMap<>();
        Map<String, Map<String, CwlAttack>> attacksByWarAndPlayer = new HashMap<>();

        for (CwlWar war : wars) {
            List<CwlWarMember> members = cwlWarMemberRepository.findByWarTag(war.getWarTag());
            membersByWar.put(war.getWarTag(), members);

            List<CwlAttack> attacks = cwlAttackRepository.findByWarTag(war.getWarTag());
            Map<String, CwlAttack> attackMap = new HashMap<>();
            for (CwlAttack attack : attacks) {
                attackMap.put(attack.getAttackerTag(), attack);
            }
            attacksByWarAndPlayer.put(war.getWarTag(), attackMap);
        }

        List<SeasonScore> scores = new ArrayList<>();

        for (CwlParticipant participant : participants) {
            String playerName = extractPlayerName(participant);
            int seasonTh = extractTownHallLevel(participant.getStatsSnapshot());

            List<ScoredAttack> scoredAttacks = new ArrayList<>();
            int attacksMade = 0;
            int attacksMissed = 0;
            int totalStars = 0;
            double totalDestruction = 0;
            int latestTh = seasonTh; // track the most recent TH seen across war days

            for (CwlWar war : wars) {
                List<CwlWarMember> warMembers = membersByWar.getOrDefault(war.getWarTag(), List.of());

                Optional<CwlWarMember> memberOpt = warMembers.stream()
                        .filter(m -> m.getPlayerTag().equals(participant.getPlayerTag()))
                        .findFirst();

                if (memberOpt.isEmpty()) {
                    continue;
                }

                CwlWarMember member = memberOpt.get();
                int attackerTh = member.getTownHallLevel();
                if (attackerTh > 0) {
                    latestTh = attackerTh;
                }

                if (!member.isAttacked()) {
                    scoredAttacks.add(ScoredAttack.ABSENT);
                    attacksMissed++;
                    continue;
                }

                Map<String, CwlAttack> warAttacks = attacksByWarAndPlayer.getOrDefault(war.getWarTag(), Map.of());
                CwlAttack attack = warAttacks.get(participant.getPlayerTag());

                if (attack == null) {
                    log.warn("War member {} in war {} marked as attacked but no attack record found",
                            participant.getPlayerTag(), war.getWarTag());
                    scoredAttacks.add(ScoredAttack.ABSENT);
                    attacksMissed++;
                    continue;
                }

                int stars = attack.getStars();
                double destructionPct = attack.getDestructionPercentage().doubleValue();
                int attackerMapPos = member.getMapPosition();
                int defenderMapPos = attack.getDefenderMapPosition();
                int defenderTh = attack.getDefenderThLevel();

                AttackScore score = CbdsCalculator.calculateAttack(
                        stars, destructionPct, attackerMapPos, defenderMapPos, attackerTh, defenderTh
                );

                scoredAttacks.add(new ScoredAttack(
                        score, attackerMapPos, defenderMapPos, attackerTh, defenderTh, stars, destructionPct
                ));
                attacksMade++;
                totalStars += stars;
                totalDestruction += destructionPct;
            }

            int totalAttacks = attacksMade + attacksMissed;
            int totalScore = scoredAttacks.stream().mapToInt(sa -> sa.score().totalScore()).sum();
            double avgStars = totalAttacks > 0 ? (double) totalStars / totalAttacks : 0;
            double avgDestruction = totalAttacks > 0 ? totalDestruction / totalAttacks : 0;

            scores.add(new SeasonScore(
                    participant.getPlayerTag(),
                    playerName,
                    latestTh,
                    totalScore,
                    attacksMade,
                    attacksMissed,
                    avgStars,
                    avgDestruction,
                    scoredAttacks
            ));
        }

        // Sort by total score descending
        scores.sort(Comparator.comparingInt(SeasonScore::totalScore).reversed());

        return scores;
    }

    /**
     * Build the all-time overall leaderboard by aggregating stored scores across all seasons.
     */
    @Transactional(readOnly = true)
    public OverallLeaderboardResponse getOverallLeaderboard() {
        List<CwlParticipant> allScored = cwlParticipantRepository.findAllWithScores();

        // Group by player tag and aggregate
        Map<String, List<CwlParticipant>> byPlayer = new LinkedHashMap<>();
        for (CwlParticipant p : allScored) {
            byPlayer.computeIfAbsent(p.getPlayerTag(), k -> new ArrayList<>()).add(p);
        }

        List<OverallLeaderboardResponse.OverallEntry> entries = new ArrayList<>();

        for (var entry : byPlayer.entrySet()) {
            String playerTag = entry.getKey();
            List<CwlParticipant> seasons = entry.getValue();

            int totalScore = 0;
            int totalAttacks = 0;
            int totalMissed = 0;
            double weightedStars = 0;
            double weightedDestruction = 0;
            String playerName = playerTag;
            short latestTh = 0;

            for (CwlParticipant p : seasons) {
                totalScore += p.getTotalScore() != null ? p.getTotalScore() : 0;
                int made = p.getAttacksMade() != null ? p.getAttacksMade() : 0;
                int missed = p.getAttacksMissed() != null ? p.getAttacksMissed() : 0;
                totalAttacks += made;
                totalMissed += missed;

                int seasonTotal = made + missed;
                if (seasonTotal > 0) {
                    weightedStars += p.getAverageStars() != null
                            ? p.getAverageStars().doubleValue() * seasonTotal : 0;
                    weightedDestruction += p.getAverageDestruction() != null
                            ? p.getAverageDestruction().doubleValue() * seasonTotal : 0;
                }

                // Use latest TH and try to get player name
                if (p.getTownHallLevel() != null && p.getTownHallLevel() > latestTh) {
                    latestTh = p.getTownHallLevel();
                }
                String name = StatsSnapshotHelper.extractName(p.getStatsSnapshot());
                if (name != null) {
                    playerName = name;
                }
            }

            int allTotal = totalAttacks + totalMissed;
            BigDecimal careerAvgStars = allTotal > 0
                    ? BigDecimal.valueOf(weightedStars / allTotal).setScale(2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            BigDecimal careerAvgDestruction = allTotal > 0
                    ? BigDecimal.valueOf(weightedDestruction / allTotal).setScale(2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            entries.add(new OverallLeaderboardResponse.OverallEntry(
                    0, // rank placeholder, assigned after sorting
                    playerTag,
                    playerName,
                    latestTh,
                    totalScore,
                    totalAttacks,
                    totalMissed,
                    careerAvgStars,
                    careerAvgDestruction,
                    seasons.size()
            ));
        }

        // Sort by total career score descending
        entries.sort(Comparator.comparingInt(OverallLeaderboardResponse.OverallEntry::totalCareerScore).reversed());

        // Assign ranks
        List<OverallLeaderboardResponse.OverallEntry> ranked = new ArrayList<>();
        for (int i = 0; i < entries.size(); i++) {
            OverallLeaderboardResponse.OverallEntry e = entries.get(i);
            ranked.add(new OverallLeaderboardResponse.OverallEntry(
                    i + 1,
                    e.playerTag(),
                    e.playerName(),
                    e.currentTownHallLevel(),
                    e.totalCareerScore(),
                    e.totalAttacks(),
                    e.totalMissed(),
                    e.careerAverageStars(),
                    e.careerAverageDestruction(),
                    e.seasonsParticipated()
            ));
        }

        return new OverallLeaderboardResponse(ranked);
    }

    /**
     * Calculate the leaderboard and persist the scores to cwl_participant rows.
     * Called after sync to ensure scores are stored in the database.
     */
    @Transactional
    public void calculateAndPersistScores(String clanTag, String season) {
        List<SeasonScore> scores = calculateLeaderboard(clanTag, season);
        List<CwlParticipant> participants = cwlParticipantRepository.findByClanTagAndSeason(clanTag, season);

        Map<String, CwlParticipant> participantMap = new HashMap<>();
        for (CwlParticipant p : participants) {
            participantMap.put(p.getPlayerTag(), p);
        }

        for (SeasonScore score : scores) {
            CwlParticipant participant = participantMap.get(score.playerTag());
            if (participant != null) {
                participant.updateScores(
                        score.totalScore(),
                        (short) score.attacksMade(),
                        (short) score.attacksMissed(),
                        BigDecimal.valueOf(score.averageStars()).setScale(2, RoundingMode.HALF_UP),
                        BigDecimal.valueOf(score.averageDestruction()).setScale(2, RoundingMode.HALF_UP),
                        (short) score.townHallLevel()
                );
                cwlParticipantRepository.save(participant);
            }
        }

        log.info("Persisted CBDS scores for {} participants in {}/{}", scores.size(), clanTag, season);
    }

    /* ==================== Private helpers ==================== */

    private int extractTownHallLevel(String statsSnapshot) {
        return StatsSnapshotHelper.extractTownHallLevel(statsSnapshot);
    }

    private String extractPlayerName(CwlParticipant participant) {
        if (participant.getPlayer() != null) {
            return participant.getPlayer().getName();
        }
        return participant.getPlayerTag();
    }
}
