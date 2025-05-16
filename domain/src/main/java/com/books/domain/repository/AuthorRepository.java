package com.books.domain.repository;

import com.books.domain.model.Author;

import lombok.NonNull;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Author domain entity.
 * Defines the contract for database operations related to authors.
 * Will be implemented by the infrastructure layer using PL/SQL procedures.
 *
 * @author books
 */
public interface AuthorRepository {

    /**
     * Finds all authors in the system.
     *
     * @return a list of all authors
     */
    @NonNull
    List<Author> findAll();

    /**
     * Finds an author by their ID.
     *
     * @param id the ID of the author to find
     * @return an Optional containing the author if found, or empty if not found
     */
    Optional<Author> findById(Long id);

    /**
     * Saves an author to the database.
     * If the author has an ID, it will be updated; otherwise, it will be created.
     *
     * @param author the author to save
     * @return the saved author with ID populated if it was a new entity
     */
    Author save(Author author);

    /**
     * Deletes an author by their ID.
     *
     * @param id the ID of the author to delete
     * @return true if the author was deleted, false if the author was not found
     */
    boolean deleteById(Long id);

    /**
     * Finds authors by their last name.
     *
     * @param lastName the last name to search for
     * @return a list of authors with the given last name
     */
    List<Author> findByLastName(String lastName);

    /**
     * Finds authors who have written books in the given genre.
     *
     * @param genre the genre to search for
     * @return a list of authors who have written books in the given genre
     */
    List<Author> findByBookGenre(String genre);

    /**
     * Finds authors who have written a book with the given ID.
     *
     * @param bookId the ID of the book to search for
     * @return a list of authors who have written a book with the given ID
     */
    List<Author> findByBookId(Long bookId);
}