package com.pm.clashbenchdetectionsystem.player;

import java.io.Serializable;

public record PlayerTroopId(String playerTag, String troopName) implements Serializable {
}
