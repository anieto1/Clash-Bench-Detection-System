package com.pm.clashbenchdetectionsystem.player;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.proxy.HibernateProxy;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "player_hero")
@IdClass(PlayerHeroId.class)
@Getter
@NoArgsConstructor
public class PlayerHero {

    @Id
    @Column(name = "player_tag", length =15, nullable = false)
    private String playerTag;

    @Id
    @Column(name = "hero_name", length = 20, nullable = false)
    private String heroName;


    @Column(name = "level", nullable = false)
    private short level;


    @Column(name = "max_level", nullable = false)
    private short maxLevel;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_tag", insertable = false, updatable = false)
    @Setter(AccessLevel.PACKAGE)
    private Player player;

    public PlayerHero(String playerTag, String heroName, short level, short maxLevel) {
        this.playerTag = playerTag;
        this.heroName = heroName;
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

        PlayerHero that = (PlayerHero) o;
        return playerTag != null && heroName != null
                && playerTag.equals(that.playerTag)
                && heroName.equals(that.heroName);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(playerTag, heroName);
    }

    @Override
    public String toString() {
        return "PlayerHero[playerTag=" + playerTag + ", name=" + heroName + ", level=" + level + "]";
    }
}
