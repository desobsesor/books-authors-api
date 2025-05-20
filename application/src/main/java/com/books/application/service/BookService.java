package com.books.application.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.books.application.dto.BookDTO;
import com.books.application.dto.CreateBookDTO;
import com.books.application.dto.UpdateBookDTO;
import com.books.application.mapper.BookMapper;
import com.books.domain.model.Book;
import com.books.domain.repository.BookRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
     * Pagination is supported.
     *
     * @param page the page number (0-based)
     * @param size the number of items per page
     *
     * @return a list of all books
     */
    @Transactional(readOnly = true)
    public List<BookDTO> getAllBooks(int page, int size) {
        return bookRepository.findAll(page, size)
                .stream()
                .map(bookMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a book by its ID.
     *
     * @param id the ID of the book to retrieve
     * @return an Optional containing the book if found, or empty if not found
     */
    @Transactional(readOnly = true)
    public Optional<BookDTO> findBookById(Long id) {
        return bookRepository.findById(id)
                .map(bookMapper::toDto);
    }

    /**
     * Creates a new book or updates an existing one.
     *
     * @param createBookDTO the book to save
     * @return the saved book with ID populated if it was a new entity
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public BookDTO createBook(CreateBookDTO createBookDTO) {
        Book book = bookMapper.toEntity(createBookDTO);
        return bookMapper.toDto(bookRepository.save(book));
    }

    /**
     * Updates an existing book.
     *
     * @param id            the ID of the book to update
     * @param updateBookDTO the updated book data
     * @return an Optional with the updated book DTO if found, or empty
     *         if not
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public Optional<BookDTO> updateBook(Long id, UpdateBookDTO updateBookDTO) {
        return bookRepository.findById(id)
                .map(book -> {
                    bookMapper.updateEntityFromDto(updateBookDTO, book);
                    return bookMapper.toDto(bookRepository.save(book));
                });
    }

    /**
     * Deletes a book by its ID.
     *
     * @param id the ID of the book to delete
     * @return true if the book was deleted, false if the book was not found
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public boolean deleteBook(Long id) {
        return bookRepository.deleteById(id);
    }

    /**
     * Searches for books by title (partial match).
     *
     * @param title the title to search for
     * @return a list of books with titles containing the given string
     */
    @Transactional(readOnly = true)
    public List<BookDTO> findBooksByTitle(String title) {
        return bookRepository.findByTitleContaining(title).stream()
                .map(bookMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Searches for books by genre.
     *
     * @param genre the genre to search for
     * @return a list of books with the given genre
     */
    @Transactional(readOnly = true)
    public List<BookDTO> findBooksByGenre(String genre) {
        return bookRepository.findByGenre(genre).stream()
                .map(bookMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Searches for books by author ID.
     *
     * @param authorId the ID of the author
     * @return a list of books written by the author with the given ID
     */
    @Transactional(readOnly = true)
    public List<BookDTO> findBooksByAuthorId(Long authorId) {
        return bookRepository.findByAuthorId(authorId).stream()
                .map(bookMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Searches for books published between the given years.
     *
     * @param startYear the start year (inclusive)
     * @param endYear   the end year (inclusive)
     * @return a list of books published between the given years
     */
    @Transactional(readOnly = true)
    public List<BookDTO> findBooksByPublicationYearRange(int startYear, int endYear) {
        return bookRepository.findByPublicationYearBetween(startYear, endYear).stream()
                .map(bookMapper::toDto)
                .collect(Collectors.toList());
    }
}