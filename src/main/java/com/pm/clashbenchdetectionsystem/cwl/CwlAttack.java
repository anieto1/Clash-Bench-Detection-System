package com.pm.clashbenchdetectionsystem.cwl;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.proxy.HibernateProxy;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "cwl_attack")
@IdClass(CwlAttackId.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CwlAttack {

    @Id
    @Column(name = "war_tag", length = 50, nullable = false)
    private String warTag;

    @Id
    @Column(name = "attacker_tag", length = 15, nullable = false)
    private String attackerTag;

    @Column(name = "attacker_map_position", nullable = false)
    private short attackerMapPosition;

    @Column(name = "defender_tag", length = 15, nullable = false)
    private String defenderTag;

    @Column(name = "defender_name", length = 50)
    private String defenderName;

    @Column(name = "defender_th_level", nullable = false)
    private short defenderThLevel;

    @Column(name = "defender_map_position", nullable = false)
    private short defenderMapPosition;

    @Column(name = "stars", nullable = false)
    private short stars;

    @Column(name = "destruction_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal destructionPercentage;

    @Column(name = "attack_order")
    private Short attackOrder;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /* ==================== Relationships ==================== */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "war_tag", insertable = false, updatable = false)
    private CwlWar cwlWar;

    /* ==================== Constructor ==================== */

    public CwlAttack(String warTag, String attackerTag, short attackerMapPosition,
                     String defenderTag, String defenderName, short defenderThLevel,
                     short defenderMapPosition, short stars, BigDecimal destructionPercentage,
                     Short attackOrder) {
        this.warTag = Objects.requireNonNull(warTag, "warTag must not be null");
        this.attackerTag = Objects.requireNonNull(attackerTag, "attackerTag must not be null");
        this.attackerMapPosition = attackerMapPosition;
        this.defenderTag = Objects.requireNonNull(defenderTag, "defenderTag must not be null");
        this.defenderName = defenderName;
        this.defenderThLevel = defenderThLevel;
        this.defenderMapPosition = defenderMapPosition;
        this.stars = stars;
        this.destructionPercentage = destructionPercentage;
        this.attackOrder = attackOrder;
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

        CwlAttack that = (CwlAttack) o;
        return warTag != null && attackerTag != null
                && warTag.equals(that.warTag)
                && attackerTag.equals(that.attackerTag);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(warTag, attackerTag);
    }

    @Override
    public String toString() {
        return "CwlAttack[warTag=" + warTag + ", attacker=" + attackerTag + ", stars=" + stars + "]";
    }
}
