package com.books.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO (Data Transfer Object) for updating an existing author.
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
@Schema(description = "Data for updating an existing author")
public class UpdateAuthorDTO {

    @Size(min = 1, max = 100, message = "First name must be between 1 and 100 characters")
    @Schema(description = "Author's first name", example = "Gabriel")
    private String firstName;

    @Size(min = 1, max = 100, message = "Last name must be between 1 and 100 characters")
    @Schema(description = "Author's last name", example = "García Márquez")
    private String lastName;

    @Past(message = "Birth date must be in the past")
    @Schema(description = "Author's birth date", example = "1927-03-06")
    private LocalDate birthDate;

    @Size(max = 2000, message = "Biography cannot exceed 2000 characters")
    @Schema(description = "Author's biography", example = "Colombian writer, winner of the Nobel Prize in Literature in 1982...")
    private String biography;
}