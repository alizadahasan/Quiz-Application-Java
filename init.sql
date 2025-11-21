CREATE TABLE IF NOT EXISTS users (
    user_id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL,
    role TEXT CHECK(role IN ('admin', 'user')) NOT NULL,
    email TEXT NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS quizzes (
    quiz_id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    description TEXT,
    created_by INTEGER,
    time_limit INTEGER,
    FOREIGN KEY (created_by) REFERENCES users(user_id)
);

CREATE TABLE IF NOT EXISTS questions (
    question_id INTEGER PRIMARY KEY AUTOINCREMENT,
    quiz_id INTEGER,
    question_text TEXT NOT NULL,
    option_a TEXT NOT NULL,
    option_b TEXT NOT NULL,
    option_c TEXT NOT NULL,
    option_d TEXT NOT NULL,
    correct_answer TEXT NOT NULL,
    FOREIGN KEY (quiz_id) REFERENCES quizzes(quiz_id)
);

CREATE TABLE IF NOT EXISTS results (
    result_id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER,
    quiz_id INTEGER,
    score INTEGER NOT NULL,
    completion_time DATETIME,
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (quiz_id) REFERENCES quizzes(quiz_id)
);

CREATE TABLE IF NOT EXISTS user_answers (
    answer_id INTEGER PRIMARY KEY AUTOINCREMENT,
    result_id INTEGER,
    question_id INTEGER,
    user_answer TEXT NOT NULL,
    FOREIGN KEY (result_id) REFERENCES results(result_id),
    FOREIGN KEY (question_id) REFERENCES questions(question_id)
);
