package com.books.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Set;

/**
 * DTO (Data Transfer Object) for updating an existing book.
 * Contains validations to ensure data integrity.
 * All fields are optional since only the provided fields
 * will be updated.
 *
 * @author books
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Data for updating an existing book")
public class UpdateBookDTO {
    @Schema(description = "Book ID", example = "1")
    private Long bookId;

    @Size(min = 1, max = 255, message = "Title must be between 1 and 255 characters")
    @Schema(description = "Book title", example = "One Hundred Years of Solitude")
    private String title;

    @Size(min = 10, max = 13, message = "ISBN must be between 10 and 13 characters")
    @Schema(description = "Book ISBN", example = "9780307474728")
    private String isbn;

    @PastOrPresent(message = "Publication date must be in the past or present")
    @Schema(description = "Book publication date", example = "1967-05-30")
    private LocalDate publicationDate;

    @Schema(description = "IDs of authors associated with this book")
    private Set<Long> authorIds;
}