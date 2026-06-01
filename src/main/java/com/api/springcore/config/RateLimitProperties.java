package com.api.springcore.config;

import jakarta.validation.constraints.Min;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Data
@Component
@ConfigurationProperties(prefix = "rate-limit")
@Validated
public class RateLimitProperties {
    @Min(1)
    private int capacity = 100;

    @Min(1)
    private int refillTokens = 100;

    @Min(1)
    private int refillDurationMinutes = 1;

    @Min(1)
    private int cacheExpireHours = 2;
}
