package com.pm.clashbenchdetectionsystem.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties("coc-api")
@Validated
public record CocApiProperties(
        String baseUrl,
        String token
) {

}
