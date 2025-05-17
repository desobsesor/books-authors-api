# Unit Tests for REST Controllers

## Description

This directory contains the unit tests for the REST controllers of the Books and Authors API. The tests are implemented using JUnit 5, Spring Boot Test, and Mockito.

## Test Structure

The tests follow the AAA pattern (Arrange, Act, Assert) and are organized using nested classes (`@Nested`) to group test cases by endpoint.

### AuthorControllerTest

The `AuthorControllerTest` class provides full coverage for all endpoints of the `AuthorController`:

- **GET /api/authors**: Tests the retrieval of all authors
  - Positive case: Returns a list of authors
  - Edge case: Returns an empty list when there are no authors

- **GET /api/authors/{id}**: Tests the retrieval of an author by ID
  - Positive case: Returns author when exists
  - Negative case: Returns 404 when not exists

- **POST /api/authors**: Tests the creation of a new author
  - Positive case: Creates author with valid data
  - Negative case: Returns 400 with invalid data

- **PUT /api/authors/{id}**: Tests the update of an existing author
  - Positive case: Updates author when exists
  - Negative case: Returns 404 when not exists
  - Negative case: Returns 400 with invalid data

- **DELETE /api/authors/{id}**: Tests the deletion of an author
  - Positive case: Deletes author when exists
  - Negative case: Returns 404 when not exists

- **GET /api/authors/search/by-lastname**: Tests the search for authors by last name
  - Positive case: Returns matching authors
  - Edge case: Returns an empty list when there are no matches

- **GET /api/authors/search/by-book/{bookId}**: Tests the search for authors by book ID
  - Positive case: Returns authors associated with the book
  - Edge case: Returns an empty list when there are no associated authors

## Technologies Used

- **JUnit 5**: Testing framework
- **Mockito**: Mocking framework
- **MockMvc**: To simulate HTTP requests
- **Spring Boot Test**: Support for testing in Spring Boot
- **Hamcrest**: For expressive assertions

## Running the Tests

The tests can be run with Maven:

```bash
mvn test -Dtest=*AuthorControllerTest
```