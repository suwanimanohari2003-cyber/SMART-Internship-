# Smart Internship Portal — ERD (Entity Relationship Diagram)

## Database: `intern_portal`

The ERD image (`ERD.png`) shows all 10 tables and their relationships.
See the diagram below for the logical schema.

## Tables & Relationships

```
users ──────────────────────────── user_roles ─── roles
  │ (OneToOne)                                      │
  ├──► students                                     │
  │       └──► applications ◄──── job_postings ◄──┘
  │       └──► interview_sessions ◄── interview_answers
  │                                └── interview_questions ◄── job_postings
  └──► companies ──────────────► job_postings
```

## Table Descriptions

| Table               | Key Columns                                                    | Owner |
|---------------------|----------------------------------------------------------------|-------|
| users               | id, email, username, password_hash, enabled, created_at        | M1/M2 |
| roles               | id, name (STUDENT/COMPANY/ADMIN)                               | M1/M2 |
| user_roles          | user_id FK, role_id FK                                         | M2    |
| students            | id, user_id FK, full_name, email, university, gpa, skills, cv_path, profile_complete | M3 |
| companies           | id, user_id FK, name, industry, location, website, logo_path   | M5    |
| job_postings        | id, company_id FK, title, description, required_skills, min_gpa, deadline, status | M5/M6 |
| applications        | id, student_id FK, job_posting_id FK, status, applied_at, updated_at, status_changed_by | M7 |
| interview_questions | id, job_posting_id FK, question_text, category, difficulty, sample_answer | M8 |
| interview_sessions  | id, student_id FK, job_posting_id FK, total_score, passed, started_at, completed_at | M8 |
| interview_answers   | id, session_id FK, question_id FK, answer_text, score, feedback | M8 |

> **Note:** The visual ERD diagram (ERD.png) was created using draw.io.
> To view or edit: open [draw.io](https://app.diagrams.net/) and import the schema above.
