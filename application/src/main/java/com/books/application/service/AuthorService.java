package com.books.application.service;

import com.books.application.dto.AuthorDTO;
import com.books.application.dto.CreateAuthorDTO;
import com.books.application.dto.UpdateAuthorDTO;
import com.books.application.mapper.AuthorMapper;
import com.books.domain.model.Author;
import com.books.domain.repository.AuthorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service that implements the business logic related to authors.
 * Acts as an intermediate layer between API controllers and
 * domain repositories.
 *
 * @author books
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthorService {

    private final AuthorRepository authorRepository;
    private final AuthorMapper authorMapper;

    /**
     * Gets all authors from the system.
     *
     * @return list of author DTOs
     */
    @Transactional(readOnly = true)
    public List<AuthorDTO> getAllAuthors() {
        log.debug("Getting all authors");
        return authorRepository.findAll().stream()
                .map(authorMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Gets an author by their ID.
     *
     * @param id the ID of the author to search for
     * @return an Optional with the author DTO if found, or empty if not
     */
    @Transactional(readOnly = true)
    public Optional<AuthorDTO> getAuthorById(Long id) {
        log.debug("Getting author with ID: {}", id);
        return authorRepository.findById(id)
                .map(authorMapper::toDto);
    }

    /**
     * Creates a new author in the system.
     *
     * @param createAuthorDTO the data to create the author
     * @return the created author DTO
     */
    public AuthorDTO createAuthor(CreateAuthorDTO createAuthorDTO) {
        log.debug("Creating new author: {}", createAuthorDTO);
        Author author = authorMapper.toEntity(createAuthorDTO);
        Author savedAuthor = authorRepository.save(author);
        return authorMapper.toDto(savedAuthor);
    }

    /**
     * Updates an existing author.
     *
     * @param id              the ID of the author to update
     * @param updateAuthorDTO the updated author data
     * @return an Optional with the updated author DTO if found, or empty
     *         if not
     */
    public Optional<AuthorDTO> updateAuthor(Long id, UpdateAuthorDTO updateAuthorDTO) {
        log.debug("Updating author with ID: {}", id);
        return authorRepository.findById(id)
                .map(author -> {
                    authorMapper.updateEntityFromDto(updateAuthorDTO, author);
                    Author updatedAuthor = authorRepository.save(author);
                    return authorMapper.toDto(updatedAuthor);
                });
    }

    /**
     * Deletes an author by their ID.
     *
     * @param id the ID of the author to delete
     * @return true if the author was deleted, false if not found
     */
    public boolean deleteAuthor(Long id) {
        log.debug("Deleting author with ID: {}", id);
        return authorRepository.deleteById(id);
    }

    /**
     * Searches for authors by last name.
     *
     * @param lastName the last name to search for
     * @return list of author DTOs that match the last name
     */
    @Transactional(readOnly = true)
    public List<AuthorDTO> findAuthorsByLastName(String lastName) {
        log.debug("Searching for authors with last name: {}", lastName);
        return authorRepository.findByLastName(lastName).stream()
                .map(authorMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Searches for authors by book ID.
     *
     * @param bookId the book ID
     * @return list of author DTOs associated with the book
     */
    @Transactional(readOnly = true)
    public List<AuthorDTO> findAuthorsByBookId(Long bookId) {
        log.debug("Searching for authors by book ID: {}", bookId);
        return authorRepository.findByBookId(bookId).stream()
                .map(authorMapper::toDto)
                .collect(Collectors.toList());
    }
}