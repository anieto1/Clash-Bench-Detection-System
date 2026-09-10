package com.pm.clashbenchdetectionsystem.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@ConfigurationProperties(prefix = "coc.api")
@Validated
public record CocApiProperties(
        @NotBlank String baseUrl,
        @NotBlank String token,
        TimeoutProperties timeout
) {
    public record TimeoutProperties(
            Duration connect,
            Duration read
    ) {
        public TimeoutProperties {
            if (connect == null) connect = Duration.ofSeconds(10);
            if (read == null) read = Duration.ofSeconds(30);
        }
    }

    public TimeoutProperties timeout() {
        return timeout != null ? timeout : new TimeoutProperties(Duration.ofSeconds(10), Duration.ofSeconds(30));
    }
}
