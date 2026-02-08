package com.devsecops;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class CorsConfigTest {

    @Autowired
    private CorsConfig corsConfig;

    @Test
    public void testCorsConfigurer() {
        WebMvcConfigurer configurer = corsConfig.corsConfigurer();
        assertNotNull(configurer, "CorsConfig should create WebMvcConfigurer bean");
    }

    @Test
    public void testCorsMapping() {
        // This is a simple test to verify the bean is created
        // In a real test, you'd mock CorsRegistry and verify mappings
        assertNotNull(corsConfig, "CorsConfig should be autowired");
    }
}
