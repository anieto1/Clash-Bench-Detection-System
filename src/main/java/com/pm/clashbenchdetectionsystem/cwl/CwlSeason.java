package com.pm.clashbenchdetectionsystem.cwl;

import com.pm.clashbenchdetectionsystem.clan.Clan;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.proxy.HibernateProxy;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "cwl_season")
@IdClass(CwlSeasonId.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CwlSeason {

    @Id
    @Column(name = "clan_tag", length = 15, nullable = false)
    private String clanTag;

    @Id
    @Column(name = "season", length = 7, nullable = false)
    private String season;

    @Column(name = "league_name", length = 50)
    private String leagueName;

    @Column(name = "final_placement")
    private Short finalPlacement;

    @Column(name = "total_stars", nullable = false)
    private int totalStars;

    @Column(name = "total_destruction", nullable = false, precision = 5, scale = 2)
    private BigDecimal totalDestruction = BigDecimal.ZERO;

    @Column(name = "is_completed", nullable = false)
    private boolean completed;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /* ==================== Relationships ==================== */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clan_tag", insertable = false, updatable = false)
    private Clan clan;

    @OneToMany(mappedBy = "cwlSeason", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CwlParticipant> participants = new HashSet<>();

    @OneToMany(mappedBy = "cwlSeason", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CwlWar> wars = new HashSet<>();

    /* ==================== Constructor ==================== */

    @Builder
    private CwlSeason(String clanTag, String season, String leagueName) {
        this.clanTag = Objects.requireNonNull(clanTag, "clanTag must not be null");
        this.season = Objects.requireNonNull(season, "season must not be null");
        this.leagueName = leagueName;
        this.totalStars = 0;
        this.totalDestruction = BigDecimal.ZERO;
        this.completed = false;
    }

    /* ==================== Lifecycle ==================== */

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    /* ==================== Domain Methods ==================== */

    public void markCompleted(short placement) {
        this.finalPlacement = placement;
        this.completed = true;
    }

    public void updateTotals(int totalStars, BigDecimal totalDestruction) {
        this.totalStars = totalStars;
        this.totalDestruction = totalDestruction;
    }

    public void addParticipant(CwlParticipant participant) {
        participants.add(participant);
    }

    public void addWar(CwlWar war) {
        wars.add(war);
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

        CwlSeason that = (CwlSeason) o;
        return clanTag != null && season != null
                && clanTag.equals(that.clanTag)
                && season.equals(that.season);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(clanTag, season);
    }

    @Override
    public String toString() {
        return "CwlSeason[clanTag=" + clanTag + ", season=" + season + ", completed=" + completed + "]";
    }
}
