package com.pm.clashbenchdetectionsystem.player;

import java.io.Serializable;

public record PlayerPetId(String playerTag, String petName) implements Serializable {
}
