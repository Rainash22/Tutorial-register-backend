# Student Register Backend

Spring Boot backend for a tutorial/student register app.

## Tech Stack

- Java 17
- Spring Boot 3
- Spring Web
- Spring Data JPA
- Bean Validation
- H2 database for local development
- PostgreSQL driver included for later production setup
- Swagger UI via Springdoc OpenAPI

## Run

```bash
mvn spring-boot:run
```

The API runs on:

```text
http://localhost:8080
```

Useful local URLs:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- H2 Console: `http://localhost:8080/h2-console`

H2 connection:

```text
JDBC URL: jdbc:h2:mem:student_register
User: sa
Password: blank
```

## Run With Neon PostgreSQL

Create a free project at Neon, then copy the connection details from the Neon dashboard.

Use the JDBC URL format:

```text
jdbc:postgresql://your-neon-host.neon.tech/your-database?sslmode=require
```

PowerShell example:

```powershell
$env:SPRING_PROFILES_ACTIVE="neon"
$env:DB_URL="jdbc:postgresql://your-neon-host.neon.tech/your-database?sslmode=require"
$env:DB_USERNAME="your-neon-username"
$env:DB_PASSWORD="your-neon-password"

mvn spring-boot:run
```

For this project, you can also use the helper script:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\run-neon.ps1
```

It sets:

```text
DB_URL=jdbc:postgresql://ep-floral-field-aqlwoh46.c-8.us-east-1.aws.neon.tech/neondb?sslmode=require
DB_USERNAME=neondb_owner
```

Then it prompts for the Neon password without saving it in the repository.

After the app starts, Spring Boot will create/update the tables in Neon because the `neon` profile currently uses:

```yaml
spring.jpa.hibernate.ddl-auto: update
```

For early development this is convenient. Before production deployment, replace it with Flyway migrations.

To seed sample data after switching to Neon:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\seed-sample-data.ps1
```

## First Endpoints

Each resource has basic CRUD endpoints:

- `GET /api/students`
- `POST /api/students`
- `GET /api/students/{id}`
- `PUT /api/students/{id}`
- `DELETE /api/students/{id}`

The same pattern exists for:

- `/api/staff`
- `/api/attendance`
- `/api/fees`
- `/api/assessments`
- `/api/users`
- `/api/roles`
- `/api/notifications`

## Example Student Request

```json
{
  "fullName": "Anita Sharma",
  "admissionNumber": "STU-001",
  "dateOfBirth": "2010-04-12",
  "gender": "FEMALE",
  "className": "Grade 9",
  "courseName": "Maths Tuition",
  "guardianName": "Ravi Sharma",
  "guardianPhone": "9876543210",
  "email": "anita@example.com",
  "phone": "9876500000",
  "address": "Chennai",
  "admissionDate": "2026-05-21",
  "status": "ACTIVE",
  "assignedStaffId": 1,
  "userAccountId": 2
}
```

Relationship fields use IDs in request DTOs. For example:

- `roleIds` on users
- `userAccountId` on staff
- `assignedStaffId` and `userAccountId` on students
- `studentId`, `markedById`, and `evaluatedById` on transaction records
- `studentId` or `staffId` on notifications

## Suggested Backend Roadmap

1. Replace generic CRUD requests with DTOs.
2. Add authentication and role-based authorization.
3. Add filtered endpoints, such as attendance by student/date and unpaid fees.
4. Add PostgreSQL configuration.
5. Add notification providers for email, SMS, or push.
6. Add integration tests for important workflows.
