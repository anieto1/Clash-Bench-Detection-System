package com.pm.clashbenchdetectionsystem.player;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.proxy.HibernateProxy;

import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "player")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Player {


    @Id
    @Column(name = "tag", length = 15, nullable = false, updatable = false)
    private String tag;


    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "town_hall_level", nullable = false)
    private short townHallLevel;

    @Column(name = "clan_tag", length = 15)
    private String clanTag;

    @Column(name = "clan_role", length = 20)
    private String clanRole;

    @Column(name = "war_stars", nullable = false)
    private int warStars;

    @Column(name = "donations", nullable = false)
    private int donations;

    @Column(name = "donations_received", nullable = false)
    private int donationsReceived;

    @Column(name = "exp_level", nullable = false)
    private int expLevel;

    @Column(name = "trophies", nullable = false)
    private int trophies;

    /* ==================== Timestamps ==================== */

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;


    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PlayerHero> heroes = new HashSet<>();

    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PlayerTroop> troops = new HashSet<>();

    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PlayerSpell> spells = new HashSet<>();

    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PlayerPet> pets = new HashSet<>();

    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PlayerEquipment> equipment = new HashSet<>();

    @Builder
    private Player(
            String tag,
            String name,
            short townHallLevel,
            String clanTag,
            String clanRole,
            int warStars,
            int donations,
            int donationsReceived,
            int expLevel,
            int trophies
    ) {
        this.tag = Objects.requireNonNull(tag, "tag must not be null");
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.townHallLevel = townHallLevel;
        this.clanTag = clanTag;
        this.clanRole = clanRole;
        this.warStars = warStars;
        this.donations = donations;
        this.donationsReceived = donationsReceived;
        this.expLevel = expLevel;
        this.trophies = trophies;
    }

    /* ==================== Lifecycle Callbacks ==================== */

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }


    public void updateFrom(PlayerUpdateData data) {
        this.name = Objects.requireNonNull(data.name(), "name must not be null");
        this.townHallLevel = data.townHallLevel();
        this.clanTag = data.clanTag();
        this.clanRole = data.clanRole();
        this.warStars = data.warStars();
        this.donations = data.donations();
        this.donationsReceived = data.donationsReceived();
        this.expLevel = data.expLevel();
        this.trophies = data.trophies();
    }

    public void addHero(PlayerHero hero) {
        heroes.add(hero);
        hero.setPlayer(this);
    }

    public void removeHero(PlayerHero hero) {
        heroes.remove(hero);
        hero.setPlayer(null);
    }

    public void addTroop(PlayerTroop troop) {
        troops.add(troop);
        troop.setPlayer(this);
    }

    public void removeTroop(PlayerTroop troop) {
        troops.remove(troop);
        troop.setPlayer(null);
    }

    public void addSpell(PlayerSpell spell) {
        spells.add(spell);
        spell.setPlayer(this);
    }

    public void removeSpell(PlayerSpell spell) {
        spells.remove(spell);
        spell.setPlayer(null);
    }

    public void addPet(PlayerPet pet) {
        pets.add(pet);
        pet.setPlayer(this);
    }

    public void removePet(PlayerPet pet) {
        pets.remove(pet);
        pet.setPlayer(null);
    }

    public void addEquipment(PlayerEquipment eq) {
        equipment.add(eq);
        eq.setPlayer(this);
    }

    public void removeEquipment(PlayerEquipment eq) {
        equipment.remove(eq);
        eq.setPlayer(null);
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

        Player player = (Player) o;
        return tag != null && tag.equals(player.tag);
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy
                ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode()
                : getClass().hashCode();
    }


    @Override
    public String toString() {
        return "Player[tag=" + tag + ", name=" + name + ", townHallLevel=" + townHallLevel + "]";
    }
}