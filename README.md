# Restaurant Management System (Vanilla CRM)

This is a modern full-stack CRM platform for restaurant management, featuring a modular architecture.

## Repository Structure

- `/backend` - Java Spring Boot REST API (JPA + H2/PostgreSQL)
- `/frontend` - Angular 21+ Frontend Application (Signals API)

## Getting Started

### Backend Setup (`/backend`)
1. Open the `/backend` directory: `cd backend`
2. Build and run with Maven: `./mvnw spring-boot:run` (or `mvn spring-boot:run`)
   - The API will be available at `http://localhost:8080/api`
   - H2 Console: `http://localhost:8080/api/h2-console`
   - Swagger UI: `http://localhost:8080/api/swagger-ui.html`
   - Health check: `http://localhost:8080/api/health`

### Frontend Setup (`/frontend`)
1. Open the `/frontend` directory: `cd frontend`
2. Install dependencies: `npm install`
3. Start the Angular dev server: `npm start` (or `ng serve`)
   - The UI will be available at `http://localhost:4200`
   - The frontend proxy routes `/api` calls to the Spring Boot backend.

## Architecture Highlights
* **Auth**: JWT-based authentication with BCrypt hashing (Spring Security).
* **Database**: H2 (development) / PostgreSQL (production) through Spring Data JPA.
* **UI**: Fully reactive Angular UI utilizing the modern Signals API.
* **API Docs**: Auto-generated Swagger/OpenAPI documentation.

## Tech Stack
| Layer | Technology |
|---|---|
| Frontend | Angular 21, TypeScript, SCSS, Signals |
| Backend | Java 21, Spring Boot 3.4, Spring Security |
| ORM | Spring Data JPA, Hibernate |
| Database | H2 (dev), PostgreSQL (prod) |
| Auth | JWT (jjwt), BCrypt |
| Docs | SpringDoc OpenAPI (Swagger) |
