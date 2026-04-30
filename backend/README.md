# NPAxis Backend

NPAxis is a platform designed to facilitate interactions between students and preceptors (Nurse Practitioners, MDs,
etc.). The backend is built using Spring Boot, providing a robust and scalable REST API.

## 🚀 Features

- **User Authentication:** Secure login, registration, and JWT-based session management.
- **Role-Based Access Control:** Distinct functionalities for Students, Preceptors, and Administrators.
- **Analytics & Reporting:** Tracking and reporting of platform activities.
- **Email Notifications:** Automated email delivery for OTP verification and system notifications.
- **API Documentation:** Interactive Swagger UI for easy API exploration and testing.

## 🛠️ Tech Stack

- **Java:** 25
- **Framework:** Spring Boot 4.0.3
- **Database:** PostgreSQL
- **Security:** Spring Security with JWT
- **Email Service:** Spring Boot Starter Mail (SMTP)
- **Documentation:** SpringDoc OpenAPI (Swagger UI)
- **Build Tool:** Maven

## 📋 Prerequisites

Before you begin, ensure you have the following installed on your local machine:

- **JDK 25:** Ensure your `JAVA_HOME` is set to JDK 25.
- **Maven:** For building and managing dependencies.
- **PostgreSQL:** A running PostgreSQL instance.

## ⚙️ Local Setup

Follow these steps to get the project running locally:

### 1. Database Configuration

1. **Create a Database:** Open your PostgreSQL client (like pgAdmin or psql) and create a database named `npaxis`.
2. **Configure Credentials:** Open `src/main/resources/application.yaml` and update the `spring.datasource.username` and
   `spring.datasource.password` fields with your local PostgreSQL credentials.

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/npaxis
    username: your_username
    password: your_password
```

### 2. Email Configuration (Optional for Local)

The system uses Gmail's SMTP server by default. You can update the credentials in `application.yaml` if you need to test
email functionality.

```yaml
spring:
  mail:
    username: your_email@gmail.com
    password: your_app_password
```

### 3. Build the Project

Run the following command in the `backend` directory to build the project and install dependencies:

```bash
mvn clean install
```

### 4. Run the Application

You can start the application using the Maven wrapper:

```bash
./mvnw spring-boot:run
```

The application will start on `http://localhost:8080`.

## 📖 API Documentation

- **Swagger UI:** Once the application is running, you can access the interactive API documentation
  at: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- **API Reference:** For a detailed breakdown of available endpoints, refer to the [APIs readme.md](./APIs%20readme.md)
  file.

## 🔑 Initializing the System

To set up default roles and an admin user, you can call the initialization endpoint after the application starts:

- **Endpoint:** `POST /api/v1/auth/initialize`
- **Description:** Initializes default roles (Student, Preceptor, Admin) and creates a default admin user.

---
© 2026 NPAxis Platform. All rights reserved.
