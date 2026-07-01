# Restaurant Management System (Vanilla CRM)

This is a modern full-stack CRM platform for restaurant management, featuring a modular architecture.

## Repository Structure

- `/api` - NestJS REST API Backend (TypeORM + SQLite)
- `/frontend` - Angular 18+ Frontend Application (Signals API)
- `/docs` - Project documentation and implementation plans

## Getting Started

### Backend Setup (`/api`)
1. Open the `/api` directory: `cd api`
2. Install dependencies: `npm install`
3. Start the NestJS server: `npm run start:dev`
   - The API will be available at `http://localhost:3000/api`
   - SQLite database will be automatically created at `api/data/restaurant.db`

### Frontend Setup (`/frontend`)
1. Open the `/frontend` directory: `cd frontend`
2. Install dependencies: `npm install`
3. Start the Angular dev server: `npm start` (or `ng serve`)
   - The UI will be available at `http://localhost:4200`
   - The frontend proxy routes `/api` calls to the NestJS backend.

## Architecture Highlights
* **Auth**: JWT-based authentication with bcrypt hashing.
* **Database**: SQLite through TypeORM, ensuring easy development and distribution.
* **UI**: Fully reactive Angular UI utilizing the modern Signals API.
