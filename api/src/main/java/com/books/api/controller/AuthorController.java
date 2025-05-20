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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.books.api.security.JwtTokenProvider;
import com.books.application.dto.AuthorDTO;
import com.books.application.dto.CreateAuthorDTO;
import com.books.application.dto.UpdateAuthorDTO;
import com.books.application.service.AuthorService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST controller for author-related operations.
 * Exposes endpoints to create, read, update and delete authors.
 *
 * @author books-authors-api
 */
@RestController
@RequestMapping("${CONTEXT_PATH}/authors")
@SecurityRequirement(name = "bearer-key")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "authors", description = "Authors management API")
public class AuthorController {

        private final AuthorService authorService;
        private final JwtTokenProvider jwtTokenProvider;

        /**
         * Gets all authors.
         * Pagination is supported.
         * By default, 10 authors per page are returned.
         * Can be customized using query parameters:
         * - page: page number (default: 0)
         * - size: number of authors per page (default: 10)
         *
         * @return list of authors
         */
        @GetMapping
        @Operation(summary = "Get all authors", description = "Retrieves a paginated list of all authors available in the system")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Authors found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthorDTO.class)))
        })
        public ResponseEntity<List<AuthorDTO>> getAllAuthors(
                        @RequestParam(defaultValue = "1") int page,
                        @RequestParam(defaultValue = "10") int size) {
                log.debug("REST request to get all authors with pagination: page={}, size={}", page, size);
                List<AuthorDTO> authors = authorService.getAllAuthors(page, size);
                return ResponseEntity.ok(authors);
        }

        @PostMapping("/generate-token")
        @Operation(summary = "Generate JWT token", description = "Generates a valid JWT token for testing purposes")
        @ApiResponse(responseCode = "200", description = "Token generated successfully")
        public ResponseEntity<String> generateToken() {
                return ResponseEntity.ok(jwtTokenProvider.createToken());
        }

        /**
         * Gets an author by their ID.
         *
         * @param id the ID of the author to search for
         * @return the found author or 404 if not exists
         */
        @GetMapping("/{id}")
        @Operation(summary = "Get an author by ID", description = "Retrieves a specific author based on their ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Author found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthorDTO.class))),
                        @ApiResponse(responseCode = "404", description = "Author not found", content = @Content)
        })
        public ResponseEntity<AuthorDTO> getAuthorById(
                        @Parameter(description = "ID of the author to search for", required = true) @PathVariable Long id) {
                log.debug("REST request to get author with ID: {}", id);
                return authorService.getAuthorById(id)
                                .map(ResponseEntity::ok)
                                .orElse(ResponseEntity.notFound().build());
        }

        /**
         * Creates a new author.
         *
         * @param createAuthorDTO the data to create the author
         * @return the created author
         */
        @PostMapping
        @ResponseStatus(HttpStatus.CREATED)
        @Operation(summary = "Create a new author", description = "Creates a new author with the provided data")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Author created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthorDTO.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid author data", content = @Content)
        })
        public ResponseEntity<AuthorDTO> createAuthor(
                        @Parameter(description = "Data to create the author", required = true) @Valid @RequestBody CreateAuthorDTO createAuthorDTO) {
                log.debug("REST request to create a new author: {}", createAuthorDTO);
                AuthorDTO result = authorService.createAuthor(createAuthorDTO);
                return ResponseEntity.status(HttpStatus.CREATED).body(result);
        }

        /**
         * Updates an existing author.
         *
         * @param id              the ID of the author to update
         * @param updateAuthorDTO the updated author data
         * @return the updated author or 404 if not exists
         */
        @PutMapping("/{id}")
        @Operation(summary = "Update an author", description = "Updates an existing author with the provided data")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Author updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthorDTO.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid author data", content = @Content),
                        @ApiResponse(responseCode = "404", description = "Author not found", content = @Content)
        })
        public ResponseEntity<AuthorDTO> updateAuthor(
                        @Parameter(description = "ID of the author to update", required = true) @PathVariable Long id,
                        @Parameter(description = "Updated author data", required = true) @Valid @RequestBody UpdateAuthorDTO updateAuthorDTO) {
                log.debug("REST request to update author with ID: {}", id);
                return authorService.updateAuthor(id, updateAuthorDTO)
                                .map(ResponseEntity::ok)
                                .orElse(ResponseEntity.notFound().build());
        }

        /**
         * Deletes an author by their ID.
         *
         * @param id the ID of the author to delete
         * @return 204 if deleted successfully, 404 if not exists
         */
        @DeleteMapping("/{id}")
        @Operation(summary = "Delete an author", description = "Deletes an existing author by their ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "204", description = "Author deleted successfully"),
                        @ApiResponse(responseCode = "404", description = "Author not found", content = @Content)
        })
        public ResponseEntity<Void> deleteAuthor(
                        @Parameter(description = "ID of the author to delete", required = true) @PathVariable Long id) {
                log.debug("REST request to delete author with ID: {}", id);
                boolean deleted = authorService.deleteAuthor(id);
                return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
        }

        /**
         * Searches authors by last name.
         *
         * @param lastName the last name to search for
         * @return list of authors matching the last name
         */
        @GetMapping("/search/by-lastname")
        @Operation(summary = "Search authors by last name", description = "Retrieves authors matching the provided last name")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Search completed successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthorDTO.class)))
        })
        public ResponseEntity<List<AuthorDTO>> findAuthorsByLastName(
                        @Parameter(description = "Last name to search for", required = true) @RequestParam String lastName) {
                log.debug("REST request to search authors with last name: {}", lastName);
                List<AuthorDTO> authors = authorService.findAuthorsByLastName(lastName);
                return ResponseEntity.ok(authors);
        }

        /**
         * Searches authors by book ID.
         *
         * @param bookId the ID of the book
         * @return list of authors associated with the book
         */
        @GetMapping("/search/by-book/{bookId}")
        @Operation(summary = "Search authors by book ID", description = "Retrieves authors associated with a specific book")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Search completed successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthorDTO.class)))
        })
        public ResponseEntity<List<AuthorDTO>> findAuthorsByBookId(
                        @Parameter(description = "ID of the book", required = true) @PathVariable Long bookId) {
                log.debug("REST request to search authors by book ID: {}", bookId);
                List<AuthorDTO> authors = authorService.findAuthorsByBookId(bookId);
                return ResponseEntity.ok(authors);
        }
}