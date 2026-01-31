package com.pm.clashbenchdetectionsystem.player;

import com.pm.clashbenchdetectionsystem.clan.Clan;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Entity
@NoArgsConstructor
@Table(name = "players")
public class Player {

    @Id
    @Column(name = "player_id")
    private String tag;
    
    @Column(name = "name")
    @NotNull
    private String name;
    
    @Column(name = "town_hall_level")
    @Min(value = 1)
    @Max(value = 18)
    private int townHallLevel;

    @ManyToOne
    @JoinColumn(name = "clan_tag", referencedColumnName = "tag")
    private Clan clan;

    @Column(name = "clan_role", length = 20)
    private String clanRole;

    @Column(name = "war_stars", nullable = false)
    @Min(value = 0)
    private int warStars = 0;

    @Column(name = "donations", nullable = false)
    @Min(value = 0)
    private int donations = 0;

    @Column(name = "donations_received", nullable = false)
    @Min(value = 0)
    private int donationsReceived = 0;

    @Column(name = "exp_level", nullable = false)
    @Min(value = 1)
    private int expLevel = 1;

    @Column(name = "trophies", nullable = false)
    @Min(value = 0)
    private int trophies = 0;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;


}
