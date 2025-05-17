package com.books.application.service;

import com.books.application.dto.BookDTO;
import com.books.application.dto.CreateBookDTO;
import com.books.application.dto.UpdateBookDTO;
import com.books.application.mapper.BookMapper;
import com.books.domain.model.Book;
import com.books.domain.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service class that implements business logic for book operations.
 * Acts as a facade between the API layer and the domain layer.
 *
 * @author books
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BookService {

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;

    /**
     * Retrieves all books from the repository.
     *
     * @return a list of all books
     */
    public List<Book> findAllBooks() {
        log.debug("Getting all books");
        return bookRepository.findAll();
    }

    /**
     * Retrieves a book by its ID.
     *
     * @param id the ID of the book to retrieve
     * @return an Optional containing the book if found, or empty if not found
     */
    public Optional<Book> findBookById(Long id) {
        log.debug("Getting book with ID: {}", id);
        return bookRepository.findById(id);
    }

    /**
     * Creates a new book or updates an existing one.
     *
     * @param book the book to save
     * @return the saved book with ID populated if it was a new entity
     */
    public BookDTO createBook(CreateBookDTO createBookDTO) {
        log.debug("Creating new book: {}", createBookDTO);
        Book book = bookMapper.toEntity(createBookDTO);
        Book savedBook = bookRepository.save(book);
        return bookMapper.toDto(savedBook);
    }

    /**
     * Updates an existing book.
     *
     * @param id            the ID of the book to update
     * @param updateBookDTO the updated book data
     * @return an Optional with the updated book DTO if found, or empty
     *         if not
     */
    public Optional<BookDTO> updateBook(Long id, UpdateBookDTO updateBookDTO) {
        log.debug("Updating book with ID: {}", id);
        return bookRepository.findById(id)
                .map(book -> {
                    bookMapper.updateEntityFromDto(updateBookDTO, book);
                    Book updatedBook = bookRepository.save(book);
                    return bookMapper.toDto(updatedBook);
                });
    }

    /**
     * Deletes a book by its ID.
     *
     * @param id the ID of the book to delete
     * @return true if the book was deleted, false if the book was not found
     */
    public boolean deleteBook(Long id) {
        return bookRepository.deleteById(id);
    }

    /**
     * Searches for books by title (partial match).
     *
     * @param title the title to search for
     * @return a list of books with titles containing the given string
     */
    public List<Book> findBooksByTitle(String title) {
        return bookRepository.findByTitleContaining(title);
    }

    /**
     * Searches for books by genre.
     *
     * @param genre the genre to search for
     * @return a list of books with the given genre
     */
    public List<Book> findBooksByGenre(String genre) {
        return bookRepository.findByGenre(genre);
    }

    /**
     * Searches for books by author ID.
     *
     * @param authorId the ID of the author
     * @return a list of books written by the author with the given ID
     */
    public List<Book> findBooksByAuthorId(Long authorId) {
        return bookRepository.findByAuthorId(authorId);
    }

    /**
     * Searches for books published between the given years.
     *
     * @param startYear the start year (inclusive)
     * @param endYear   the end year (inclusive)
     * @return a list of books published between the given years
     */
    public List<Book> findBooksByPublicationYearRange(int startYear, int endYear) {
        return bookRepository.findByPublicationYearBetween(startYear, endYear);
    }
}