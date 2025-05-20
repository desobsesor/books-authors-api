package com.books.api.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.books.application.dto.BookDTO;
import com.books.application.dto.CreateBookDTO;
import com.books.application.dto.UpdateBookDTO;
import com.books.application.service.BookService;
import com.books.domain.model.Author;

/**
 * Unit tests for {@link BookController}.
 *
 * These tests verify the behavior of the REST endpoints related to books,
 * using Mockito to simulate the behavior of the underlying service.
 *
 * @author books
 */
@ExtendWith(MockitoExtension.class)
public class BookControllerTest {

    @Mock
    private BookService bookService;

    @InjectMocks
    private BookController bookController;

    private List<BookDTO> bookListDTO;
    private BookDTO bookDTO1;
    private BookDTO bookDTO2;
    private CreateBookDTO createBook1;

    /**
     * Initial setup for each test.
     * Creates test data that will be used in the tests.
     */
    @BeforeEach
    void setUp() {
        // Create test data for CreateBookDTO
        createBook1 = CreateBookDTO.builder()
                .title("Cien años de soledad")
                .isbn("9780307474728")
                .publicationDate(LocalDate.of(1967, 5, 30))
                .publisher("Editorial Sudamericana")
                .genre("Realismo mágico")
                .build();

        // Create test data for BookDTO
        Set<Long> authorIds1 = new HashSet<>();
        authorIds1.add(1L);

        Set<Author> author1 = new HashSet<>(Collections.singleton(Author.builder()
                .authorId(1L)
                .firstName("Gabriel")
                .lastName("García Márquez")
                .birthDate(LocalDate.of(1927, 3, 6))
                .biography("Realismo mágico")
                .build()));

        bookDTO1 = BookDTO.builder()
                .bookId(1L)
                .title("Nuestro Hogar")
                .isbn("9780307474728")
                .publicationDate(LocalDate.of(1967, 5, 30))
                .authorIds(author1)
                .build();

        bookDTO2 = BookDTO.builder()
                .bookId(2L)
                .title("A la luz del Camino")
                .isbn("97803074343434")
                .publicationDate(LocalDate.of(1967, 5, 30))
                .authorIds(author1)
                .build();

        bookListDTO = new ArrayList<>();
        bookListDTO.add(bookDTO1);
        bookListDTO.add(bookDTO2);

        Set<Long> authorIds2 = new HashSet<>();
        authorIds2.add(2L);

    }

    /**
     * Test to verify that the getAllBooks method correctly returns a list of
     * books when books are available.
     */
    @Test
    @DisplayName("Should return all books when they exist")
    void getAllBooks_ShouldReturnAllBooks_WhenBooksExist() {
        // Configure the mocked service behavior
        when(bookService.getAllBooks(0, 2)).thenReturn(bookListDTO);

        // Execute the method under test
        ResponseEntity<List<BookDTO>> response = bookController.getAllBooks(0, 2);

        // Verify results
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        assertEquals(bookDTO1.getBookId(), response.getBody().get(0).getBookId());
        assertEquals(bookDTO2.getBookId(), response.getBody().get(1).getBookId());
    }

    /**
     * Test to verify that the getAllBooks method correctly returns an empty list
     * when no books are available.
     */
    @Test
    @DisplayName("Should return empty list when no books exist")
    void getAllBooks_ShouldReturnEmptyList_WhenNoBooksExist() {
        // Configure the mocked service behavior to return an empty list
        when(bookService.getAllBooks(1, 10)).thenReturn(Collections.emptyList());

        // Execute the method under test
        ResponseEntity<List<BookDTO>> response = bookController.getAllBooks(1, 10);

        // Verify results
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, response.getBody().size());
    }

    /**
     * Test to verify that the getBookById method correctly returns a book when
     * the ID exists.
     */
    @Test
    @DisplayName("Should return the book when the ID exists")
    void getBookById_ShouldReturnBook_WhenIdExists() {
        // Configure the mocked service behavior
        when(bookService.findBookById(1L)).thenReturn(java.util.Optional.of(bookDTO1));

        // Execute the method under test
        ResponseEntity<BookDTO> response = bookController.getBookById(1L);

        // Verify results
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(bookDTO1.getBookId(), response.getBody().getBookId());
        assertEquals(bookDTO1.getTitle(), response.getBody().getTitle());
    }

    /**
     * Test to verify that the getBookById method returns 404 when the ID does not
     * exist.
     */
    @Test
    @DisplayName("Should return 404 when the ID does not exist")
    void getBookById_ShouldReturnNotFound_WhenIdDoesNotExist() {
        // Configure the mocked service behavior
        when(bookService.findBookById(99L)).thenReturn(java.util.Optional.empty());

        // Execute the method under test
        ResponseEntity<BookDTO> response = bookController.getBookById(99L);

        // Verify results
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    /**
     * Test to verify that the createBook method creates a book correctly when
     * the data is valid.
     */
    @Test
    @DisplayName("Should create a book with valid data")
    void createBook_ShouldCreateBook_WhenValidData() {
        when(bookService.createBook(createBook1)).thenReturn(bookDTO1);
        ResponseEntity<BookDTO> response = bookController.createBook(createBook1);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(bookDTO1.getBookId(), response.getBody().getBookId());
    }

    /**
     * Test to verify that the createBook method returns 400 when the data is
     * invalid.
     */
    @Test
    @DisplayName("Should return 400 when the data is invalid when creating book")
    void createBook_ShouldReturnBadRequest_WhenInvalidData() {
        CreateBookDTO invalidBook = CreateBookDTO.builder().build();
        when(bookService.createBook(invalidBook)).thenThrow(new IllegalArgumentException("Datos inválidos"));
        assertThrows(IllegalArgumentException.class, () -> bookController.createBook(invalidBook));
    }

    /**
     * Test to verify that the updateBook method updates an existing book
     * correctly.
     */
    @Test
    @DisplayName("Should update the book when exists")
    void updateBook_ShouldUpdateBook_WhenExists() {
        UpdateBookDTO updated = UpdateBookDTO.builder()
                .bookId(1L)
                .title("Cien años de soledad - Edición especial")
                .isbn("9780307474728")
                .build();
        when(bookService.updateBook(1L, updated)).thenReturn(java.util.Optional.of(bookDTO1));
        ResponseEntity<BookDTO> response = bookController.updateBook(1L, updated);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updated.getBookId(), response.getBody().getBookId());
    }

    /**
     * Test to verify that the updateBook method returns 404 when the book does
     * not exist.
     */
    @Test
    @DisplayName("Should return 404 when the book to update does not exist")
    void updateBook_ShouldReturnNotFound_WhenNotExists() {
        UpdateBookDTO updated = UpdateBookDTO.builder().bookId(99L).build();
        when(bookService.updateBook(99L, updated)).thenReturn(java.util.Optional.empty());
        ResponseEntity<BookDTO> response = bookController.updateBook(99L, updated);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    /**
     * Test to verify that the deleteBook method deletes an existing book
     * correctly.
     */
    @Test
    @DisplayName("Should delete the book when exists")
    void deleteBook_ShouldDeleteBook_WhenExists() {
        when(bookService.deleteBook(1L)).thenReturn(true);
        ResponseEntity<Void> response = bookController.deleteBook(1L);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    /**
     * Test to verify that the deleteBook method returns 404 when the book does
     * not exist.
     */
    @Test
    @DisplayName("Should return 404 when the book to delete does not exist")
    void deleteBook_ShouldReturnNotFound_WhenNotExists() {
        when(bookService.deleteBook(99L)).thenReturn(false);
        ResponseEntity<Void> response = bookController.deleteBook(99L);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    /**
     * Test to verify the search for books by title.
     */
    @Test
    @DisplayName("Should return books that match the title")
    void searchBooksByTitle_ShouldReturnBooks_WhenMatch() {
        when(bookService.findBooksByTitle("Nuestro Hogar")).thenReturn(List.of(bookDTO1));
        ResponseEntity<List<BookDTO>> response = bookController.searchBooksByTitle("Nuestro Hogar");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("Nuestro Hogar", response.getBody().get(0).getTitle());
    }

    /**
     * Test to verify that the search by title returns an empty list when there
     * are no matches.
     */
    @Test
    @DisplayName("Should return empty list when there are no title matches")
    void searchBooksByTitle_ShouldReturnEmptyList_WhenNoMatch() {
        when(bookService.findBooksByTitle("NoExiste")).thenReturn(Collections.emptyList());
        ResponseEntity<List<BookDTO>> response = bookController.searchBooksByTitle("NoExiste");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, response.getBody().size());
    }

    /**
     * Test to verify the search for books by genre.
     */
    @Test
    @DisplayName("Should return books that match the genre")
    void searchBooksByGenre_ShouldReturnBooks_WhenMatch() {
        when(bookService.findBooksByGenre("Realismo mágico")).thenReturn(List.of(bookDTO1));
        ResponseEntity<List<BookDTO>> response = bookController.searchBooksByGenre("Realismo mágico");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("Nuestro Hogar", response.getBody().get(0).getTitle());
    }

    /**
     * Test to verify that the search by genre returns an empty list when there
     * are no matches.
     */
    @Test
    @DisplayName("Should return empty list when there are no genre matches")
    void searchBooksByGenre_ShouldReturnEmptyList_WhenNoMatch() {
        when(bookService.findBooksByGenre("NoExiste")).thenReturn(Collections.emptyList());
        ResponseEntity<List<BookDTO>> response = bookController.searchBooksByGenre("NoExiste");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, response.getBody().size());
    }

    /**
     * Test to verify the search for books by author ID.
     */
    @Test
    @DisplayName("Should return books associated with the author")
    void searchBooksByAuthor_ShouldReturnBooks_WhenAuthorHasBooks() {
        when(bookService.findBooksByAuthorId(1L)).thenReturn(List.of(bookDTO1));
        ResponseEntity<List<BookDTO>> response = bookController.searchBooksByAuthor(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    /**
     * Test to verify that the search by author returns an empty list when there are
     * no associated books.
     */
    @Test
    @DisplayName("Should return empty list when there are no books associated with the author")
    void searchBooksByAuthor_ShouldReturnEmptyList_WhenNoBooksForAuthor() {
        when(bookService.findBooksByAuthorId(99L)).thenReturn(Collections.emptyList());
        ResponseEntity<List<BookDTO>> response = bookController.searchBooksByAuthor(99L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, response.getBody().size());
    }

    /**
     * Test to verify the search for books by publication year range.
     */
    @Test
    @DisplayName("Should return books published within the year range")
    void searchBooksByYearRange_ShouldReturnBooks_WhenInRange() {
        when(bookService.findBooksByPublicationYearRange(1960, 1970)).thenReturn(List.of(bookDTO1));
        ResponseEntity<List<BookDTO>> response = bookController.searchBooksByYearRange(1960, 1970);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("Nuestro Hogar", response.getBody().get(0).getTitle());
    }

    /**
     * Test to verify that the search by year range returns an empty list when there
     * are no books in that range.
     */
    @Test
    @DisplayName("Should return empty list when there are no books in the year range")
    void searchBooksByYearRange_ShouldReturnEmptyList_WhenNoMatch() {
        when(bookService.findBooksByPublicationYearRange(2000, 2010)).thenReturn(Collections.emptyList());
        ResponseEntity<List<BookDTO>> response = bookController.searchBooksByYearRange(2000, 2010);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, response.getBody().size());
    }
}