package com.books.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Domain entity representing a book in the system.
 * Contains the core business data and behavior for books.
 *
 * @author yovanysuarez
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Book {
    private Long bookId;
    private String title;
    private String isbn;
    private LocalDate publicationDate;
    private String publisher;
    private String genre;
    private String summary;

    @Builder.Default
    private Set<Author> authors = new HashSet<>();

    /**
     * Adds an author to the book's collection of authors.
     *
     * @param author the author to add
     * @return true if the author was added, false otherwise
     */
    public boolean addAuthor(Author author) {
        boolean added = authors.add(author);
        if (added) {
            author.addBook(this);
        }
        return added;
    }

    /**
     * Removes an author from the book's collection of authors.
     *
     * @param author the author to remove
     * @return true if the author was removed, false otherwise
     */
    public boolean removeAuthor(Author author) {
        boolean removed = authors.remove(author);
        if (removed) {
            author.removeBook(this);
        }
        return removed;
    }
}