package com.books.infrastructure.repository;

import com.books.domain.model.Author;
import com.books.domain.repository.AuthorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
                    .build();
        }
    };

    @Override
    public List<Author> findAll() {
        log.debug("Getting all authors using stored procedure");

        try {
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withCatalogName("AUTHOR_PKG")
                    .withProcedureName("GET_ALL_AUTHORS")
                    .returningResultSet("p_authors", AUTHOR_ROW_MAPPER);

            Map<String, Object> result = jdbcCall.execute(new HashMap<>());
            if (result == null) {
                log.warn("Stored procedure returned null result");
                return List.of();
            }

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
            // Convert INTEGER (1/0) to Boolean (true/false)
            Number successNum = (Number) result.get("p_success");
            return successNum != null && successNum.intValue() == 1;
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
        List<Author> authors = (List<Author>) result.get("p_authors");
        return authors != null ? authors : List.of();
    }

    @Override
    /**
     * Finds authors who have written books in the given genre.
     *
     * @param genre the genre to search for
     * @return a list of authors who have written books in the given genre
     */
    public List<Author> findByBookGenre(String genre) {
        log.debug("Finding authors by book genre: {}", genre);

        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName("AUTHOR_PKG")
                .withProcedureName("FIND_AUTHORS_BY_BOOK_GENRE")
                .returningResultSet("p_authors", AUTHOR_ROW_MAPPER);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("p_genre", genre);

        Map<String, Object> result = jdbcCall.execute(params);
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
        List<Author> authors = (List<Author>) result.get("p_authors");
        return authors != null ? authors : List.of();
    }
}