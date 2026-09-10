package com.pm.clashbenchdetectionsystem.common.exception;


import lombok.Getter;

@Getter
public class ResourceNotFoundException extends RuntimeException {
    private final String resourceType;
    private final String resourceId;

    private ResourceNotFoundException(String resourceType, String resourceId) {
        super(resourceType + " not found: " + resourceId);
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }

    public static ResourceNotFoundException player(String tag) {
        return new ResourceNotFoundException("Player", tag);
    }

    public static ResourceNotFoundException clan(String tag) {
        return new ResourceNotFoundException("Clan", tag);
    }

    public static ResourceNotFoundException cwlSeason(String clanTag, String season) {
        return new ResourceNotFoundException("CwlSeason", clanTag + "/" + season);
    }

    public static ResourceNotFoundException cwlWar(String warTag) {
        return new ResourceNotFoundException("CwlWar", warTag);
    }
}
