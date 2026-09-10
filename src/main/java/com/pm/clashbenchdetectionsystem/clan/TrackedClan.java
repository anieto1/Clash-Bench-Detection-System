package com.pm.clashbenchdetectionsystem.clan;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "tracked_clan")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TrackedClan {

    @Id
    @Column(name = "clan_tag", length = 15, nullable = false)
    private String clanTag;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clan_tag", insertable = false, updatable = false)
    private Clan clan;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "last_polled_at")
    private Instant lastPolledAt;

    @Column(name = "poll_interval", nullable = false)
    private int pollInterval = 300;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public TrackedClan(String clanTag) {
        this.clanTag = clanTag;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }

    public void markPolled() {
        this.lastPolledAt = Instant.now();
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    @Override
    public String toString() {
        return "TrackedClan[clanTag=" + clanTag + ", active=" + active + "]";
    }
}
