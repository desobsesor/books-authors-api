package com.books.api.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.books.application.dto.AuthorDTO;
import com.books.application.dto.CreateAuthorDTO;
import com.books.application.dto.UpdateAuthorDTO;
import com.books.application.service.AuthorService;

/**
 * Unit tests for {@link AuthorController}.
 *
 * These tests verify the behavior of the REST endpoints related to authors,
 * using Mockito to simulate the behavior of the underlying service.
 *
 * @author books
 */
@ExtendWith(MockitoExtension.class)
public class AuthorControllerTest {

    @Mock
    private AuthorService authorService;

    @InjectMocks
    private AuthorController authorController;

    private List<AuthorDTO> authorList;
    private AuthorDTO author1;
    private AuthorDTO author2;

    private CreateAuthorDTO createAuthor1;

    /**
     * Initial setup for each test.
     * Creates test data that will be used in the tests.
     */
    @BeforeEach
    void setUp() {
        createAuthor1 = CreateAuthorDTO.builder()
                .firstName("Gabriel")
                .lastName("García Márquez")
                .birthDate(LocalDate.of(1927, 3, 6))
                .biography("Colombian writer, Nobel Prize winner in literature")
                .build();
        // Create test data
        author1 = AuthorDTO.builder()
                .authorId(1L)
                .firstName("Gabriel")
                .lastName("García Márquez")
                .birthDate(LocalDate.of(1927, 3, 6))
                .biography("Colombian writer, Nobel Prize winner in literature")
                .fullName("Gabriel García Márquez")
                .bookBookIds(new HashSet<>())
                .build();

        author2 = AuthorDTO.builder()
                .authorId(2L)
                .firstName("Jorge Luis")
                .lastName("Borges")
                .birthDate(LocalDate.of(1899, 8, 24))
                .biography("Argentine writer, known for his short stories")
                .fullName("Jorge Luis Borges")
                .bookBookIds(new HashSet<>())
                .build();

        authorList = new ArrayList<>();
        authorList.add(author1);
        authorList.add(author2);
    }

    /**
     * Test to verify that the getAllAuthors method correctly returns a list of
     * authors when authors are available.
     */
    @Test
    @DisplayName("Should return all authors when they exist")
    void getAllAuthors_ShouldReturnAllAuthors_WhenAuthorsExist() {
        // Configure the mocked service behavior
        when(authorService.getAllAuthors(1, 10)).thenReturn(authorList);

        // Execute the method under test
        ResponseEntity<List<AuthorDTO>> response = authorController.getAllAuthors(1, 10);

        // Verify results
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        assertEquals(author1.getAuthorId(), response.getBody().get(0).getAuthorId());
        assertEquals(author2.getAuthorId(), response.getBody().get(1).getAuthorId());
    }

    /**
     * Test to verify that the getAllAuthors method correctly returns an empty list
     * when no authors are available.
     */
    @Test
    @DisplayName("Should return empty list when no authors exist")
    void getAllAuthors_ShouldReturnEmptyList_WhenNoAuthorsExist() {
        // Configure the mocked service behavior to return an empty list
        when(authorService.getAllAuthors(1, 10)).thenReturn(Collections.emptyList());

        // Execute the method under test
        ResponseEntity<List<AuthorDTO>> response = authorController.getAllAuthors(1, 10);

        // Verify results
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, response.getBody().size());
    }

    /**
     * Test to verify that the getAuthorById method correctly returns an author when
     * the ID exists.
     */
    @Test
    @DisplayName("Should return the author when the ID exists")
    void getAuthorById_ShouldReturnAuthor_WhenIdExists() {
        // Configure the mocked service behavior
        when(authorService.getAuthorById(1L)).thenReturn(java.util.Optional.of(author1));

        // Execute the method under test
        ResponseEntity<com.books.application.dto.AuthorDTO> response = authorController.getAuthorById(1L);

        // Verify results
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(author1.getAuthorId(), response.getBody().getAuthorId());
        assertEquals(author1.getFullName(), response.getBody().getFullName());
    }

    /**
     * Test to verify that the getAuthorById method returns 404 when the ID does not
     * exist.
     */
    @Test
    @DisplayName("Should return 404 when the ID does not exist")
    void getAuthorById_ShouldReturnNotFound_WhenIdDoesNotExist() {
        // Configure the mocked service behavior
        when(authorService.getAuthorById(99L)).thenReturn(java.util.Optional.empty());

        // Execute the method under test
        ResponseEntity<com.books.application.dto.AuthorDTO> response = authorController.getAuthorById(99L);

        // Verify results
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    /**
     * Test to verify that the createAuthor method creates an author correctly when
     * the data is valid.
     */
    @Test
    @DisplayName("Should create an author with valid data")
    void createAuthor_ShouldCreateAuthor_WhenValidData() {
        when(authorService.createAuthor(createAuthor1)).thenReturn(author1);
        ResponseEntity<com.books.application.dto.AuthorDTO> response = authorController.createAuthor(createAuthor1);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(author1.getAuthorId(), response.getBody().getAuthorId());
    }

    /**
     * Test to verify that the createAuthor method returns 400 when the data is
     * invalid.
     */
    @Test
    @DisplayName("Should return 400 when the data is invalid when creating author")
    void createAuthor_ShouldReturnBadRequest_WhenInvalidData() {
        com.books.application.dto.CreateAuthorDTO invalidAuthor = com.books.application.dto.CreateAuthorDTO.builder()
                .build();
        when(authorService.createAuthor(invalidAuthor)).thenThrow(new IllegalArgumentException("Datos inválidos"));
        assertThrows(IllegalArgumentException.class, () -> authorController.createAuthor(invalidAuthor));
    }

    /**
     * Test to verify that the updateAuthor method updates an existing author
     * correctly.
     */
    @Test
    @DisplayName("Should update the author when exists")
    void updateAuthor_ShouldUpdateAuthor_WhenExists() {
        UpdateAuthorDTO updated = UpdateAuthorDTO.builder().authorId(1L).firstName("Gabriel")
                .lastName("García Márquez")
                .build();
        when(authorService.updateAuthor(1L, updated)).thenReturn(java.util.Optional.of(author1));
        ResponseEntity<AuthorDTO> response = authorController.updateAuthor(1L, updated);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updated.getAuthorId(), response.getBody().getAuthorId());
    }

    /**
     * Test to verify that the updateAuthor method returns 404 when the author does
     * not exist.
     */
    @Test
    @DisplayName("Should return 404 when the author to update does not exist")
    void updateAuthor_ShouldReturnNotFound_WhenNotExists() {
        UpdateAuthorDTO updated = UpdateAuthorDTO.builder().authorId(99L).build();
        when(authorService.updateAuthor(99L, updated)).thenReturn(java.util.Optional.empty());
        ResponseEntity<AuthorDTO> response = authorController.updateAuthor(99L, updated);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    /**
     * Test to verify that the deleteAuthor method deletes an existing author
     * correctly.
     */
    @Test
    @DisplayName("Should delete the author when exists")
    void deleteAuthor_ShouldDeleteAuthor_WhenExists() {
        when(authorService.deleteAuthor(1L)).thenReturn(true);
        ResponseEntity<Void> response = authorController.deleteAuthor(1L);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    /**
     * Test to verify that the deleteAuthor method returns 404 when the author does
     * not exist.
     */
    @Test
    @DisplayName("Should return 404 when the author to delete does not exist")
    void deleteAuthor_ShouldReturnNotFound_WhenNotExists() {
        when(authorService.deleteAuthor(99L)).thenReturn(false);
        ResponseEntity<Void> response = authorController.deleteAuthor(99L);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    /**
     * Test to verify the search for authors by last name.
     */
    @Test
    @DisplayName("Should return authors that match the last name")
    void searchAuthorsByLastName_ShouldReturnAuthors_WhenMatch() {
        when(authorService.findAuthorsByLastName("Borges")).thenReturn(List.of(author2));
        ResponseEntity<List<AuthorDTO>> response = authorController.findAuthorsByLastName("Borges");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("Borges", response.getBody().get(0).getLastName());
    }

    /**
     * Test to verify that the search by last name returns an empty list when there
     * are no matches.
     */
    @Test
    @DisplayName("Should return empty list when there are no last name matches")
    void searchAuthorsByLastName_ShouldReturnEmptyList_WhenNoMatch() {
        when(authorService.findAuthorsByLastName("NoExiste")).thenReturn(Collections.emptyList());
        ResponseEntity<List<AuthorDTO>> response = authorController.findAuthorsByLastName("NoExiste");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, response.getBody().size());
    }

    /**
     * Test to verify the search for authors by book ID.
     */
    @Test
    @DisplayName("Should return authors associated with the book")
    void searchAuthorsByBook_ShouldReturnAuthors_WhenBookHasAuthors() {
        when(authorService.findAuthorsByBookId(10L)).thenReturn(List.of(author1));
        ResponseEntity<List<AuthorDTO>> response = authorController.findAuthorsByBookId(10L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    /**
     * Test to verify that the search by book returns an empty list when there are
     * no associated authors.
     */
    @Test
    @DisplayName("Should return empty list when there are no authors associated with the book")
    void searchAuthorsByBook_ShouldReturnEmptyList_WhenNoAuthorsForBook() {
        when(authorService.findAuthorsByBookId(99L)).thenReturn(Collections.emptyList());
        ResponseEntity<List<AuthorDTO>> response = authorController.findAuthorsByBookId(99L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, response.getBody().size());
    }
}