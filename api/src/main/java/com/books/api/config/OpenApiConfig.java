package com.books.api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

/**
 * Advanced configuration for OpenAPI documentation.
 * Configures the OpenAPI specification for the Books and Authors API.
 * Includes detailed information, servers, security schemes and global
 * tags.
 *
 * @author books
 */
@Configuration
public class OpenApiConfig {

        @Value("${spring.application.name}")
        private String applicationName;

        @Value("${server.servlet.context-path:}")
        private String contextPath;

        @Value("${swagger.title}")
        private String swaggerTitle;

        @Value("${swagger.description}")
        private String swaggerDescription;

        @Value("${swagger.version}")
        private String swaggerVersion;

        @Value("${swagger.contact.name}")
        private String contactName;

        @Value("${swagger.contact.url}")
        private String contactUrl;

        @Value("${swagger.contact.email}")
        private String contactEmail;

        /**
         * Creates and configures the OpenAPI bean for API documentation.
         * Includes detailed information, development and production servers,
         * JWT security schemes and global tags for endpoint categorization.
         *
         * @return the configured OpenAPI bean
         */
        @Bean
        public OpenAPI customOpenAPI() {
                final String securitySchemeName = "bearer-key";

                // Server definitions
                Server devServer = new Server()
                                .url("http://localhost:8080" + contextPath)
                                .description("Development server");

                Server prodServer = new Server()
                                .url("https://api.books-authors.com" + contextPath)
                                .description("Production server");

                // Global tags definition
                List<Tag> tags = Arrays.asList(
                                new Tag().name("authors").description("Author-related operations"),
                                new Tag().name("books").description("Book-related operations"));

                return new OpenAPI()
                                .servers(Arrays.asList(devServer, prodServer))
                                .tags(tags)
                                .components(new Components()
                                                .addSecuritySchemes(securitySchemeName,
                                                                new SecurityScheme()
                                                                                .type(SecurityScheme.Type.HTTP)
                                                                                .scheme("bearer")
                                                                                .bearerFormat("JWT")
                                                                                .description("Enter the JWT token with Bearer prefix: Bearer <token>")))
                                .security(Arrays.asList(new SecurityRequirement().addList(securitySchemeName)))
                                .info(new Info()
                                                .title(swaggerTitle)
                                                .description(swaggerDescription)
                                                .version(swaggerVersion)
                                                .contact(new Contact()
                                                                .name(contactName)
                                                                .url(contactUrl)
                                                                .email(contactEmail))
                                                .license(new License()
                                                                .name("Apache 2.0")
                                                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
        }
}