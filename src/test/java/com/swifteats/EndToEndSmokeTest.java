package com.swifteats;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EndToEndSmokeTest {

    @Autowired
    TestRestTemplate rest;

    @Test
    void contextLoads() {
        // Minimal e2e boot sanity; detailed flows are covered by scripts and WebMvcTests
        assertNotNull(rest);
    }
}
