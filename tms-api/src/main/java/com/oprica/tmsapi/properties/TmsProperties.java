package com.oprica.tmsapi.properties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.nio.file.Path;
import java.util.List;

@Validated
@ConfigurationProperties(prefix = "tms")
public record TmsProperties(
        @NotNull @Valid Storage storage,
        @NotNull @Valid Cors cors
) {

    public record Storage(@NotNull Path csvPath) {
    }

    public record Cors(@NotEmpty List<@NotBlank String> allowedOrigins) {
    }
}
