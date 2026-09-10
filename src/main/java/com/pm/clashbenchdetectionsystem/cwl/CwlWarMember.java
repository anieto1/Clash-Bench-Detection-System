package com.pm.clashbenchdetectionsystem.cwl;

import com.pm.clashbenchdetectionsystem.player.Player;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.proxy.HibernateProxy;

import java.util.Objects;

@Entity
@Table(name = "cwl_war_member", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"war_tag", "map_position"})
})
@IdClass(CwlWarMemberId.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CwlWarMember {

    @Id
    @Column(name = "war_tag", length = 50, nullable = false)
    private String warTag;

    @Id
    @Column(name = "player_tag", length = 15, nullable = false)
    private String playerTag;

    @Column(name = "map_position", nullable = false)
    private short mapPosition;

    @Column(name = "town_hall_level", nullable = false)
    private short townHallLevel;

    @Column(name = "attacked", nullable = false)
    private boolean attacked;

    /* ==================== Relationships ==================== */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "war_tag", insertable = false, updatable = false)
    private CwlWar cwlWar;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_tag", insertable = false, updatable = false)
    private Player player;

    /* ==================== Constructor ==================== */

    public CwlWarMember(String warTag, String playerTag, short mapPosition, short townHallLevel) {
        this.warTag = Objects.requireNonNull(warTag, "warTag must not be null");
        this.playerTag = Objects.requireNonNull(playerTag, "playerTag must not be null");
        this.mapPosition = mapPosition;
        this.townHallLevel = townHallLevel;
        this.attacked = false;
    }

    /* ==================== Domain Methods ==================== */

    public void markAttacked() {
        this.attacked = true;
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

        CwlWarMember that = (CwlWarMember) o;
        return warTag != null && playerTag != null
                && warTag.equals(that.warTag)
                && playerTag.equals(that.playerTag);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(warTag, playerTag);
    }

    @Override
    public String toString() {
        return "CwlWarMember[warTag=" + warTag + ", playerTag=" + playerTag + ", pos=" + mapPosition + ", th=" + townHallLevel + "]";
    }
}
