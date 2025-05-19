package com.books.application.mapper;

import com.books.application.dto.AuthorDTO;
import com.books.application.dto.CreateAuthorDTO;
import com.books.application.dto.UpdateAuthorDTO;
import com.books.domain.model.Author;
import com.books.domain.model.Book;
import org.mapstruct.*;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mapper for converting between domain Author entities and their
 * corresponding DTOs.
 * Uses MapStruct to automatically generate implementations of the
 * mapping methods.
 *
 * @author books
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AuthorMapper {

    /**
     * Converts an Author entity to an AuthorDTO.
     * Includes custom mapping for fullName and bookIds.
     *
     * @param author the Author entity to convert
     * @return the resulting AuthorDTO
     */
    @Mapping(target = "fullName", expression = "java(author.getFullName())")
    @Mapping(target = "bookBookIds", expression = "java(mapBookBookIdsFromAuthor(author))")
    AuthorDTO toDto(Author author);

    /**
     * Converts a CreateAuthorDTO to an Author entity.
     *
     * @param createAuthorDTO the creation DTO to convert
     * @return the resulting Author entity
     */
    Author toEntity(CreateAuthorDTO createAuthorDTO);

    /**
     * Updates an existing Author entity with data from an UpdateAuthorDTO.
     * Only non-null fields from the DTO will be updated.
     *
     * @param updateAuthorDTO the DTO with updated data
     * @param author          the Author entity to update
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(UpdateAuthorDTO updateAuthorDTO, @MappingTarget Author author);

    /**
     * Helper method to extract book IDs associated with an author.
     *
     * @param author the Author entity from which to extract book IDs
     * @return a set of book IDs
     */
    default Set<Book> mapBookBookIdsFromAuthor(Author author) {
        if (author.getBooks() == null) {
            return Set.of();
        }
        return author.getBooks();
    }
}