package com.books.application.dto;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import com.books.domain.model.Author;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO (Data Transfer Object) for the Book entity.
 * Used to transfer book data between the application layer and the API.
 *
 * @author books-authors-api
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookDTO {
    private Long bookId;
    private String title;
    private String isbn;
    private LocalDate publicationDate;

    /**
     * IDs of authors associated with this book.
     * A Set is used to avoid duplicates.
     */
    @Builder.Default
    private Set<Author> authorIds = new HashSet<>();
}