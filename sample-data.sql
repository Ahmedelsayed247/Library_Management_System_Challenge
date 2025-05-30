USE library_management_system;

-- Insert sample Users-- The password for all is password123
INSERT INTO users (username, password, email, first_name, last_name, role, active, created_at)
VALUES 
('admin', '$2a$10$hashedpassword123admin', 'admin@library.com', 'System', 'Administrator', 'ADMINISTRATOR', TRUE, NOW()),
('librarian1', '$2a$10$hashedpassword123librarian', 'librarian@library.com', 'John', 'Librarian', 'LIBRARIAN', TRUE, NOW()),
('staff1', '$2a$10$hashedpassword123staff', 'staff@library.com', 'Jane', 'Staff', 'STAFF', TRUE, NOW());

-- Insert sample User Activities
INSERT INTO user_activities (user_id, action, description, created_at) VALUES
(1, 'LOGIN', 'Admin user logged in', NOW()),
(2, 'ADD_BOOK', 'Added new book "Java Programming"', NOW()),
(3, 'BORROW_BOOK', 'Borrowed book with ID 1', NOW());

-- Insert sample Authors
INSERT INTO authors (first_name, last_name, biography) VALUES
('Joshua', 'Bloch', 'Author of Effective Java'),
('Robert', 'Martin', 'Known as Uncle Bob, author and software engineer'),
('Martin', 'Fowler', 'Software developer, author, and speaker');

-- Insert sample Publishers
INSERT INTO publishers (name, address, contact_email) VALUES
('Addison-Wesley', '75 Arlington St, Boston, MA', 'contact@aw.com'),
('O\'Reilly Media', '1005 Gravenstein Hwy N, Sebastopol, CA', 'info@oreilly.com');

-- Insert sample Categories
INSERT INTO categories (name, description, parent_id) VALUES
('Programming', 'Books related to programming languages and software development', NULL),
('Java', 'Java programming books', 1),
('Software Engineering', 'Books about software engineering principles', NULL);

-- Insert sample Books
INSERT INTO books (title, isbn, summary, publication_year, edition, language, cover_image_url, publisher_id, category_id, available, total_copies, available_copies, created_at) VALUES
('Effective Java', '978-0134685991', 'A comprehensive guide to best practices in Java programming', 2018, '3rd', 'English', NULL, 1, 2, TRUE, 5, 5, NOW()),
('Clean Code', '978-0132350884', 'A handbook of agile software craftsmanship', 2008, '1st', 'English', NULL, 1, 3, TRUE, 3, 3, NOW()),
('Refactoring', '978-0201485677', 'Improving the design of existing code', 1999, '1st', 'English', NULL, 1, 3, TRUE, 4, 4, NOW());

-- Insert sample Book-Authors associations
INSERT INTO book_authors (book_id, author_id) VALUES
(1, 1), -- Effective Java by Joshua Bloch
(2, 2), -- Clean Code by Robert Martin
(3, 3); -- Refactoring by Martin Fowler

-- Insert sample Members
INSERT INTO members (first_name, last_name, email, phone, address, membership_date, expiry_date, active, created_at) VALUES
('Alice', 'Wonderland', 'alice@example.com', '1234567890', '123 Fantasy Rd', '2023-01-01', '2024-01-01', TRUE, NOW()),
('Bob', 'Builder', 'bob@example.com', '0987654321', '456 Construction St', '2022-06-15', '2023-06-15', TRUE, NOW());

-- Insert sample Borrowing Transactions
INSERT INTO borrowing_transactions (book_id, member_id, issued_by, returned_by, borrow_date, due_date, return_date, status, fine_amount, created_at) VALUES
(1, 1, 2, NULL, '2024-05-01', '2024-05-15', NULL, 'BORROWED', 0, NOW()),
(2, 2, 2, 2, '2024-04-10', '2024-04-24', '2024-04-20', 'RETURNED', 0, NOW());
