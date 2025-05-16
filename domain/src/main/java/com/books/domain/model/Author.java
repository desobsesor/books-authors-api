package com.books.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Domain entity representing an author in the system.
 * Contains the core business data and behavior for authors.
 *
 * @author books
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Author {
    private Long authorId;
    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private String biography;

    @Builder.Default
    private Set<Book> books = new HashSet<>();

    /**
     * Returns the full name of the author by combining first and last name.
     *
     * @return the full name of the author
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Adds a book to the author's collection of books.
     *
     * @param book the book to add
     * @return true if the book was added, false otherwise
     */
    public boolean addBook(Book book) {
        return books.add(book);
    }

    /**
     * Removes a book from the author's collection of books.
     *
     * @param book the book to remove
     * @return true if the book was removed, false otherwise
     */
    public boolean removeBook(Book book) {
        return books.remove(book);
    }
}