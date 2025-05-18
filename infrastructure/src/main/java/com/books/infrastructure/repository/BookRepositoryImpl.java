package com.books.infrastructure.repository;

import java.util.Collections;

import com.books.domain.model.Author;
import com.books.domain.model.Book;
import com.books.domain.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    private Book mapRowToBook(ResultSet rs, int rowNum) throws SQLException {
        return Book.builder()
                .bookId(rs.getLong("BOOK_ID"))
                .title(rs.getString("TITLE"))
                .isbn(rs.getString("ISBN"))
                .publicationDate(rs.getDate("PUBLICATION_DATE").toLocalDate())
                .publisher(rs.getString("PUBLISHER"))
                .genre(rs.getString("GENRE"))
                .summary(rs.getString("SUMMARY"))
                .authors(new HashSet<>(Collections.singleton(Author.builder()
                        .authorId(rs.getLong("AUTHOR_ID"))
                        .firstName(rs.getString("AUTHOR_FIRST_NAME"))
                        .lastName(rs.getString("AUTHOR_LAST_NAME"))
                        .birthDate(
                                rs.getDate("AUTHOR_BIRTH_DATE") != null ? rs.getDate("AUTHOR_BIRTH_DATE").toLocalDate()
                                        : null)
                        .biography(rs.getString("AUTHOR_BIOGRAPHY"))
                        .build())))
                .build();
    }

    @Override
    public List<Book> findAll() {
        // Call PL/SQL procedure: BOOK_PKG.GET_ALL_BOOKS
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName("BOOK_PKG")
                .withProcedureName("GET_ALL_BOOKS")
                .returningResultSet("p_books", this::mapRowToBook);

        Map<String, Object> result = jdbcCall.execute(new HashMap<>());
        if (result == null) {
            log.warn("Stored procedure returned null result");
            return List.of();
        }

        List<Book> books = (List<Book>) result.get("p_books");
        return books != null ? books : List.of();
    }

    @Override
    public Optional<Book> findById(Long id) {
        try {
            // Call PL/SQL procedure: BOOK_PKG.GET_BOOK_BY_ID
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withCatalogName("BOOK_PKG")
                    .withProcedureName("GET_BOOK_BY_ID")
                    .returningResultSet("p_book", this::mapRowToBook);

            Map<String, Object> inParams = new HashMap<>();
            inParams.put("p_book_id", id);

            Map<String, Object> result = jdbcCall.execute(inParams);
            List<Book> books = (List<Book>) result.get("p_book");

            return books.isEmpty() ? Optional.empty() : Optional.of(books.get(0));
        } catch (Exception e) {
            log.error("Error finding book with ID: {}", id, e);
            return Optional.empty();
        }
    }

    @Override
    public Book save(Book book) {
        // Call PL/SQL procedure: BOOK_PKG.SAVE_BOOK
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
        Long bookId = (Long) result.get("P_BOOK_ID");
        book.setBookId(bookId);

        // Save book-author relationships if authors exist
        if (!book.getAuthors().isEmpty()) {
            for (Author author : book.getAuthors()) {
                saveBookAuthorRelationship(book.getBookId(), author.getAuthorId());
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
        // Call PL/SQL procedure: BOOK_PKG.LINK_BOOK_AUTHOR
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
            // Call PL/SQL procedure: BOOK_PKG.DELETE_BOOK
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

            Number successNum = (Number) result.get("p_success");
            return successNum != null && successNum.intValue() == 1;
        } catch (Exception e) {
            log.error("Error deleting author with ID: {}", id, e);
            return false;
        }
    }

    @Override
    public List<Book> findByTitleContaining(String title) {
        // Call PL/SQL procedure: BOOK_PKG.FIND_BOOKS_BY_TITLE
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName("BOOK_PKG")
                .withProcedureName("FIND_BOOKS_BY_TITLE")
                .returningResultSet("p_books", this::mapRowToBook);

        Map<String, Object> inParams = new HashMap<>();
        inParams.put("P_TITLE", "%" + title + "%");

        Map<String, Object> result = jdbcCall.execute(inParams);
        return (List<Book>) result.get("p_books");
    }

    @Override
    public List<Book> findByGenre(String genre) {
        // Call PL/SQL procedure: BOOK_PKG.FIND_BOOKS_BY_GENRE
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName("BOOK_PKG")
                .withProcedureName("FIND_BOOKS_BY_GENRE")
                .returningResultSet("p_books", this::mapRowToBook);

        Map<String, Object> inParams = new HashMap<>();
        inParams.put("P_GENRE", genre);

        Map<String, Object> result = jdbcCall.execute(inParams);
        return (List<Book>) result.get("p_books");
    }

    @Override
    public List<Book> findByAuthorId(Long authorId) {
        // Call PL/SQL procedure: BOOK_PKG.FIND_BOOKS_BY_AUTHOR
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName("BOOK_PKG")
                .withProcedureName("FIND_BOOKS_BY_AUTHOR")
                .returningResultSet("p_books", this::mapRowToBook);

        Map<String, Object> inParams = new HashMap<>();
        inParams.put("P_AUTHOR_ID", authorId);

        Map<String, Object> result = jdbcCall.execute(inParams);
        return (List<Book>) result.get("p_books");
    }

    @Override
    public List<Book> findByPublicationYearBetween(int startYear, int endYear) {
        // Call PL/SQL procedure: BOOK_PKG.FIND_BOOKS_BY_YEAR_RANGE
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName("BOOK_PKG")
                .withProcedureName("FIND_BOOKS_BY_YEAR_RANGE")
                .returningResultSet("p_books", this::mapRowToBook);

        Map<String, Object> inParams = new HashMap<>();
        inParams.put("P_START_YEAR", startYear);
        inParams.put("P_END_YEAR", endYear);

        Map<String, Object> result = jdbcCall.execute(inParams);
        return (List<Book>) result.get("p_books");
    }
}