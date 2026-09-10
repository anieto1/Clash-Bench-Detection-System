package com.pm.clashbenchdetectionsystem.cocAPI.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BadgeUrls(
        String small,
        String medium,
        String large
) {}
