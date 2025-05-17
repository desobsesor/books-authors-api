package com.books.application.mapper;

import com.books.application.dto.BookDTO;
import com.books.application.dto.CreateBookDTO;
import com.books.application.dto.UpdateBookDTO;
import com.books.domain.model.Book;
import com.books.domain.model.Author;
import org.mapstruct.*;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mapper for converting between domain Book entities and their
 * corresponding DTOs.
 * Uses MapStruct to automatically generate implementations of the
 * mapping methods.
 *
 * @author books
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BookMapper {

    /**
     * Converts a Book entity to a BookDTO.
     * Includes custom mapping for authorIds.
     *
     * @param book the Book entity to convert
     * @return the resulting BookDTO
     */
    @Mapping(target = "authorIds", expression = "java(mapAuthorIdsFromBook(book))")
    BookDTO toDto(Book book);

    /**
     * Converts a CreateBookDTO to a Book entity.
     *
     * @param createBookDTO the creation DTO to convert
     * @return the resulting Book entity
     */
    Book toEntity(CreateBookDTO createBookDTO);

    /**
     * Updates an existing Book entity with data from an UpdateBookDTO.
     * Only non-null fields from the DTO will be updated.
     *
     * @param updateBookDTO the DTO with updated data
     * @param book          the Book entity to update
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(UpdateBookDTO updateBookDTO, @MappingTarget Book book);

    /**
     * Helper method to extract author IDs associated with a book.
     *
     * @param book the Book entity from which to extract author IDs
     * @return a set of author IDs
     */
    default Set<Long> mapAuthorIdsFromBook(Book book) {
        if (book.getAuthors() == null) {
            return Set.of();
        }
        return book.getAuthors().stream()
                .map(Author::getAuthorId)
                .collect(Collectors.toSet());
    }
}