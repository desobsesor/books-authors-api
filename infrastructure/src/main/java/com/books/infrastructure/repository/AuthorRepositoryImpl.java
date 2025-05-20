package com.books.infrastructure.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import com.books.domain.model.Author;
import com.books.domain.model.Book;
import com.books.domain.repository.AuthorRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of the author repository that uses PL/SQL stored procedures.
 * This class implements the AuthorRepository domain interface and translates
 * operations into calls to stored procedures in the Oracle database.
 *
 * @author books
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class AuthorRepositoryImpl implements AuthorRepository {

    private final JdbcTemplate jdbcTemplate;

    /**
     * RowMapper to convert ResultSet rows into Author objects.
     */
    private static final RowMapper<Author> AUTHOR_ROW_MAPPER = new RowMapper<Author>() {
        @Override
        public Author mapRow(ResultSet rs, int rowNum) throws SQLException {
            return Author.builder()
                    .authorId(rs.getLong("author_id"))
                    .firstName(rs.getString("first_name"))
                    .lastName(rs.getString("last_name"))
                    .birthDate(rs.getDate("birth_date") != null ? rs.getDate("birth_date").toLocalDate() : null)
                    .biography(rs.getString("biography"))
                    .books(rs.getString("books_json") != null
                            ? parseBooksJson(rs.getString("books_json"))
                            : new HashSet<>(Collections.singletonList(Book.builder()
                                    .bookId(rs.getLong("bookId"))
                                    .title(rs.getString("title"))
                                    .isbn(rs.getString("isbn"))
                                    .publicationDate(rs.getString("publicationDate") != null
                                            ? rs.getDate("publicationDate").toLocalDate()
                                            : null)
                                    .publisher(rs.getString("publisher"))
                                    .genre(rs.getString("genre"))
                                    .summary(rs.getString("summary"))
                                    .build())))
                    .build();
        }

        private Set<Book> parseBooksJson(String json) {
            try {
                ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
                return mapper.readValue(json, new TypeReference<HashSet<Book>>() {
                });
            } catch (Exception e) {
                log.error("Error parsing books JSON", e);
                return new HashSet<>();
            }
        }
    };

    @Override
    public List<Author> findAll(int page, int size) {
        log.debug("Getting all authors with pagination using stored procedure");

        try {
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withCatalogName("AUTHOR_PKG")
                    .withProcedureName("GET_ALL_AUTHORS")
                    .returningResultSet("p_authors", AUTHOR_ROW_MAPPER);

            MapSqlParameterSource params = new MapSqlParameterSource()
                    .addValue("p_page_number", page)
                    .addValue("p_page_size", size);

            Map<String, Object> result = jdbcCall.execute(params);
            if (result == null) {
                log.warn("Stored procedure returned null result");
                return List.of();
            }

            @SuppressWarnings("unchecked")
            List<Author> authors = (List<Author>) result.get("p_authors");
            return authors != null ? authors : List.of();
        } catch (Exception e) {
            log.error("Error retrieving all authors", e);
            return List.of();
        }
    }

    @Override
    public Optional<Author> findById(Long id) {
        log.debug("Finding author with ID: {}", id);

        try {
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withCatalogName("AUTHOR_PKG")
                    .withProcedureName("GET_AUTHOR_BY_ID")
                    .returningResultSet("p_author", AUTHOR_ROW_MAPPER);

            MapSqlParameterSource params = new MapSqlParameterSource()
                    .addValue("p_author_id", id);

            Map<String, Object> result = jdbcCall.execute(params);
            @SuppressWarnings("unchecked")
            List<Author> authors = (List<Author>) result.get("p_author");

            return authors.isEmpty() ? Optional.empty() : Optional.of(authors.get(0));
        } catch (Exception e) {
            log.error("Error finding author with ID: {}", id, e);
            return Optional.empty();
        }
    }

    @Override
    public Author save(Author author) {
        log.debug("Saving author: {}", author);

        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName("AUTHOR_PKG")
                .withProcedureName("SAVE_AUTHOR")
                .declareParameters(
                        new org.springframework.jdbc.core.SqlOutParameter("p_author_id", java.sql.Types.NUMERIC));

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("p_author_id", author.getAuthorId())
                .addValue("p_first_name", author.getFirstName())
                .addValue("p_last_name", author.getLastName())
                .addValue("p_birth_date", author.getBirthDate())
                .addValue("p_biography", author.getBiography());

        Map<String, Object> result = jdbcCall.execute(params);
        Long newId = ((Number) result.get("p_author_id")).longValue();
        author.setAuthorId(newId);

        return author;
    }

    @Override
    public boolean deleteById(Long id) {
        log.debug("Deleting author with ID: {}", id);

        try {
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withCatalogName("AUTHOR_PKG")
                    .withProcedureName("DELETE_AUTHOR")
                    .declareParameters(
                            new org.springframework.jdbc.core.SqlParameter("p_author_id", java.sql.Types.NUMERIC),
                            new org.springframework.jdbc.core.SqlOutParameter("p_success", java.sql.Types.BOOLEAN));

            MapSqlParameterSource params = new MapSqlParameterSource()
                    .addValue("p_author_id", id)
                    .addValue("p_success", null);

            Map<String, Object> result = jdbcCall.execute(params);
            return Boolean.parseBoolean(result.get("p_success").toString());
        } catch (Exception e) {
            log.error("Error deleting author with ID: {}", id, e);
            return false;
        }
    }

    @Override
    public List<Author> findByLastName(String lastName) {
        log.debug("Finding authors with last name: {}", lastName);
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName("AUTHOR_PKG")
                .withProcedureName("FIND_AUTHORS_BY_LAST_NAME")
                .returningResultSet("p_authors", AUTHOR_ROW_MAPPER);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("p_last_name", lastName);

        Map<String, Object> result = jdbcCall.execute(params);
        @SuppressWarnings("unchecked")
        List<Author> authors = (List<Author>) result.get("p_authors");
        return authors != null ? authors : List.of();
    }

    /**
     * Finds authors who have written books in the given genre.
     *
     * @param genre the genre to search for
     * @return a list of authors who have written books in the given genre
     */
    @Override
    public List<Author> findByBookGenre(String genre) {
        log.debug("Finding authors by book genre: {}", genre);

        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName("AUTHOR_PKG")
                .withProcedureName("FIND_AUTHORS_BY_BOOK_GENRE")
                .returningResultSet("p_authors", AUTHOR_ROW_MAPPER);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("p_genre", genre);

        Map<String, Object> result = jdbcCall.execute(params);
        @SuppressWarnings("unchecked")
        List<Author> authors = (List<Author>) result.get("p_authors");
        return authors != null ? authors : List.of();
    }

    @Override
    public List<Author> findByBookId(Long bookId) {
        log.debug("Finding authors by book ID: {}", bookId);

        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName("AUTHOR_PKG")
                .withProcedureName("FIND_AUTHORS_BY_BOOK_ID")
                .returningResultSet("p_authors", AUTHOR_ROW_MAPPER);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("p_book_id", bookId);

        Map<String, Object> result = jdbcCall.execute(params);
        @SuppressWarnings("unchecked")
        List<Author> authors = (List<Author>) result.get("p_authors");
        return authors != null ? authors : List.of();
    }
}