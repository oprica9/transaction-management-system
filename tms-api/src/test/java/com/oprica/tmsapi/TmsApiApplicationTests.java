package com.oprica.tmsapi;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@SpringBootTest
class TmsApiApplicationTests {

    private static final Path TEST_CSV = Path.of(
            System.getProperty("java.io.tmpdir"),
            "tms-api-" + UUID.randomUUID() + ".csv"
    );

    @DynamicPropertySource
    static void configureProperties(
            DynamicPropertyRegistry registry
    ) {
        registry.add(
                "tms.storage.csv-path",
                TEST_CSV::toString
        );
    }

    @AfterAll
    static void cleanUp() throws IOException {
        Files.deleteIfExists(TEST_CSV);
    }

    @Test
    void contextLoads() {
    }
}
