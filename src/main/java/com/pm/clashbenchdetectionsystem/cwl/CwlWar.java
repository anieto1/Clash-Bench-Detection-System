package com.pm.clashbenchdetectionsystem.cwl;

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
@Table(name = "cwl_war", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"clan_tag", "season", "day_number"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CwlWar {

    @Id
    @Column(name = "war_tag", length = 50, nullable = false)
    private String warTag;

    @Column(name = "clan_tag", length = 15, nullable = false)
    private String clanTag;

    @Column(name = "season", length = 7, nullable = false)
    private String season;

    @Column(name = "day_number", nullable = false)
    private short dayNumber;

    @Column(name = "opponent_clan_tag", length = 15, nullable = false)
    private String opponentClanTag;

    @Column(name = "opponent_clan_name", length = 50, nullable = false)
    private String opponentClanName;

    @Column(name = "opponent_clan_level")
    private Integer opponentClanLevel;

    @Column(name = "our_stars", nullable = false)
    private short ourStars;

    @Column(name = "our_destruction", nullable = false, precision = 5, scale = 2)
    private BigDecimal ourDestruction = BigDecimal.ZERO;

    @Column(name = "opponent_stars", nullable = false)
    private short opponentStars;

    @Column(name = "opponent_destruction", nullable = false, precision = 5, scale = 2)
    private BigDecimal opponentDestruction = BigDecimal.ZERO;

    @Column(name = "result", length = 4)
    private String result;

    @Column(name = "start_time")
    private Instant startTime;

    @Column(name = "end_time")
    private Instant endTime;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /* ==================== Relationships ==================== */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "clan_tag", referencedColumnName = "clan_tag", insertable = false, updatable = false),
            @JoinColumn(name = "season", referencedColumnName = "season", insertable = false, updatable = false)
    })
    private CwlSeason cwlSeason;

    @OneToMany(mappedBy = "cwlWar", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CwlWarMember> warMembers = new HashSet<>();

    @OneToMany(mappedBy = "cwlWar", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CwlAttack> attacks = new HashSet<>();

    /* ==================== Constructor ==================== */

    @Builder
    private CwlWar(String warTag, String clanTag, String season, short dayNumber,
                   String opponentClanTag, String opponentClanName, Integer opponentClanLevel,
                   Instant startTime, Instant endTime) {
        this.warTag = Objects.requireNonNull(warTag, "warTag must not be null");
        this.clanTag = Objects.requireNonNull(clanTag, "clanTag must not be null");
        this.season = Objects.requireNonNull(season, "season must not be null");
        this.dayNumber = dayNumber;
        this.opponentClanTag = Objects.requireNonNull(opponentClanTag, "opponentClanTag must not be null");
        this.opponentClanName = Objects.requireNonNull(opponentClanName, "opponentClanName must not be null");
        this.opponentClanLevel = opponentClanLevel;
        this.ourStars = 0;
        this.ourDestruction = BigDecimal.ZERO;
        this.opponentStars = 0;
        this.opponentDestruction = BigDecimal.ZERO;
        this.startTime = startTime;
        this.endTime = endTime;
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

    public void updateResult(String result) {
        this.result = result;
    }

    public void updateScores(short ourStars, BigDecimal ourDestruction,
                             short opponentStars, BigDecimal opponentDestruction) {
        this.ourStars = ourStars;
        this.ourDestruction = ourDestruction;
        this.opponentStars = opponentStars;
        this.opponentDestruction = opponentDestruction;
    }

    public void addWarMember(CwlWarMember member) {
        warMembers.add(member);
    }

    public void addAttack(CwlAttack attack) {
        attacks.add(attack);
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

        CwlWar that = (CwlWar) o;
        return warTag != null && warTag.equals(that.warTag);
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy
                ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode()
                : getClass().hashCode();
    }

    @Override
    public String toString() {
        return "CwlWar[warTag=" + warTag + ", day=" + dayNumber + ", vs=" + opponentClanName + "]";
    }
}
