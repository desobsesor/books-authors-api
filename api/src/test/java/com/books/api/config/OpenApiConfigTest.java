package com.books.api.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.swagger.v3.oas.models.OpenAPI;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { OpenApiConfig.class })
public class OpenApiConfigTest {

    @Autowired
    private OpenAPI openAPI;

    @Test
    public void testOpenAPIBean() {
        assertNotNull(openAPI);
    }
}