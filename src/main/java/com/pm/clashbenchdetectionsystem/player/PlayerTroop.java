package com.pm.clashbenchdetectionsystem.player;


import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.proxy.HibernateProxy;

import java.time.Instant;
import java.util.Objects;

@Entity
@IdClass(PlayerHeroId.class)
@Getter
@NoArgsConstructor
@Table(name = "player_troop")
public class PlayerTroop {

    @Id
    private String playerTag;

    @Id
    private String troopName;

    @Min(0)
    @Column(name = "level", nullable = false)
    private short level;

    @Min(0)
    @Column(name = "max_level", nullable = false)
    private short maxLevel;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_tag", insertable = false, updatable = false)
    private Player player;

    public PlayerTroop(String playerTag, String troopName, short level, short maxLevel) {
        this.playerTag = playerTag;
        this.troopName = troopName;
        this.level = level;
        this.maxLevel = maxLevel;
    }

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public void setPlayer(Player player) {
        this.player = player;
        this.playerTag = (player != null) ? player.getTag() : null;
    }

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

        PlayerTroop that = (PlayerTroop) o;
        return playerTag != null && troopName != null
                && playerTag.equals(that.playerTag)
                && troopName.equals(that.troopName);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(playerTag, troopName);
    }

    @Override
    public String toString() {
        return "PlayerHero[playerTag=" + playerTag + ", name=" + troopName + ", level=" + level + "]";
    }
}
