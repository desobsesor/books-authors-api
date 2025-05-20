package com.books.api.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.books.application.dto.BookDTO;
import com.books.application.dto.CreateBookDTO;
import com.books.application.dto.UpdateBookDTO;
import com.books.application.service.BookService;
import com.books.domain.model.Book;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST controller for book operations.
 * Exposes endpoints for CRUD operations and searches on books.
 *
 * @author books-authors-api
 */
@RestController
@RequestMapping("${CONTEXT_PATH}/books")
@SecurityRequirement(name = "bearer-key")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "books", description = "Book management API")
public class BookController {

        private final BookService bookService;

        /**
         * Get all books.
         *
         * Pagination is supported.
         * By default, 10 authors per page are returned.
         * Can be customized using query parameters:
         * - page: page number (default: 0)
         * - size: number of authors per page (default: 10)
         *
         * @return a list of all books
         */
        @GetMapping
        @Operation(summary = "Get all books", description = "Retrieves a list of all books in the system")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Books retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookDTO.class)))
        })
        public ResponseEntity<List<BookDTO>> getAllBooks(
                        @RequestParam(defaultValue = "1") int page,
                        @RequestParam(defaultValue = "10") int size) {

                log.debug("REST request to get all authors with pagination: page={}, size={}", page, size);
                List<BookDTO> books = bookService.getAllBooks(page, size);
                return ResponseEntity.ok(books);
        }

        /**
         * Retrieves a book by its ID.
         *
         * @param id the ID of the book to retrieve
         * @return the book if found, or 404 Not Found if not found
         */
        @GetMapping("/{id}")
        @Operation(summary = "Get book by ID", description = "Retrieves a book by its ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Book found", content = @Content(schema = @Schema(implementation = Book.class))),
                        @ApiResponse(responseCode = "404", description = "Book not found")
        })
        public ResponseEntity<BookDTO> getBookById(
                        @Parameter(description = "ID of the book to retrieve") @PathVariable Long id) {
                return bookService.findBookById(id)
                                .map(ResponseEntity::ok)
                                .orElse(ResponseEntity.notFound().build());
        }

        /**
         * Creates a new book.
         *
         * @param book the book to create
         * @return the created book with ID populated
         */
        @PostMapping
        @Operation(summary = "Create a new book", description = "Creates a new book in the system")
        @ApiResponse(responseCode = "201", description = "Book created successfully")
        public ResponseEntity<BookDTO> createBook(
                        @Parameter(description = "Data to create the author", required = true) @RequestBody CreateBookDTO createBookDTO) {
                log.debug("REST request to create a new book: {}", createBookDTO);
                BookDTO savedBook = bookService.createBook(createBookDTO);
                return ResponseEntity.status(HttpStatus.CREATED).body(savedBook);
        }

        /**
         * Updates an existing book.
         *
         * @param id   the ID of the book to update
         * @param book the updated book data
         * @return the updated book, or 404 Not Found if the book was not found
         */
        @PutMapping("/{id}")
        @Operation(summary = "Update a book", description = "Updates an existing book in the system")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Book updated successfully"),
                        @ApiResponse(responseCode = "404", description = "Book not found")
        })
        public ResponseEntity<BookDTO> updateBook(
                        @Parameter(description = "ID of the book to update") @PathVariable Long id,
                        @Parameter(description = "Updated book data", required = true) @RequestBody UpdateBookDTO updateBookDTO) {
                log.debug("REST request to update book with ID: {}", id);
                return bookService.updateBook(id, updateBookDTO)
                                .map(ResponseEntity::ok)
                                .orElse(ResponseEntity.notFound().build());
        }

        /**
         * Deletes a book by its ID.
         *
         * @param id the ID of the book to delete
         * @return 204 No Content if the book was deleted, or 404 Not Found if the book
         *         was not found
         */
        @DeleteMapping("/{id}")
        @Operation(summary = "Delete a book", description = "Deletes a book from the system")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "204", description = "Book deleted successfully"),
                        @ApiResponse(responseCode = "404", description = "Book not found")
        })
        public ResponseEntity<Void> deleteBook(
                        @Parameter(description = "ID of the book to delete") @PathVariable Long id) {
                boolean deleted = bookService.deleteBook(id);
                return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
        }

        /**
         * Searches for books by title (partial match).
         *
         * @param title the title to search for
         * @return a list of books with titles containing the given string
         */
        @GetMapping("/search/title")
        @Operation(summary = "Search books by title", description = "Searches for books with titles containing the given string")
        @ApiResponse(responseCode = "200", description = "Search completed successfully")
        public ResponseEntity<List<BookDTO>> searchBooksByTitle(
                        @Parameter(description = "Title to search for") @RequestParam String title) {
                return ResponseEntity.ok(bookService.findBooksByTitle(title));
        }

        /**
         * Searches for books by genre.
         *
         * @param genre the genre to search for
         * @return a list of books with the given genre
         */
        @GetMapping("/search/genre")
        @Operation(summary = "Search books by genre", description = "Searches for books with the given genre")
        @ApiResponse(responseCode = "200", description = "Search completed successfully")
        public ResponseEntity<List<BookDTO>> searchBooksByGenre(
                        @Parameter(description = "Genre to search for") @RequestParam String genre) {
                return ResponseEntity.ok(bookService.findBooksByGenre(genre));
        }

        /**
         * Searches for books by author ID.
         *
         * @param authorId the ID of the author
         * @return a list of books written by the author with the given ID
         */
        @GetMapping("/search/author/{authorId}")
        @Operation(summary = "Search books by author", description = "Searches for books written by the author with the given ID")
        @ApiResponse(responseCode = "200", description = "Search completed successfully")
        public ResponseEntity<List<BookDTO>> searchBooksByAuthor(
                        @Parameter(description = "ID of the author") @PathVariable Long authorId) {
                return ResponseEntity.ok(bookService.findBooksByAuthorId(authorId));
        }

        /**
         * Searches for books published between the given years.
         *
         * @param startYear the start year (inclusive)
         * @param endYear   the end year (inclusive)
         * @return a list of books published between the given years
         */
        @GetMapping("/search/year-range")
        @Operation(summary = "Search books by publication year range", description = "Searches for books published between the given years")
        @ApiResponse(responseCode = "200", description = "Search completed successfully")
        public ResponseEntity<List<BookDTO>> searchBooksByYearRange(
                        @Parameter(description = "Start year (inclusive)") @RequestParam int startYear,
                        @Parameter(description = "End year (inclusive)") @RequestParam int endYear) {
                return ResponseEntity.ok(bookService.findBooksByPublicationYearRange(startYear, endYear));
        }
}