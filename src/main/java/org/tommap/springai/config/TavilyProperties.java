package org.tommap.springai.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@Data
@ConfigurationProperties(prefix = "tavily")
@Validated
public class TavilyProperties {
    @NotBlank(message = "apiKey must be set!")
    private String apiKey;

    @NotBlank(message = "searchBaseUrl must be set!")
    private String searchBaseUrl;

    @Positive(message = "maxResults must be greater than 0!")
    private int maxResults;
}
