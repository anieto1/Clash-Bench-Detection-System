package com.pm.clashbenchdetectionsystem.cwl;

import java.io.Serializable;

public record CwlParticipantId(String clanTag, String season, String playerTag) implements Serializable {
}
