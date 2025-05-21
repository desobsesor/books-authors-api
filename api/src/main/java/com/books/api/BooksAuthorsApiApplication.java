package com.books.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import io.github.cdimascio.dotenv.Dotenv;

/**
 * Main application class for the Books and Authors API.
 * Configures component scanning, entity scanning, and repository scanning
 * across all modules of the application.
 * Loads environment variables from .env file before starting the application.
 *
 * @author books-authors-api
 */
@SpringBootApplication
@ComponentScan(basePackages = { "com.books" })
@EntityScan("com.books.domain.model")
@EnableJpaRepositories(basePackages = "com.books.domain.repository")
public class BooksAuthorsApiApplication {

    /**
     * Main method that starts the Spring Boot application.
     * Loads environment variables from .env file before starting.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        // Load environment variables from .env file and add them to system properties
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .systemProperties() // This is crucial - adds env vars to System properties
                .load();

        // Now Spring can access the environment variables
        SpringApplication.run(BooksAuthorsApiApplication.class, args);
    }
}