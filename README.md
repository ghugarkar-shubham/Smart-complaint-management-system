# Online Complaint & Issue Tracking System

Modern full-stack complaint management system with a React frontend, Spring Boot REST API, JWT authentication, and MySQL persistence.

## Features

- User registration and login
- Role-based access for users and admins
- User dashboard for complaint submission and tracking
- Admin dashboard for managing, updating, resolving, and deleting complaints
- Search, filter, and status updates
- OTP verification during registration using Fast2SMS
- Automatic SMS alerts to the complaint owner when admins change status
- Responsive UI with cards, tables, sidebar navigation, loading states, and alerts
- Clean REST API and MVC-style backend structure

## Project Structure

```text
project-root/
├── frontend/
├── backend/
├── database/
└── README.md
```

## Prerequisites

- Node.js 18+
- Java 17+
- Maven 3.9+
- MySQL 8+

## Backend Setup

1. Create a MySQL database named `complaint_system`.
2. Update `backend/src/main/resources/application.properties` with your MySQL credentials.
3. Set the Fast2SMS API key in `FAST2SMS_API_KEY` or in `application.properties`.
4. Run the backend:

```bash
cd backend
./mvnw.cmd spring-boot:run
```

Backend base URL: `http://localhost:8080`

If you already have Maven installed globally, `mvn spring-boot:run` also works. On Windows, the included wrapper is the simplest option.

## Frontend Setup

1. Install dependencies:

```bash
cd frontend
npm install
```

2. Start the frontend:

```bash
npm run dev
```

Frontend URL: `http://localhost:5173`

## API Overview

### Auth

- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/send-otp`
- `POST /api/auth/verify-otp`

Register requests now include `mobileNumber`, and registration is allowed only after OTP verification.

### User Complaints

- `GET /api/complaints`
- `POST /api/complaints`
- `GET /api/complaints/{id}`

### Admin Complaints

- `GET /api/admin/complaints`
- `PUT /api/admin/complaints/{id}/status`
- `DELETE /api/admin/complaints/{id}`
- `GET /api/admin/summary`

The status update endpoint returns the updated complaint plus SMS delivery status.

## Default Demo Accounts

The backend seed data creates these accounts on first run:

- Admin: `admin@complaints.com` / `Admin@12345`
- User: `user@complaints.com` / `User@12345`

## Database

The SQL schema is available at `database/complaint_system.sql`.

## Fast2SMS Integration

1. Store your API key as `FAST2SMS_API_KEY` in the environment or in `backend/src/main/resources/application.properties`.
2. During registration, the backend sends an OTP to the submitted mobile number.
3. The user must verify that OTP before the account is created.
4. When an admin updates a complaint status, the backend loads the registered user mobile number from the `users` table and sends a status SMS.
5. The API response includes the SMS result so you can see whether delivery succeeded.

### Sample Request

```http
PUT /api/admin/complaints/101/status
Content-Type: application/json

{
	"status": "RESOLVED"
}
```

### Sample Response

```json
{
	"complaint": {
		"id": 101,
		"title": "Street light not working",
		"category": "Infrastructure",
		"description": "The street light near block A has been off for a week.",
		"status": "RESOLVED",
		"createdAt": "2026-05-17T09:30:00",
		"updatedAt": "2026-05-17T10:02:00",
		"submittedBy": "Demo User",
		"resolvedBy": "Admin",
		"photoFilename": null
	},
	"smsNotification": {
		"sent": true,
		"message": "SMS sent successfully",
		"providerResponse": "{\"return\":true,...}"
	}
}
```

## Deployment Notes

- Configure `VITE_API_BASE_URL` for the frontend when deploying.
- Configure production MySQL credentials and JWT secret in the backend.
- Configure `FAST2SMS_API_KEY` in production rather than checking secrets into source control.
- Run the Spring Boot service behind a reverse proxy if needed.
# Online Complaint & Issue Tracking System

Modern full-stack complaint management system with a React frontend, Spring Boot REST API, JWT authentication, and MySQL persistence.

## Features

- User registration and login
- Role-based access for users and admins
- User dashboard for complaint submission and tracking
- Admin dashboard for managing, updating, resolving, and deleting complaints
- Search, filter, and status updates
- Responsive UI with cards, tables, sidebar navigation, loading states, and alerts
- Clean REST API and MVC-style backend structure

## Project Structure

```text
project-root/
├── frontend/
├── backend/
├── database/
└── README.md
```

## Prerequisites

- Node.js 18+
- Java 17+
- Maven 3.9+
- MySQL 8+

## Backend Setup

1. Create a MySQL database named `complaint_system`.
2. Update `backend/src/main/resources/application.properties` with your MySQL credentials.
3. Run the backend:

```bash
cd backend
.\mvnw.cmd spring-boot:run
```

Backend base URL: `http://localhost:8080`

If you already have Maven installed globally, `mvn spring-boot:run` also works. On Windows, the included wrapper is the simplest option.

## Frontend Setup

1. Install dependencies:

```bash
cd frontend
npm install
```

2. Start the frontend:

```bash
npm run dev
```

Frontend URL: `http://localhost:5173`

## API Overview

### Auth

- `POST /api/auth/register`
- `POST /api/auth/login`

### User Complaints

- `GET /api/complaints`
- `POST /api/complaints`
- `GET /api/complaints/{id}`

### Admin Complaints

- `GET /api/admin/complaints`
- `PUT /api/admin/complaints/{id}/status`
- `DELETE /api/admin/complaints/{id}`
- `GET /api/admin/summary`

## Default Demo Accounts

The backend seed data creates these accounts on first run:

- Admin: `admin@complaints.com` / `Admin@12345`
- User: `user@complaints.com` / `User@12345`

## Database

The SQL schema is available at `database/complaint_system.sql`.

## Deployment Notes

- Configure `VITE_API_BASE_URL` for the frontend when deploying.
- Configure production MySQL credentials and JWT secret in the backend.
- Run the Spring Boot service behind a reverse proxy if needed.
