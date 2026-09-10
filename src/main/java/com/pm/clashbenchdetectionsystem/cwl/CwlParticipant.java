package com.pm.clashbenchdetectionsystem.cwl;

import com.pm.clashbenchdetectionsystem.player.Player;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "cwl_participant")
@IdClass(CwlParticipantId.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CwlParticipant {

    @Id
    @Column(name = "clan_tag", length = 15, nullable = false)
    private String clanTag;

    @Id
    @Column(name = "season", length = 7, nullable = false)
    private String season;

    @Id
    @Column(name = "player_tag", length = 15, nullable = false)
    private String playerTag;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "stats_snapshot", nullable = false, columnDefinition = "jsonb")
    private String statsSnapshot;

    /* ==================== CBDS Score Fields ==================== */

    @Column(name = "total_score")
    private Integer totalScore;

    @Column(name = "attacks_made")
    private Short attacksMade;

    @Column(name = "attacks_missed")
    private Short attacksMissed;

    @Column(name = "average_stars", precision = 4, scale = 2)
    private BigDecimal averageStars;

    @Column(name = "average_destruction", precision = 5, scale = 2)
    private BigDecimal averageDestruction;

    @Column(name = "town_hall_level")
    private Short townHallLevel;

    @Column(name = "scores_computed_at")
    private Instant scoresComputedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /* ==================== Relationships ==================== */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "clan_tag", referencedColumnName = "clan_tag", insertable = false, updatable = false),
            @JoinColumn(name = "season", referencedColumnName = "season", insertable = false, updatable = false)
    })
    private CwlSeason cwlSeason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_tag", insertable = false, updatable = false)
    private Player player;

    /* ==================== Constructor ==================== */

    public CwlParticipant(String clanTag, String season, String playerTag, String statsSnapshot) {
        this.clanTag = Objects.requireNonNull(clanTag, "clanTag must not be null");
        this.season = Objects.requireNonNull(season, "season must not be null");
        this.playerTag = Objects.requireNonNull(playerTag, "playerTag must not be null");
        this.statsSnapshot = Objects.requireNonNull(statsSnapshot, "statsSnapshot must not be null");
    }

    /* ==================== Domain Methods ==================== */

    public void updateScores(int totalScore, short attacksMade, short attacksMissed,
                             BigDecimal averageStars, BigDecimal averageDestruction, short townHallLevel) {
        this.totalScore = totalScore;
        this.attacksMade = attacksMade;
        this.attacksMissed = attacksMissed;
        this.averageStars = averageStars;
        this.averageDestruction = averageDestruction;
        this.townHallLevel = townHallLevel;
        this.scoresComputedAt = Instant.now();
    }

    public boolean hasScores() {
        return this.totalScore != null;
    }

    /* ==================== Lifecycle ==================== */

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }

    /* ==================== equals / hashCode / toString ==================== */

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;

        Class<?> oEffectiveClass = o instanceof HibernateProxy
                ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass()
                : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy
                ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass()
                : this.getClass();

        if (thisEffectiveClass != oEffectiveClass) return false;

        CwlParticipant that = (CwlParticipant) o;
        return clanTag != null && season != null && playerTag != null
                && clanTag.equals(that.clanTag)
                && season.equals(that.season)
                && playerTag.equals(that.playerTag);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(clanTag, season, playerTag);
    }

    @Override
    public String toString() {
        return "CwlParticipant[clanTag=" + clanTag + ", season=" + season + ", playerTag=" + playerTag + "]";
    }
}
