-- =====================================================================
-- Smart Internship Portal — Seed Data (data.sql)
-- Columns match schema.sql exactly
-- =====================================================================

-- Roles
INSERT IGNORE INTO roles (id, name) VALUES (1, 'STUDENT'), (2, 'COMPANY'), (3, 'ADMIN');

-- =====================================================================
-- USERS  (password: password123)
-- =====================================================================
INSERT IGNORE INTO users (id, email, username, password_hash, enabled, created_at, is_deleted)
VALUES (1, 'interngo26@gmail.com', 'System Admin', '$2y$10$VRvwCJiTa58JL/vEiB9NEuTAiWBW4WURGtlQY4KK2sBylEHmZCiQe', true, NOW(), false);
INSERT IGNORE INTO user_roles (user_id, role_id) VALUES (1, 3);

INSERT IGNORE INTO users (id, email, username, password_hash, enabled, created_at, is_deleted)
VALUES (2, 'hr@virtusa.com', 'Virtusa HR', '$2y$10$RzTqiNJM0AK8odXIHz4ck..oPauHmjjZPkohDD3ic8FVWrTJq/Rie', true, NOW(), false);
INSERT IGNORE INTO user_roles (user_id, role_id) VALUES (2, 2);

INSERT IGNORE INTO users (id, email, username, password_hash, enabled, created_at, is_deleted)
VALUES (3, 'careers@wso2.com', 'WSO2 Careers', '$2a$10$wN9.4VfG27K2V5iL.x7syeQ6.gG./A.3x4e.g8e/2Q4.t.M7/kE.O', true, NOW(), false);
INSERT IGNORE INTO user_roles (user_id, role_id) VALUES (3, 2);

INSERT IGNORE INTO users (id, email, username, password_hash, enabled, created_at, is_deleted)
VALUES (5, 'lumelk2026@gmail.com', 'Lume LK', '$2y$10$MuvjIkO9XuohWiB9uHF6oeZJFn.2ULYphshVD8vhZr.AM3nrTEQ7.', true, NOW(), false);
INSERT IGNORE INTO user_roles (user_id, role_id) VALUES (5, 2);

INSERT IGNORE INTO users (id, email, username, password_hash, enabled, created_at, is_deleted)
VALUES (6, 'recruit@99x.io', '99x Technology', '$2a$10$wN9.4VfG27K2V5iL.x7syeQ6.gG./A.3x4e.g8e/2Q4.t.M7/kE.O', true, NOW(), false);
INSERT IGNORE INTO user_roles (user_id, role_id) VALUES (6, 2);

INSERT IGNORE INTO users (id, email, username, password_hash, enabled, created_at, is_deleted)
VALUES (4, 'amandiewanthika@gmail.com', 'Student', '$2y$10$alNbvzDAMq5qO7/I12gZz.sSYvIcfNiVyStwxFuN7kuMSDuXlvRaS', true, NOW(), false);
INSERT IGNORE INTO user_roles (user_id, role_id) VALUES (4, 1);

INSERT IGNORE INTO users (id, email, username, password_hash, enabled, created_at, is_deleted)
VALUES (7, 'kasun.perera@nsbm.ac.lk', 'Kasun Perera', '$2a$10$wN9.4VfG27K2V5iL.x7syeQ6.gG./A.3x4e.g8e/2Q4.t.M7/kE.O', true, NOW(), false);
INSERT IGNORE INTO user_roles (user_id, role_id) VALUES (7, 1);

INSERT IGNORE INTO users (id, email, username, password_hash, enabled, created_at, is_deleted)
VALUES (8, 'dinara.silva@nsbm.ac.lk', 'Dinara Silva', '$2a$10$wN9.4VfG27K2V5iL.x7syeQ6.gG./A.3x4e.g8e/2Q4.t.M7/kE.O', true, NOW(), false);
INSERT IGNORE INTO user_roles (user_id, role_id) VALUES (8, 1);

INSERT IGNORE INTO users (id, email, username, password_hash, enabled, created_at, is_deleted)
VALUES (9, 'nuwan.k@university.lk', 'Nuwan Kumara', '$2a$10$wN9.4VfG27K2V5iL.x7syeQ6.gG./A.3x4e.g8e/2Q4.t.M7/kE.O', true, NOW(), false);
INSERT IGNORE INTO user_roles (user_id, role_id) VALUES (9, 1);

INSERT IGNORE INTO users (id, email, username, password_hash, enabled, created_at, is_deleted)
VALUES (10, 'sachini.r@students.uok.ac.lk', 'Sachini Ranathunga', '$2a$10$wN9.4VfG27K2V5iL.x7syeQ6.gG./A.3x4e.g8e/2Q4.t.M7/kE.O', true, NOW(), false);
INSERT IGNORE INTO user_roles (user_id, role_id) VALUES (10, 1);

-- =====================================================================
-- COMPANIES
-- =====================================================================
INSERT IGNORE INTO companies (id, user_id, name, industry, location, website, created_at, is_deleted)
VALUES (1, 2, 'Virtusa Corp', 'Technology', 'Colombo 09', 'www.virtusa.com', NOW(), false);

INSERT IGNORE INTO companies (id, user_id, name, industry, location, website, created_at, is_deleted)
VALUES (2, 3, 'WSO2', 'Software Engineering', 'Colombo 03', 'www.wso2.com', NOW(), false);

INSERT IGNORE INTO companies (id, user_id, name, industry, location, website, created_at, is_deleted)
VALUES (3, 5, 'Lume LK', 'Digital Health', 'Colombo 07', 'www.lumelk.com', NOW(), false);

INSERT IGNORE INTO companies (id, user_id, name, industry, location, website, created_at, is_deleted)
VALUES (4, 6, '99x Technology', 'Software Engineering', 'Colombo 01', 'www.99x.io', NOW(), false);

-- =====================================================================
-- JOB POSTINGS
-- =====================================================================
INSERT IGNORE INTO job_postings (id, company_id, title, description, required_skills, min_gpa, deadline, status, created_at, is_deleted)
VALUES (1, 1, 'Software Engineering Intern', 'Join our dynamic team to build enterprise-grade applications used by Fortune 500 clients.', 'Java, Spring Boot, MySQL', 3.0, '2026-12-31', 'OPEN', NOW(), false);

INSERT IGNORE INTO job_postings (id, company_id, title, description, required_skills, min_gpa, deadline, status, created_at, is_deleted)
VALUES (2, 2, 'QA Automation Intern', 'Looking for passionate testers eager to break things in the best way possible.', 'Selenium, Java, API Testing', 2.8, '2026-11-30', 'OPEN', NOW(), false);

INSERT IGNORE INTO job_postings (id, company_id, title, description, required_skills, min_gpa, deadline, status, created_at, is_deleted)
VALUES (3, 2, 'Frontend Developer Intern', 'React and UI/UX enthusiasts wanted! Build user interfaces for our developer portal.', 'React, CSS3, HTML5, Node', 3.2, '2026-10-15', 'OPEN', NOW(), false);

INSERT IGNORE INTO job_postings (id, company_id, title, description, required_skills, min_gpa, deadline, status, created_at, is_deleted)
VALUES (4, 3, 'Senior Automation Engineer Intern', 'Lead development of intelligent healthcare workflow automation at Lume LK.', 'Selenium, Playwright, API Integration', 2.5, '2026-08-02', 'OPEN', NOW(), false);

INSERT IGNORE INTO job_postings (id, company_id, title, description, required_skills, min_gpa, deadline, status, created_at, is_deleted)
VALUES (5, 4, 'Data Engineering Intern', 'Work with big data pipelines, ETL processes, and cloud infrastructure at 99x.', 'Python, SQL, Apache Spark, AWS', 3.0, '2026-09-30', 'OPEN', NOW(), false);

INSERT IGNORE INTO job_postings (id, company_id, title, description, required_skills, min_gpa, deadline, status, created_at, is_deleted)
VALUES (6, 4, 'DevOps Intern', 'Build and maintain CI/CD pipelines and containerized deployments. Learn from expert engineers.', 'Docker, Kubernetes, Jenkins, Linux', 2.8, '2026-09-15', 'OPEN', NOW(), false);

INSERT IGNORE INTO job_postings (id, company_id, title, description, required_skills, min_gpa, deadline, status, created_at, is_deleted)
VALUES (7, 1, 'Mobile App Developer Intern', 'Build mobile applications for enterprise clients using React Native and Flutter.', 'React Native, Flutter, JavaScript', 3.0, '2026-12-15', 'OPEN', NOW(), false);

INSERT IGNORE INTO job_postings (id, company_id, title, description, required_skills, min_gpa, deadline, status, created_at, is_deleted)
VALUES (8, 2, 'Backend Developer Intern', 'Design and build RESTful APIs for the WSO2 product suite.', 'Java, Spring Boot, REST, Microservices', 3.1, '2026-12-31', 'OPEN', NOW(), false);

INSERT IGNORE INTO job_postings (id, company_id, title, description, required_skills, min_gpa, deadline, status, created_at, is_deleted)
VALUES (9, 3, 'ML Engineer Intern', 'Closed position — no longer accepting applications.', 'Python, TensorFlow, Machine Learning', 3.5, '2025-06-01', 'CLOSED', NOW(), false);

INSERT IGNORE INTO job_postings (id, company_id, title, description, required_skills, min_gpa, deadline, status, created_at, is_deleted)
VALUES (10, 4, 'Cloud Solutions Intern', 'Support cloud migration projects and AWS/Azure deployments.', 'AWS, Azure, Python, Networking', 2.7, '2026-11-15', 'OPEN', NOW(), false);

-- =====================================================================
-- STUDENTS  (schema: id, user_id, full_name, email, university, gpa, skills, cv_path, profile_complete)
-- NO created_at or is_deleted in students table schema!
-- =====================================================================
INSERT IGNORE INTO students (id, user_id, full_name, email, university, gpa, skills, profile_complete)
VALUES (1, 4, 'Amandi Wanthika', 'amandiewanthika@gmail.com', 'NSBM Green University', 2.54, 'Java, Spring Boot, MySQL, HTML, CSS', true);

INSERT IGNORE INTO students (id, user_id, full_name, email, university, gpa, skills, profile_complete)
VALUES (2, 7, 'Kasun Perera', 'kasun.perera@nsbm.ac.lk', 'NSBM Green University', 3.45, 'Python, Django, React, SQL, Machine Learning', true);

INSERT IGNORE INTO students (id, user_id, full_name, email, university, gpa, skills, profile_complete)
VALUES (3, 8, 'Dinara Silva', 'dinara.silva@nsbm.ac.lk', 'University of Moratuwa', 3.72, 'Java, Spring Boot, REST APIs, Microservices, Docker', true);

INSERT IGNORE INTO students (id, user_id, full_name, email, university, gpa, skills, profile_complete)
VALUES (4, 9, 'Nuwan Kumara', 'nuwan.k@university.lk', 'University of Kelaniya', 3.10, 'React, JavaScript, Node, CSS3, HTML5', true);

INSERT IGNORE INTO students (id, user_id, full_name, email, university, gpa, skills, profile_complete)
VALUES (5, 10, 'Sachini Ranathunga', 'sachini.r@students.uok.ac.lk', 'University of Kelaniya', 3.85, 'Selenium, Java, API Testing, Postman, JUnit', true);

-- =====================================================================
-- APPLICATIONS  (schema: id, student_id, job_posting_id, status, applied_at, updated_at, status_changed_by)
-- NO is_deleted in applications table schema!
-- =====================================================================
INSERT IGNORE INTO applications (id, student_id, job_posting_id, status)
VALUES (1, 2, 1, 'SHORTLISTED');

INSERT IGNORE INTO applications (id, student_id, job_posting_id, status)
VALUES (2, 3, 2, 'PENDING');

INSERT IGNORE INTO applications (id, student_id, job_posting_id, status)
VALUES (3, 4, 3, 'PENDING');

INSERT IGNORE INTO applications (id, student_id, job_posting_id, status)
VALUES (4, 5, 2, 'ACCEPTED');

INSERT IGNORE INTO applications (id, student_id, job_posting_id, status)
VALUES (5, 3, 1, 'REJECTED');

INSERT IGNORE INTO applications (id, student_id, job_posting_id, status)
VALUES (6, 2, 8, 'PENDING');

INSERT IGNORE INTO applications (id, student_id, job_posting_id, status)
VALUES (7, 4, 8, 'PENDING');

-- =====================================================================
-- INTERVIEW QUESTIONS  (schema: id, job_posting_id, question_text, category, difficulty, sample_answer)
-- NO is_deleted in interview_questions table schema!
-- =====================================================================
INSERT IGNORE INTO interview_questions (id, job_posting_id, question_text, category, difficulty, sample_answer)
VALUES (1, 1, 'What is the difference between an interface and an abstract class in Java?', 'TECHNICAL', 'MEDIUM',
'An interface defines a contract with all abstract methods (Java 8+ allows default/static methods), while an abstract class can have both abstract and concrete methods. A class can implement multiple interfaces but can only extend one abstract class.');

INSERT IGNORE INTO interview_questions (id, job_posting_id, question_text, category, difficulty, sample_answer)
VALUES (2, 1, 'Explain the concept of dependency injection in Spring Boot.', 'TECHNICAL', 'HARD',
'Dependency Injection is a design pattern where an object receives its dependencies from an external source. Spring uses @Autowired, @Component, @Service to manage beans via the IoC container.');

INSERT IGNORE INTO interview_questions (id, job_posting_id, question_text, category, difficulty, sample_answer)
VALUES (3, 1, 'Tell me about a time you faced a technical challenge and how you solved it.', 'HR', 'MEDIUM',
'Describe a specific situation, the challenge, steps taken to solve it (research, debugging, collaboration), and the successful outcome. Focus on your problem-solving process.');

INSERT IGNORE INTO interview_questions (id, job_posting_id, question_text, category, difficulty, sample_answer)
VALUES (4, 2, 'What is the difference between black-box and white-box testing?', 'TECHNICAL', 'EASY',
'Black-box testing tests functionality without knowledge of internal code. White-box testing tests internal structure and code paths. Black-box focuses on outputs; white-box focuses on code coverage.');

INSERT IGNORE INTO interview_questions (id, job_posting_id, question_text, category, difficulty, sample_answer)
VALUES (5, 2, 'How would you write a Selenium test for a login form?', 'TECHNICAL', 'MEDIUM',
'Use WebDriver to navigate to the login URL, find elements by ID/name for username and password, send keys, click submit, then assert the expected post-login element is present.');
