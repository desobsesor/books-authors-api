package com.books.application.dto;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import com.books.domain.model.Book;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO (Data Transfer Object) for the Author entity.
 * Used to transfer author data between the application layer and the API.
 *
 * @author books-authors-api
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthorDTO {
    private Long authorId;
    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private String biography;

    /**
     * Author's full name, combining first name and last name.
     */
    private String fullName;

    /**
     * IDs of books associated with this author.
     * A Set is used to avoid duplicates.
     */
    @Builder.Default
    private Set<Book> bookBookIds = new HashSet<>();
}