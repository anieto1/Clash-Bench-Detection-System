package com.pm.clashbenchdetectionsystem.player;

import java.io.Serializable;

public record PlayerSpellId(String playerTag, String spellName) implements Serializable {
}
