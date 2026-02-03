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
@Table(name = "player_spell")
public class PlayerSpell {

    @Id
    @Column(name = "player_tag", length =15, nullable = false)
    private String playerTag;

    @Id
    @Column(name = "spell_name", length = 20, nullable = false)
    private String spellName;

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

    public PlayerSpell(String playerTag, String spellName, short level, short maxLevel) {
        this.playerTag = playerTag;
        this.spellName = spellName;
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

        PlayerSpell that = (PlayerSpell) o;
        return playerTag != null && spellName != null
                && playerTag.equals(that.playerTag)
                && spellName.equals(that.spellName);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(playerTag, spellName);
    }


    @Override
    public String toString() {
        return "PlayerHero[playerTag=" + playerTag + ", name=" + spellName + ", level=" + level + "]";
    }
}
