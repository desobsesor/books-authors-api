package com.books.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Main application class for the Books and Authors API.
 * Configures component scanning, entity scanning, and repository scanning
 * across all modules of the application.
 *
 * @author books
 */
@SpringBootApplication
@ComponentScan(basePackages = { "com.books" })
@EntityScan("com.books.domain.model")
@EnableJpaRepositories("com.books.infrastructure.repository")
public class BooksAuthorsApiApplication {

    /**
     * Main method that starts the Spring Boot application.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(BooksAuthorsApiApplication.class, args);
    }
}