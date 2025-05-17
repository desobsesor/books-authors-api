package com.books.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Set;

/**
 * DTO (Data Transfer Object) to create a new book.
 * Contains validations to ensure data integrity.
 *
 * @author books
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Data for creating a new book")
public class CreateBookDTO {

    @NotBlank(message = "Title is required")
    @Size(min = 1, max = 255, message = "Title must be between 1 and 255 characters")
    @Schema(description = "Book title", example = "One Hundred Years of Solitude", required = true)
    private String title;

    @NotBlank(message = "ISBN is required")
    @Size(min = 10, max = 13, message = "ISBN must be between 10 and 13 characters")
    @Schema(description = "Book ISBN", example = "9780307474728", required = true)
    private String isbn;

    @PastOrPresent(message = "Publication date must be in the past or present")
    @Schema(description = "Book publication date", example = "1967-05-30")
    private LocalDate publicationDate;

    @Schema(description = "Book publisher")
    @Size(min = 1, max = 255, message = "Publisher must be between 1 and 255 characters")
    private String publisher;

    @Schema(description = "Book genre")
    @Size(min = 1, max = 255, message = "Genre must be between 1 and 255 characters")
    private String genre;

    @Schema(description = "Book summary")
    @Size(min = 1, max = 255, message = "Summary must be between 1 and 255 characters")
    private String summary;

    @Schema(description = "IDs of authors associated with this book")
    private Set<Long> authorIds;
}