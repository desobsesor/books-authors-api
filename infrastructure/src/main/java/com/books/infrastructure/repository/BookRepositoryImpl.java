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
import com.books.domain.repository.BookRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of the BookRepository interface using PL/SQL procedures.
 * This class uses Spring JDBC to call Oracle stored procedures and functions.
 *
 * @author books
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class BookRepositoryImpl implements BookRepository {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Maps a database row to a Book entity.
     *
     * @param rs     the ResultSet containing the database row
     * @param rowNum the row number
     * @return a Book entity populated with data from the database
     * @throws SQLException if an error occurs while accessing the database
     */
    private static final RowMapper<Book> BOOK_ROW_MAPPER = new RowMapper<Book>() {
        @Override
        public Book mapRow(ResultSet rs, int rowNum) throws SQLException {
            return Book.builder()
                    .bookId(rs.getLong("BOOK_ID"))
                    .title(rs.getString("TITLE"))
                    .isbn(rs.getString("ISBN"))
                    .publicationDate(rs.getDate("PUBLICATION_DATE").toLocalDate())
                    .publisher(rs.getString("PUBLISHER"))
                    .genre(rs.getString("GENRE"))
                    .summary(rs.getString("SUMMARY"))
                    .authors(rs.getString("authors_json") != null
                            ? parseAuthorsJson(rs.getString("authors_json"))
                            : new HashSet<>(Collections.singleton(Author.builder()
                                    .authorId(rs.getLong("authorId"))
                                    .firstName(rs.getString("firstName"))
                                    .lastName(rs.getString("lastName"))
                                    .birthDate(
                                            rs.getDate("birthDate") != null ? rs.getDate("birthDate").toLocalDate()
                                                    : null)
                                    .biography(rs.getString("biography"))
                                    .build())))
                    .build();
        }

        private Set<Author> parseAuthorsJson(String json) {
            try {
                ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
                return mapper.readValue(json, new TypeReference<HashSet<Author>>() {
                });
            } catch (Exception e) {
                log.error("Error parsing authors JSON", e);
                return new HashSet<>();
            }
        }
    };

    @Override
    public List<Book> findAll(int page, int size) {
        log.debug("Getting all books with pagination using stored procedure");
        try {
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withCatalogName("BOOK_PKG")
                    .withProcedureName("GET_ALL_BOOKS")
                    .returningResultSet("p_books", BOOK_ROW_MAPPER);

            MapSqlParameterSource params = new MapSqlParameterSource()
                    .addValue("p_page_number", page)
                    .addValue("p_page_size", size);

            Map<String, Object> result = jdbcCall.execute(params);
            if (result == null) {
                log.warn("Stored procedure returned null result");
                return List.of();
            }

            @SuppressWarnings("unchecked")
            List<Book> books = (List<Book>) result.get("p_books");
            return books != null ? books : List.of();
        } catch (Exception e) {
            log.error("Error retrieving all authors", e);
            return List.of();
        }
    }

    @Override
    public Optional<Book> findById(Long id) {
        try {
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withCatalogName("BOOK_PKG")
                    .withProcedureName("GET_BOOK_BY_ID")
                    .returningResultSet("p_book", BOOK_ROW_MAPPER);

            Map<String, Object> inParams = new HashMap<>();
            inParams.put("p_book_id", id);

            Map<String, Object> result = jdbcCall.execute(inParams);
            @SuppressWarnings("unchecked")
            List<Book> books = (List<Book>) result.get("p_book");

            return books.isEmpty() ? Optional.empty() : Optional.of(books.get(0));
        } catch (Exception e) {
            log.error("Error finding book with ID: {}", id, e);
            return Optional.empty();
        }
    }

    @Override
    public Book save(Book book) {
        log.debug("Saving book: {}", book);

        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName("BOOK_PKG")
                .withProcedureName("SAVE_BOOK");

        Map<String, Object> inParams = new HashMap<>();
        inParams.put("P_BOOK_ID", book.getBookId());
        inParams.put("P_TITLE", book.getTitle());
        inParams.put("P_ISBN", book.getIsbn());
        inParams.put("P_PUBLICATION_DATE", java.sql.Date.valueOf(book.getPublicationDate()));
        inParams.put("P_PUBLISHER", book.getPublisher());
        inParams.put("P_GENRE", book.getGenre());
        inParams.put("P_SUMMARY", book.getSummary());

        Map<String, Object> result = jdbcCall.execute(inParams);
        Long bookId = ((Number) result.get("P_BOOK_ID")).longValue();
        book.setBookId(bookId);

        // Save author-book relationships if authorIds exist
        if (book.getAuthorIds() != null && !book.getAuthorIds().isEmpty()) {
            for (Long authorId : book.getAuthorIds()) {
                saveBookAuthorRelationship(book.getBookId(), authorId);
            }
        }

        return book;
    }

    /**
     * Saves a relationship between a book and an author.
     *
     * @param bookId   the ID of the book
     * @param authorId the ID of the author
     */
    private void saveBookAuthorRelationship(Long bookId, Long authorId) {
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName("BOOK_PKG")
                .withProcedureName("LINK_BOOK_AUTHOR");

        Map<String, Object> inParams = new HashMap<>();
        inParams.put("P_BOOK_ID", bookId);
        inParams.put("P_AUTHOR_ID", authorId);

        jdbcCall.execute(inParams);
    }

    @Override
    public boolean deleteById(Long id) {
        log.debug("Deleting book with ID: {}", id);
        try {
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withCatalogName("BOOK_PKG")
                    .withProcedureName("DELETE_BOOK")
                    .declareParameters(
                            new org.springframework.jdbc.core.SqlParameter("p_book_id", java.sql.Types.NUMERIC),
                            new org.springframework.jdbc.core.SqlOutParameter("p_success", java.sql.Types.BOOLEAN));

            MapSqlParameterSource params = new MapSqlParameterSource()
                    .addValue("p_book_id", id)
                    .addValue("p_success", null);

            Map<String, Object> result = jdbcCall.execute(params);
            return Boolean.parseBoolean(result.get("p_success").toString());
        } catch (Exception e) {
            log.error("Error deleting book with ID: {}", id, e);
            return false;
        }
    }

    @Override
    public List<Book> findByTitleContaining(String title) {
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName("BOOK_PKG")
                .withProcedureName("FIND_BOOKS_BY_TITLE")
                .returningResultSet("p_books", BOOK_ROW_MAPPER);

        Map<String, Object> inParams = new HashMap<>();
        inParams.put("P_TITLE", "%" + title + "%");

        Map<String, Object> result = jdbcCall.execute(inParams);
        @SuppressWarnings("unchecked")
        List<Book> books = (List<Book>) result.get("p_books");
        return books != null ? books : List.of();
    }

    @Override
    public List<Book> findByGenre(String genre) {
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName("BOOK_PKG")
                .withProcedureName("FIND_BOOKS_BY_GENRE")
                .returningResultSet("p_books", BOOK_ROW_MAPPER);

        Map<String, Object> inParams = new HashMap<>();
        inParams.put("P_GENRE", genre);

        Map<String, Object> result = jdbcCall.execute(inParams);
        @SuppressWarnings("unchecked")
        List<Book> books = (List<Book>) result.get("p_books");
        return books != null ? books : List.of();
    }

    @Override
    public List<Book> findByAuthorId(Long authorId) {
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName("BOOK_PKG")
                .withProcedureName("FIND_BOOKS_BY_AUTHOR")
                .returningResultSet("p_books", BOOK_ROW_MAPPER);

        Map<String, Object> inParams = new HashMap<>();
        inParams.put("P_AUTHOR_ID", authorId);

        Map<String, Object> result = jdbcCall.execute(inParams);
        @SuppressWarnings("unchecked")
        List<Book> books = (List<Book>) result.get("p_books");
        return books != null ? books : List.of();
    }

    @Override
    public List<Book> findByPublicationYearBetween(int startYear, int endYear) {
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName("BOOK_PKG")
                .withProcedureName("FIND_BOOKS_BY_YEAR_RANGE")
                .returningResultSet("p_books", BOOK_ROW_MAPPER);

        Map<String, Object> inParams = new HashMap<>();
        inParams.put("P_START_YEAR", startYear);
        inParams.put("P_END_YEAR", endYear);

        Map<String, Object> result = jdbcCall.execute(inParams);
        @SuppressWarnings("unchecked")
        List<Book> books = (List<Book>) result.get("p_books");
        return books != null ? books : List.of();
    }
}