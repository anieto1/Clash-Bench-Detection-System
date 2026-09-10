package com.pm.clashbenchdetectionsystem.clan;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.proxy.HibernateProxy;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "clan")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Clan {

    @Id
    @Column(name = "tag", length = 15, nullable = false, updatable = false)
    private String tag;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "clan_level", nullable = false)
    private int clanLevel;

    @Column(name = "clan_points", nullable = false)
    private int clanPoints;

    @Column(name = "war_wins", nullable = false)
    private int warWins;

    @Column(name = "war_ties", nullable = false)
    private int warTies;

    @Column(name = "war_losses", nullable = false)
    private int warLosses;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "badge_url", length = 255)
    private String badgeUrl;


    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Builder
    private Clan(String tag, String name, Integer clanLevel, Integer clanPoints,
                 Integer warWins, Integer warTies, Integer warLosses,
                 String description, String badgeUrl) {
        this.tag = Objects.requireNonNull(tag, "tag must not be null");
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.clanLevel = clanLevel != null ? clanLevel : 0;
        this.clanPoints = clanPoints != null ? clanPoints : 0;
        this.warWins = warWins != null ? warWins : 0;
        this.warTies = warTies != null ? warTies : 0;
        this.warLosses = warLosses != null ? warLosses : 0;
        this.description = description;
        this.badgeUrl = badgeUrl;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public void updateFrom(ClanUpdateData data) {
        this.name = Objects.requireNonNull(data.name(), "name must not be null");
        if (data.clanLevel() != null) this.clanLevel = data.clanLevel();
        if (data.clanPoints() != null) this.clanPoints = data.clanPoints();
        if (data.warWins() != null) this.warWins = data.warWins();
        if (data.warTies() != null) this.warTies = data.warTies();
        if (data.warLosses() != null) this.warLosses = data.warLosses();
        this.description = data.description();
        this.badgeUrl = data.badgeUrl();
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

        Clan clan = (Clan) o;
        return tag != null && tag.equals(clan.tag);
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy
                ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode()
                : getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Clan[tag=" + tag + ", name=" + name + ", clanLevel=" + clanLevel + "]";
    }
}
