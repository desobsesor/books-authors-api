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
                    .id(rs.getLong("author_id"))
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

        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName("AUTHOR_PKG")
                .withProcedureName("GET_ALL_AUTHORS")
                .returningResultSet("authors", AUTHOR_ROW_MAPPER);

        Map<String, Object> result = jdbcCall.execute(new HashMap<>());
        return (List<Author>) result.get("authors");
    }

    @Override
    public Optional<Author> findById(Long id) {
        log.debug("Finding author with ID: {}", id);

        try {
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withCatalogName("AUTHOR_PKG")
                    .withProcedureName("GET_AUTHOR_BY_ID")
                    .returningResultSet("author", AUTHOR_ROW_MAPPER);

            MapSqlParameterSource params = new MapSqlParameterSource()
                    .addValue("p_author_id", id);

            Map<String, Object> result = jdbcCall.execute(params);
            List<Author> authors = (List<Author>) result.get("author");

            return authors.isEmpty() ? Optional.empty() : Optional.of(authors.get(0));
        } catch (Exception e) {
            log.error("Error finding author with ID: {}", id, e);
            return Optional.empty();
        }
    }

    @Override
    public Author save(Author author) {
        log.debug("Saving author: {}", author);

        SimpleJdbcCall jdbcCall;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("p_first_name", author.getFirstName())
                .addValue("p_last_name", author.getLastName())
                .addValue("p_birth_date", author.getBirthDate())
                .addValue("p_biography", author.getBiography());

        if (author.getId() == null) {
            // Create new author
            jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withCatalogName("AUTHOR_PKG")
                    .withProcedureName("CREATE_AUTHOR")
                    .withReturnValue();

            Map<String, Object> result = jdbcCall.execute(params);
            Long newId = ((Number) result.get("p_author_id")).longValue();
            author.setId(newId);
        } else {
            // Update existing author
            jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withCatalogName("AUTHOR_PKG")
                    .withProcedureName("UPDATE_AUTHOR");

            params.addValue("p_author_id", author.getId());
            jdbcCall.execute(params);
        }

        return author;
    }

    @Override
    public boolean deleteById(Long id) {
        log.debug("Deleting author with ID: {}", id);

        try {
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withCatalogName("AUTHOR_PKG")
                    .withProcedureName("DELETE_AUTHOR");

            MapSqlParameterSource params = new MapSqlParameterSource()
                    .addValue("p_author_id", id);

            jdbcCall.execute(params);
            return true;
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
                .withProcedureName("GET_AUTHORS_BY_LAST_NAME")
                .returningResultSet("authors", AUTHOR_ROW_MAPPER);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("p_last_name", lastName);

        Map<String, Object> result = jdbcCall.execute(params);
        return (List<Author>) result.get("authors");
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
                .withProcedureName("GET_AUTHORS_BY_BOOK_GENRE")
                .returningResultSet("authors", AUTHOR_ROW_MAPPER);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("p_genre", genre);

        Map<String, Object> result = jdbcCall.execute(params);
        return (List<Author>) result.get("authors");
    }

    @Override
    public List<Author> findByBookId(Long bookId) {
        log.debug("Finding authors by book ID: {}", bookId);

        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName("AUTHOR_PKG")
                .withProcedureName("GET_AUTHORS_BY_BOOK_ID")
                .returningResultSet("authors", AUTHOR_ROW_MAPPER);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("p_book_id", bookId);

        Map<String, Object> result = jdbcCall.execute(params);
        return (List<Author>) result.get("authors");
    }
}