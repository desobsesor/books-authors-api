package com.books.application.dto;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO (Data Transfer Object) for creating a new author.
 * Contains validations to ensure data integrity.
 *
 * @author books
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Data for creating a new author")
public class CreateAuthorDTO {

    @NotBlank(message = "First name is required")
    @Size(min = 1, max = 100, message = "First name must be between 1 and 100 characters")
    @Schema(description = "Author's first name", example = "Gabriel", required = true)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 1, max = 100, message = "Last name must be between 1 and 100 characters")
    @Schema(description = "Author's last name", example = "García Márquez", required = true)
    private String lastName;

    @Past(message = "Birth date must be in the past")
    @Schema(description = "Author's birth date", example = "1927-03-06")
    private LocalDate birthDate;

    @Size(max = 2000, message = "Biography cannot exceed 2000 characters")
    @Schema(description = "Author's biography", example = "Colombian writer, winner of the Nobel Prize in Literature in 1982...")
    private String biography;
}