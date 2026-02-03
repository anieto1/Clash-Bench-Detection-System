package com.pm.clashbenchdetectionsystem.player;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.proxy.HibernateProxy;

import java.time.Instant;
import java.util.Objects;


@Entity
@Table(name = "player_pets")
@IdClass(PlayerHeroId.class)
@Getter
@NoArgsConstructor
public class PlayerPet {

    @Id
    @Column(name = "player_tag", length =15, nullable = false)
    private String playerTag;

    @Id
    @Column(name = "pet_name", length = 20, nullable = false)
    private String petName;

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

    public PlayerPet(String playerTag, String petName, short level, short maxLevel) {
        this.playerTag = playerTag;
        this.petName = petName;
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

        PlayerPet that = (PlayerPet) o;
        return playerTag != null && petName != null
                && playerTag.equals(that.playerTag)
                && petName.equals(that.petName);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(playerTag, petName);
    }


    @Override
    public String toString() {
        return "PlayerHero[playerTag=" + playerTag + ", name=" + petName + ", level=" + level + "]";
    }


}
