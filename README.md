# Smart Internship & Placement Portal

A full-stack Spring Boot and Thymeleaf application designed to connect students with companies for internship opportunities. Features include CV parsing, automated job matching, mock interviews, and advanced analytics.

## Tech Stack
* **Backend:** Java 21, Spring Boot (Web, Data JPA, Security, Mail)
* **Frontend:** HTML5, CSS3, Bootstrap 5, Thymeleaf, Chart.js
* **Database:** MySQL

## How to Run
1. Clone the repository: `git clone <repository-url>`
2. Create a MySQL database named `intern_portal`.
3. Update `src/main/resources/application.properties` with your MySQL username and password.
4. Run the application via your IDE or using Maven: `./mvnw spring-boot:run`
5. Access the application at `http://localhost:8080`.

## Branching Strategy
We follow a standard Git Feature Branch Workflow:
* `main`: Production-ready code. Do not push directly here.
* `dev`: Integration branch for the current sprint.
* `feature/<member-module>`: Individual branches for development.

## Contribution Guide
* Each member must complete their 3 backend files and 3 frontend pages.
* Ensure all code is documented and passes tests.

*Database Architect Note: The Entity Relationship Diagram (ERD) can be found in the `/docs` folder.*