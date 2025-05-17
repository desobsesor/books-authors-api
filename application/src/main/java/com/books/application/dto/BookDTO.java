package com.books.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * DTO (Data Transfer Object) for the Book entity.
 * Used to transfer book data between the application layer and the
 * API.
 *
 * @author books
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
    private Set<Long> authorIds = new HashSet<>();
}