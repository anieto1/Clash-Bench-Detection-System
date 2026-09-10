package com.pm.clashbenchdetectionsystem.cwl;

import java.io.Serializable;

public record CwlAttackId(String warTag, String attackerTag) implements Serializable {
}
