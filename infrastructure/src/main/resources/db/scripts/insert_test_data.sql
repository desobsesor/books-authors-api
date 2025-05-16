-- Test data for the books-authors database

-- Insert authors
INSERT INTO authors (author_id, first_name, last_name, birth_date, biography) VALUES (author_seq.NEXTVAL, 'Gabriel', 'García Márquez', TO_DATE('1927-03-06', 'YYYY-MM-DD'), 'Colombian writer and journalist, winner of the Nobel Prize in Literature in 1982.');
INSERT INTO authors (author_id, first_name, last_name, birth_date, biography) VALUES (author_seq.NEXTVAL, 'Jorge', 'Luis Borges', TO_DATE('1899-08-24', 'YYYY-MM-DD'), 'Argentine writer, one of the most prominent authors of 20th century literature.');
INSERT INTO authors (author_id, first_name, last_name, birth_date, biography) VALUES (author_seq.NEXTVAL, 'Pablo', 'Neruda', TO_DATE('1904-07-12', 'YYYY-MM-DD'), 'Chilean poet, winner of the Nobel Prize in Literature in 1971.');

-- Insert books
INSERT INTO books (book_id, title, isbn, publication_date, publisher, genre, summary) VALUES (book_seq.NEXTVAL, 'One Hundred Years of Solitude', '978-0307350448', TO_DATE('1967-05-30', 'YYYY-MM-DD'), 'Editorial Sudamericana', 'Magical realism', 'Novel that tells the story of the Buendía family in the fictional town of Macondo.');
INSERT INTO books (book_id, title, isbn, publication_date, publisher, genre, summary) VALUES (book_seq.NEXTVAL, 'Fictions', '978-8420633138', TO_DATE('1944-01-01', 'YYYY-MM-DD'), 'Editorial Sur', 'Fiction', 'Collection of short stories exploring themes such as time, reality, and identity.');
INSERT INTO books (book_id, title, isbn, publication_date, publisher, genre, summary) VALUES (book_seq.NEXTVAL, 'Twenty Love Poems and a Song of Despair', '978-9561115434', TO_DATE('1924-01-01', 'YYYY-MM-DD'), 'Editorial Norma', 'Poetry', 'Collection of poems exploring love, passion, and melancholy.');

-- Relate books to authors
INSERT INTO book_authors (book_id, author_id) VALUES (1, 1);
INSERT INTO book_authors (book_id, author_id) VALUES (2, 2);
INSERT INTO book_authors (book_id, author_id) VALUES (3, 3);