CREATE TABLE users (
    user_id     SERIAL PRIMARY KEY,
    username    VARCHAR(50) UNIQUE NOT NULL,
    password    VARCHAR(100) NOT NULL
);

CREATE TABLE admins (
    admin_id    SERIAL PRIMARY KEY,
    username    VARCHAR(50) UNIQUE NOT NULL,
    password    VARCHAR(100) NOT NULL
);

CREATE TABLE books (
    book_id     SERIAL PRIMARY KEY,
    title       VARCHAR(200) NOT NULL,
    author      VARCHAR(150) NOT NULL,
    available   BOOLEAN DEFAULT TRUE
);

CREATE TABLE loans (
    loan_id       SERIAL PRIMARY KEY,
    user_id       INT NOT NULL REFERENCES users(user_id),
    book_id       INT NOT NULL REFERENCES books(book_id),
    borrow_date   DATE DEFAULT CURRENT_DATE,
    return_date   DATE,
    is_returned   BOOLEAN DEFAULT FALSE
);