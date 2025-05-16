package com.books.domain.repository;

import com.books.domain.model.Book;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Book domain entity.
 * Defines the contract for database operations related to books.
 * Will be implemented by the infrastructure layer using PL/SQL procedures.
 *
 * @author books
 */
public interface BookRepository {

    /**
     * Finds all books in the system.
     *
     * @return a list of all books
     */
    List<Book> findAll();

    /**
     * Finds a book by its ID.
     *
     * @param id the ID of the book to find
     * @return an Optional containing the book if found, or empty if not found
     */
    Optional<Book> findById(Long id);

    /**
     * Saves a book to the database.
     * If the book has an ID, it will be updated; otherwise, it will be created.
     *
     * @param book the book to save
     * @return the saved book with ID populated if it was a new entity
     */
    Book save(Book book);

    /**
     * Deletes a book by its ID.
     *
     * @param id the ID of the book to delete
     * @return true if the book was deleted, false if the book was not found
     */
    boolean deleteById(Long id);

    /**
     * Finds books by their title (partial match).
     *
     * @param title the title to search for
     * @return a list of books with titles containing the given string
     */
    List<Book> findByTitleContaining(String title);

    /**
     * Finds books by their genre.
     *
     * @param genre the genre to search for
     * @return a list of books with the given genre
     */
    List<Book> findByGenre(String genre);

    /**
     * Finds books by author ID.
     *
     * @param authorId the ID of the author
     * @return a list of books written by the author with the given ID
     */
    List<Book> findByAuthorId(Long authorId);

    /**
     * Finds books published between the given years.
     *
     * @param startYear the start year (inclusive)
     * @param endYear   the end year (inclusive)
     * @return a list of books published between the given years
     */
    List<Book> findByPublicationYearBetween(int startYear, int endYear);
}