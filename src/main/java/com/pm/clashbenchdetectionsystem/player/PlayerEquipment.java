package com.pm.clashbenchdetectionsystem.player;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.proxy.HibernateProxy;

import java.time.Instant;
import java.util.Objects;

@Entity
@IdClass(PlayerHeroId.class)
@Getter
@NoArgsConstructor
@Table(name = "player_equipment")
public class PlayerEquipment {


    @Id
    @Column(name = "player_tag", length = 15, nullable = false)
    private String playerTag;

    @Id
    @Column(name = "name", length = 50, nullable = false)
    private String name;


    @Column(name = "hero_name", length = 30, nullable = false)
    private String heroName;

    @Column(name = "level", nullable = false)
    private short level;

    @Column(name = "max_level", nullable = false)
    private short maxLevel;


    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_tag", insertable = false, updatable = false)
    private Player player;


    public PlayerEquipment(String name, String heroName, short level, short maxLevel) {
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.heroName = Objects.requireNonNull(heroName, "heroName must not be null");
        this.level = level;
        this.maxLevel = maxLevel;
    }


    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }


    void setPlayer(Player player) {
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

        PlayerEquipment that = (PlayerEquipment) o;
        return playerTag != null && name != null
                && playerTag.equals(that.playerTag)
                && name.equals(that.name);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(playerTag, name);
    }


    @Override
    public String toString() {
        return "PlayerEquipment[playerTag=" + playerTag + ", name=" + name + ", heroName=" + heroName + ", level=" + level + "]";
    }
}
