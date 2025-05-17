-- Create tables for books and authors
CREATE TABLE authors (
    author_id NUMBER PRIMARY KEY,
    first_name VARCHAR2(100) NOT NULL,
    last_name VARCHAR2(100) NOT NULL,
    birth_date DATE,
    biography CLOB
);

CREATE TABLE books (
    book_id NUMBER PRIMARY KEY,
    title VARCHAR2(255) NOT NULL,
    isbn VARCHAR2(20) UNIQUE NOT NULL,
    publication_date DATE,
    publisher VARCHAR2(100),
    genre VARCHAR2(50),
    summary CLOB
);

CREATE TABLE book_authors (
    book_id NUMBER,
    author_id NUMBER,
    PRIMARY KEY (book_id, author_id),
    FOREIGN KEY (book_id) REFERENCES books(book_id) ON DELETE CASCADE,
    FOREIGN KEY (author_id) REFERENCES authors(author_id) ON DELETE CASCADE
);

-- Create sequences for ID generation
CREATE SEQUENCE author_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE book_seq START WITH 1 INCREMENT BY 1;

-- Create package for Author operations
CREATE OR REPLACE PACKAGE AUTHOR_PKG AS
    -- Get all authors
    PROCEDURE GET_ALL_AUTHORS(
        p_authors OUT SYS_REFCURSOR
    );
    
    -- Get author by ID
    PROCEDURE GET_AUTHOR_BY_ID(
        p_author_id IN NUMBER,
        p_author OUT SYS_REFCURSOR
    );
    
    -- Save (create or update) an author
    PROCEDURE SAVE_AUTHOR(
        p_author_id IN OUT NUMBER,
        p_first_name IN VARCHAR2,
        p_last_name IN VARCHAR2,
        p_birth_date IN DATE,
        p_biography IN CLOB
    );
    
    -- Delete an author
    PROCEDURE DELETE_AUTHOR(
        p_author_id IN NUMBER,
        p_success OUT BOOLEAN
    );
    
    -- Find authors by last name
    PROCEDURE FIND_AUTHORS_BY_LAST_NAME(
        p_last_name IN VARCHAR2,
        p_authors OUT SYS_REFCURSOR
    );
    
    -- Find authors by book genre
    PROCEDURE FIND_AUTHORS_BY_BOOK_GENRE(
        p_genre IN VARCHAR2,
        p_authors OUT SYS_REFCURSOR
    );

    -- Find authors by book genre
    PROCEDURE FIND_AUTHORS_BY_BOOK_ID(
        p_book_id IN NUMBER,
        p_authors OUT SYS_REFCURSOR
    );
END AUTHOR_PKG;
/

CREATE OR REPLACE PACKAGE BODY AUTHOR_PKG AS
    -- Get all authors
    PROCEDURE GET_ALL_AUTHORS(
        p_authors OUT SYS_REFCURSOR
    ) IS
    BEGIN
        OPEN p_authors FOR
        SELECT a.author_id, a.first_name, a.last_name, a.birth_date, a.biography
        FROM authors a
        ORDER BY a.last_name, a.first_name;
    END GET_ALL_AUTHORS;
    
    -- Get author by ID
    PROCEDURE GET_AUTHOR_BY_ID(
        p_author_id IN NUMBER,
        p_author OUT SYS_REFCURSOR
    ) IS
    BEGIN
        OPEN p_author FOR
        SELECT a.author_id, a.first_name, a.last_name, a.birth_date, a.biography
        FROM authors a
        WHERE a.author_id = p_author_id;
    END GET_AUTHOR_BY_ID;
    
    -- Save (create or update) an author
    PROCEDURE SAVE_AUTHOR(
        p_author_id IN OUT NUMBER,
        p_first_name IN VARCHAR2,
        p_last_name IN VARCHAR2,
        p_birth_date IN DATE,
        p_biography IN CLOB
    ) IS
    BEGIN
        IF p_author_id IS NULL THEN
            -- Create new author
            SELECT author_seq.NEXTVAL INTO p_author_id FROM DUAL;
            
            INSERT INTO authors (author_id, first_name, last_name, birth_date, biography)
            VALUES (p_author_id, p_first_name, p_last_name, p_birth_date, p_biography);
        ELSE
            -- Update existing author
            UPDATE authors
            SET first_name = p_first_name,
                last_name = p_last_name,
                birth_date = p_birth_date,
                biography = p_biography
            WHERE author_id = p_author_id;
        END IF;
        
        COMMIT;
    EXCEPTION
        WHEN OTHERS THEN
            ROLLBACK;
            RAISE;
    END SAVE_AUTHOR;
    
    -- Delete an author
    PROCEDURE DELETE_AUTHOR(
        p_author_id IN NUMBER,
        p_success OUT BOOLEAN
    ) IS
        v_count NUMBER;
    BEGIN
        SELECT COUNT(*) INTO v_count FROM authors WHERE author_id = p_author_id;
        
        IF v_count > 0 THEN
            DELETE FROM authors WHERE author_id = p_author_id;
            p_success := TRUE;
            COMMIT;
        ELSE
            p_success := FALSE;
        END IF;
    EXCEPTION
        WHEN OTHERS THEN
            ROLLBACK;
            p_success := FALSE;
            RAISE;
    END DELETE_AUTHOR;
    
    -- Find authors by last name
    PROCEDURE FIND_AUTHORS_BY_LAST_NAME(
        p_last_name IN VARCHAR2,
        p_authors OUT SYS_REFCURSOR
    ) IS
    BEGIN
        OPEN p_authors FOR
        SELECT a.author_id, a.first_name, a.last_name, a.birth_date, a.biography
        FROM authors a
        WHERE UPPER(a.last_name) LIKE UPPER(p_last_name)
        ORDER BY a.last_name, a.first_name;
    END FIND_AUTHORS_BY_LAST_NAME;
    
    -- Find authors by book genre
    PROCEDURE FIND_AUTHORS_BY_BOOK_GENRE(
        p_genre IN VARCHAR2,
        p_authors OUT SYS_REFCURSOR
    ) IS
    BEGIN
        OPEN p_authors FOR
        SELECT DISTINCT a.author_id, a.first_name, a.last_name, a.birth_date, a.biography
        FROM authors a
        JOIN book_authors ba ON a.author_id = ba.author_id
        JOIN books b ON ba.book_id = b.book_id
        WHERE UPPER(b.genre) = UPPER(p_genre)
        ORDER BY a.last_name, a.first_name;
    END FIND_AUTHORS_BY_BOOK_GENRE;

    -- Find authors by book id
    PROCEDURE FIND_AUTHORS_BY_BOOK_ID(
        p_book_id IN NUMBER,
        p_authors OUT SYS_REFCURSOR
    ) IS
    BEGIN
        OPEN p_authors FOR
        SELECT DISTINCT a.author_id, a.first_name, a.last_name, a.birth_date, a.biography
        FROM authors a
        JOIN book_authors ba ON a.author_id = ba.author_id
        JOIN books b ON ba.book_id = b.book_id
        WHERE b.p_book_id = p_book_id
        ORDER BY a.last_name, a.first_name;
    END FIND_AUTHORS_BY_BOOK_ID;
END AUTHOR_PKG;
/

-- Create package for Book operations
CREATE OR REPLACE PACKAGE BOOK_PKG AS
    -- Get all books
    PROCEDURE GET_ALL_BOOKS(
        p_books OUT SYS_REFCURSOR
    );
    
    -- Get book by ID
    PROCEDURE GET_BOOK_BY_ID(
        p_book_id IN NUMBER,
        p_book OUT SYS_REFCURSOR
    );
    
    -- Save (create or update) a book
    PROCEDURE SAVE_BOOK(
        p_book_id IN OUT NUMBER,
        p_title IN VARCHAR2,
        p_isbn IN VARCHAR2,
        p_publication_date IN DATE,
        p_publisher IN VARCHAR2,
        p_genre IN VARCHAR2,
        p_summary IN CLOB
    );
    
    -- Delete a book
    PROCEDURE DELETE_BOOK(
        p_book_id IN NUMBER,
        p_success OUT BOOLEAN
    );
    
    -- Link book and author
    PROCEDURE LINK_BOOK_AUTHOR(
        p_book_id IN NUMBER,
        p_author_id IN NUMBER
    );
    
    -- Find books by title
    PROCEDURE FIND_BOOKS_BY_TITLE(
        p_title IN VARCHAR2,
        p_books OUT SYS_REFCURSOR
    );
    
    -- Find books by genre
    PROCEDURE FIND_BOOKS_BY_GENRE(
        p_genre IN VARCHAR2,
        p_books OUT SYS_REFCURSOR
    );
    
    -- Find books by author
    PROCEDURE FIND_BOOKS_BY_AUTHOR(
        p_author_id IN NUMBER,
        p_books OUT SYS_REFCURSOR
    );
    
    -- Find books by year range
    PROCEDURE FIND_BOOKS_BY_YEAR_RANGE(
        p_start_year IN NUMBER,
        p_end_year IN NUMBER,
        p_books OUT SYS_REFCURSOR
    );
END BOOK_PKG;
/

CREATE OR REPLACE PACKAGE BODY BOOK_PKG AS
    -- Get all books
    PROCEDURE GET_ALL_BOOKS(
        p_books OUT SYS_REFCURSOR
    ) IS
    BEGIN
        OPEN p_books FOR
        SELECT b.book_id, b.title, b.isbn, b.publication_date, b.publisher, b.genre, b.summary
        FROM books b
        ORDER BY b.title;
    END GET_ALL_BOOKS;
    
    -- Get book by ID
    PROCEDURE GET_BOOK_BY_ID(
        p_book_id IN NUMBER,
        p_book OUT SYS_REFCURSOR
    ) IS
    BEGIN
        OPEN p_book FOR
        SELECT b.book_id, b.title, b.isbn, b.publication_date, b.publisher, b.genre, b.summary
        FROM books b
        WHERE b.book_id = p_book_id;
    END GET_BOOK_BY_ID;
    
    -- Save (create or update) a book
    PROCEDURE SAVE_BOOK(
        p_book_id IN OUT NUMBER,
        p_title IN VARCHAR2,
        p_isbn IN VARCHAR2,
        p_publication_date IN DATE,
        p_publisher IN VARCHAR2,
        p_genre IN VARCHAR2,
        p_summary IN CLOB
    ) IS
    BEGIN
        IF p_book_id IS NULL THEN
            -- Create new book
            SELECT book_seq.NEXTVAL INTO p_book_id FROM DUAL;
            
            INSERT INTO books (book_id, title, isbn, publication_date, publisher, genre, summary)
            VALUES (p_book_id, p_title, p_isbn, p_publication_date, p_publisher, p_genre, p_summary);
        ELSE
            -- Update existing book
            UPDATE books
            SET title = p_title,
                isbn = p_isbn,
                publication_date = p_publication_date,
                publisher = p_publisher,
                genre = p_genre,
                summary = p_summary
            WHERE book_id = p_book_id;
        END IF;
        
        COMMIT;
    EXCEPTION
        WHEN OTHERS THEN
            ROLLBACK;
            RAISE;
    END SAVE_BOOK;
    
    -- Delete a book
    PROCEDURE DELETE_BOOK(
        p_book_id IN NUMBER,
        p_success OUT BOOLEAN
    ) IS
        v_count NUMBER;
    BEGIN
        SELECT COUNT(*) INTO v_count FROM books WHERE book_id = p_book_id;
        
        IF v_count > 0 THEN
            DELETE FROM books WHERE book_id = p_book_id;
            p_success := TRUE;
            COMMIT;
        ELSE
            p_success := FALSE;
        END IF;
    EXCEPTION
        WHEN OTHERS THEN
            ROLLBACK;
            p_success := FALSE;
            RAISE;
    END DELETE_BOOK;
    
    -- Link book and author
    PROCEDURE LINK_BOOK_AUTHOR(
        p_book_id IN NUMBER,
        p_author_id IN NUMBER
    ) IS
        v_count NUMBER;
    BEGIN
        -- Check if relationship already exists
        SELECT COUNT(*) INTO v_count 
        FROM book_authors 
        WHERE book_id = p_book_id AND author_id = p_author_id;
        
        IF v_count = 0 THEN
            INSERT INTO book_authors (book_id, author_id)
            VALUES (p_book_id, p_author_id);
            COMMIT;
        END IF;
    EXCEPTION
        WHEN OTHERS THEN
            ROLLBACK;
            RAISE;
    END LINK_BOOK_AUTHOR;
    
    -- Find books by title
    PROCEDURE FIND_BOOKS_BY_TITLE(
        p_title IN VARCHAR2,
        p_books OUT SYS_REFCURSOR
    ) IS
    BEGIN
        OPEN p_books FOR
        SELECT b.book_id, b.title, b.isbn, b.publication_date, b.publisher, b.genre, b.summary
        FROM books b
        WHERE UPPER(b.title) LIKE UPPER(p_title)
        ORDER BY b.title;
    END FIND_BOOKS_BY_TITLE;
    
    -- Find books by genre
    PROCEDURE FIND_BOOKS_BY_GENRE(
        p_genre IN VARCHAR2,
        p_books OUT SYS_REFCURSOR
    ) IS
    BEGIN
        OPEN p_books FOR
        SELECT b.book_id, b.title, b.isbn, b.publication_date, b.publisher, b.genre, b.summary
        FROM books b
        WHERE UPPER(b.genre) = UPPER(p_genre)
        ORDER BY b.title;
    END FIND_BOOKS_BY_GENRE;
    
    -- Find books by author
    PROCEDURE FIND_BOOKS_BY_AUTHOR(
        p_author_id IN NUMBER,
        p_books OUT SYS_REFCURSOR
    ) IS
    BEGIN
        OPEN p_books FOR
        SELECT b.book_id, b.title, b.isbn, b.publication_date, b.publisher, b.genre, b.summary
        FROM books b
        JOIN book_authors ba ON b.book_id = ba.book_id
        WHERE ba.author_id = p_author_id
        ORDER BY b.title;
    END FIND_BOOKS_BY_AUTHOR;
    
    -- Find books by year range
    PROCEDURE FIND_BOOKS_BY_YEAR_RANGE(
        p_start_year IN NUMBER,
        p_end_year IN NUMBER,
        p_books OUT SYS_REFCURSOR
    ) IS
    BEGIN
        OPEN p_books FOR
        SELECT b.book_id, b.title, b.isbn, b.publication_date, b.publisher, b.genre, b.summary
        FROM books b
        WHERE EXTRACT(YEAR FROM b.publication_date) BETWEEN p_start_year AND p_end_year
        ORDER BY b.publication_date;
    END FIND_BOOKS_BY_YEAR_RANGE;

END BOOK_PKG;
/