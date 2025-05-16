package com.books.infrastructure.repository;

import com.books.domain.model.Author;
import com.books.domain.model.Book;
import com.books.domain.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
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
                .id(rs.getLong("BOOK_ID"))
                .title(rs.getString("TITLE"))
                .isbn(rs.getString("ISBN"))
                .publicationDate(rs.getDate("PUBLICATION_DATE").toLocalDate())
                .publisher(rs.getString("PUBLISHER"))
                .genre(rs.getString("GENRE"))
                .summary(rs.getString("SUMMARY"))
                .build();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Book> findAll() {
        // Call PL/SQL procedure: BOOK_PKG.GET_ALL_BOOKS
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName("BOOK_PKG")
                .withProcedureName("GET_ALL_BOOKS")
                .returningResultSet("BOOKS", this::mapRowToBook);

        Map<String, Object> result = jdbcCall.execute(new HashMap<>());
        return (List<Book>) result.get("BOOKS");
    }

    @Override
    public Optional<Book> findById(Long id) {
        try {
            // Call PL/SQL procedure: BOOK_PKG.GET_BOOK_BY_ID
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withCatalogName("BOOK_PKG")
                    .withProcedureName("GET_BOOK_BY_ID")
                    .returningResultSet("BOOK", this::mapRowToBook);

            Map<String, Object> inParams = new HashMap<>();
            inParams.put("P_BOOK_ID", id);

            Map<String, Object> result = jdbcCall.execute(inParams);
            List<Book> books = (List<Book>) result.get("BOOK");

            return books.isEmpty() ? Optional.empty() : Optional.of(books.get(0));
        } catch (Exception e) {
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
        inParams.put("P_BOOK_ID", book.getId());
        inParams.put("P_TITLE", book.getTitle());
        inParams.put("P_ISBN", book.getIsbn());
        inParams.put("P_PUBLICATION_DATE", java.sql.Date.valueOf(book.getPublicationDate()));
        inParams.put("P_PUBLISHER", book.getPublisher());
        inParams.put("P_GENRE", book.getGenre());
        inParams.put("P_SUMMARY", book.getSummary());

        Map<String, Object> result = jdbcCall.execute(inParams);
        Long bookId = (Long) result.get("P_BOOK_ID");
        book.setId(bookId);

        // Save book-author relationships if authors exist
        if (!book.getAuthors().isEmpty()) {
            for (Author author : book.getAuthors()) {
                saveBookAuthorRelationship(book.getId(), author.getId());
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
        // Call PL/SQL procedure: BOOK_PKG.DELETE_BOOK
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName("BOOK_PKG")
                .withProcedureName("DELETE_BOOK");

        Map<String, Object> inParams = new HashMap<>();
        inParams.put("P_BOOK_ID", id);
        inParams.put("P_SUCCESS", false);

        Map<String, Object> result = jdbcCall.execute(inParams);
        return (Boolean) result.get("P_SUCCESS");
    }

    @Override
    public List<Book> findByTitleContaining(String title) {
        // Call PL/SQL procedure: BOOK_PKG.FIND_BOOKS_BY_TITLE
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName("BOOK_PKG")
                .withProcedureName("FIND_BOOKS_BY_TITLE")
                .returningResultSet("BOOKS", this::mapRowToBook);

        Map<String, Object> inParams = new HashMap<>();
        inParams.put("P_TITLE", "%" + title + "%");

        Map<String, Object> result = jdbcCall.execute(inParams);
        return (List<Book>) result.get("BOOKS");
    }

    @Override
    public List<Book> findByGenre(String genre) {
        // Call PL/SQL procedure: BOOK_PKG.FIND_BOOKS_BY_GENRE
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName("BOOK_PKG")
                .withProcedureName("FIND_BOOKS_BY_GENRE")
                .returningResultSet("BOOKS", this::mapRowToBook);

        Map<String, Object> inParams = new HashMap<>();
        inParams.put("P_GENRE", genre);

        Map<String, Object> result = jdbcCall.execute(inParams);
        return (List<Book>) result.get("BOOKS");
    }

    @Override
    public List<Book> findByAuthorId(Long authorId) {
        // Call PL/SQL procedure: BOOK_PKG.FIND_BOOKS_BY_AUTHOR
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName("BOOK_PKG")
                .withProcedureName("FIND_BOOKS_BY_AUTHOR")
                .returningResultSet("BOOKS", this::mapRowToBook);

        Map<String, Object> inParams = new HashMap<>();
        inParams.put("P_AUTHOR_ID", authorId);

        Map<String, Object> result = jdbcCall.execute(inParams);
        return (List<Book>) result.get("BOOKS");
    }

    @Override
    public List<Book> findByPublicationYearBetween(int startYear, int endYear) {
        // Call PL/SQL procedure: BOOK_PKG.FIND_BOOKS_BY_YEAR_RANGE
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName("BOOK_PKG")
                .withProcedureName("FIND_BOOKS_BY_YEAR_RANGE")
                .returningResultSet("BOOKS", this::mapRowToBook);

        Map<String, Object> inParams = new HashMap<>();
        inParams.put("P_START_YEAR", startYear);
        inParams.put("P_END_YEAR", endYear);

        Map<String, Object> result = jdbcCall.execute(inParams);
        return (List<Book>) result.get("BOOKS");
    }
}