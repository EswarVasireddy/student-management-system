# Student Management System

A Java backend for managing student enrollment records — built around a DAO interface and a service layer with validation, so the business logic (GPA rules, duplicate-email checks) is fully unit-testable without a real database.

## Design

- **`Student`** — the domain model (immutable name/email, mutable major/gpa).
- **`StudentDAO`** — persistence interface: save, find by id/major, update, delete.
  - **`JdbcStudentDAO`** — real implementation backed by MySQL via plain JDBC, using `PreparedStatement` everywhere (no string-concatenated SQL).
  - **`InMemoryStudentDAO`** — in-memory implementation used by `Main` (so the demo runs with zero setup) and by the unit tests (so they run in milliseconds with no database).
- **`StudentService`** — business rules on top of a `StudentDAO`: email format validation, GPA range checks (0.0–4.0), duplicate-email rejection, GPA/major updates, and reporting (average GPA overall or per major, top-N students by GPA).

Programming to the `StudentDAO` interface — rather than `StudentService` depending on `JdbcStudentDAO` directly — is what makes the service layer testable in isolation: `StudentServiceTest` wires it up with `InMemoryStudentDAO` and never touches a database.

## Tech Stack

- Java 17
- JDBC (`mysql-connector-j`) for the real DAO
- Maven
- JUnit 5 for unit tests

## Project Structure

    student-management-system/
    ├── pom.xml
    ├── schema.sql
    └── src/
        ├── main/java/com/klu/sms/
        │   ├── Main.java                    # Console demo (runs on InMemoryStudentDAO)
        │   ├── model/Student.java
        │   ├── dao/StudentDAO.java          # Interface
        │   ├── dao/InMemoryStudentDAO.java  # For the demo + tests
        │   ├── dao/JdbcStudentDAO.java       # MySQL-backed, for real use
        │   └── service/StudentService.java  # Validation + business rules
        └── test/java/com/klu/sms/service/
            └── StudentServiceTest.java       # 12 JUnit 5 tests against InMemoryStudentDAO

## Getting Started

### Run the console demo (no database needed)

    mvn compile exec:java -Dexec.mainClass=com.klu.sms.Main

Or compile and run directly:

    mvn compile
    java -cp target/classes com.klu.sms.Main

This enrolls five sample students, prints the roster, computes overall and per-major average GPA, lists the top 3 students by GPA, updates a GPA, and withdraws a student — exercising every method on `StudentService`.

### Run the tests

    mvn test

12 tests covering: successful enrollment, invalid email rejection, GPA out-of-range rejection, duplicate-email rejection, GPA updates (including on an unknown student), overall and per-major average GPA (including the empty case), top-N ranking, withdrawal, and major transfers.

### Point it at real MySQL

    mysql -u root -p -e "CREATE DATABASE student_management"
    mysql -u root -p student_management < schema.sql

Then construct the service with the JDBC DAO instead of the in-memory one:

    StudentDAO dao = new JdbcStudentDAO(() -> DriverManager.getConnection(
        "jdbc:mysql://localhost:3306/student_management", "root", "yourpassword"));
    StudentService service = new StudentService(dao);

`JdbcStudentDAO` takes a `Supplier<Connection>` rather than a single connection, so swapping in a connection pool later doesn't change its API.
