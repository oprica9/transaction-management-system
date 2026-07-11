package com.oprica.tmsapi.properties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.nio.file.Path;

@Validated
@ConfigurationProperties(prefix = "tms")
public record TmsProperties(
        @NotNull @Valid Storage storage
) {

    public record Storage(@NotNull Path csvPath) {
    }
}
