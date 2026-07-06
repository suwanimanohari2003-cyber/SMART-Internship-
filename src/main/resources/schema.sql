CREATE TABLE IF NOT EXISTS users (
                                     id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                     username VARCHAR(50) NOT NULL UNIQUE,
                                     email VARCHAR(100) NOT NULL UNIQUE,
                                     password_hash VARCHAR(255) NOT NULL,
                                     enabled BOOLEAN DEFAULT TRUE,
                                     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                     updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                     created_by VARCHAR(50),
                                     is_deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS roles (
                                     id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                     name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS user_roles (
                                          user_id BIGINT NOT NULL,
                                          role_id BIGINT NOT NULL,
                                          PRIMARY KEY (user_id, role_id),
                                          FOREIGN KEY (user_id) REFERENCES users(id),
                                          FOREIGN KEY (role_id) REFERENCES roles(id)
);

CREATE TABLE IF NOT EXISTS students (
                                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                        user_id BIGINT UNIQUE,
                                        full_name VARCHAR(100),
                                        email VARCHAR(100) UNIQUE,
                                        university VARCHAR(100),
                                        gpa DOUBLE,
                                        skills TEXT,
                                        cv_path VARCHAR(255),
                                        profile_complete BOOLEAN DEFAULT FALSE,
                                        FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS companies (
                                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                         user_id BIGINT UNIQUE,
                                         name VARCHAR(100),
                                         industry VARCHAR(100),
                                         location VARCHAR(100),
                                         website VARCHAR(255),
                                         logo_path VARCHAR(255),
                                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                         is_deleted BOOLEAN DEFAULT FALSE,
                                         FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS job_postings (
                                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                            company_id BIGINT,
                                            title VARCHAR(100),
                                            description TEXT,
                                            required_skills TEXT,
                                            min_gpa DOUBLE,
                                            deadline DATE,
                                            status VARCHAR(20) DEFAULT 'OPEN',
                                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                            is_deleted BOOLEAN DEFAULT FALSE,
                                            FOREIGN KEY (company_id) REFERENCES companies(id)
);

CREATE TABLE IF NOT EXISTS applications (
                                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                            student_id BIGINT,
                                            job_posting_id BIGINT,
                                            status VARCHAR(20) DEFAULT 'PENDING',
                                            applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                            status_changed_by VARCHAR(50),
                                            FOREIGN KEY (student_id) REFERENCES students(id),
                                            FOREIGN KEY (job_posting_id) REFERENCES job_postings(id),
                                            UNIQUE (student_id, job_posting_id)
);

CREATE TABLE IF NOT EXISTS interview_questions (
                                     id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                     job_posting_id BIGINT,
                                     question_text TEXT,
                                     category VARCHAR(50),
                                     difficulty VARCHAR(20),
                                     sample_answer TEXT,
                                     FOREIGN KEY (job_posting_id) REFERENCES job_postings(id)
);

CREATE TABLE IF NOT EXISTS interview_sessions (
                                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                    student_id BIGINT,
                                    job_posting_id BIGINT,
                                    total_score DOUBLE,
                                    passed BOOLEAN DEFAULT FALSE,
                                    started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                    completed_at TIMESTAMP NULL,
                                    FOREIGN KEY (student_id) REFERENCES students(id),
                                    FOREIGN KEY (job_posting_id) REFERENCES job_postings(id)
);

CREATE TABLE IF NOT EXISTS interview_answers (
                                   id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                   session_id BIGINT,
                                   question_id BIGINT,
                                   answer_text TEXT,
                                   score DOUBLE,
                                   feedback TEXT,
                                   FOREIGN KEY (session_id) REFERENCES interview_sessions(id),
                                   FOREIGN KEY (question_id) REFERENCES interview_questions(id)
);