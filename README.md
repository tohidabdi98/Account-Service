# Account Service

A Spring Boot REST API for corporate employee accounts and payroll management.

## Features

- User registration and HTTP Basic authentication
- Password security and password changes
- Role-based authorization:
  - `ROLE_ADMINISTRATOR`
  - `ROLE_USER`
  - `ROLE_ACCOUNTANT`
  - `ROLE_AUDITOR`
- Payroll upload, correction, and employee salary lookup
- Persistent security-event logging
- Brute-force detection and account locking
- HTTPS with a self-signed PKCS12 certificate

## Running

Requirements:

- Java 17+
- Maven

Start the service with:

```bash
mvn spring-boot:run
```

The API listens on HTTPS port `28852`.

Because the project uses a self-signed certificate, clients must trust or temporarily allow the certificate when testing locally.

The certificate is stored at:

```text
src/main/resources/keystore/service.p12
```

Keystore password: `service`

## Main endpoints

| Method | Endpoint | Access |
|---|---|---|
| POST | `/api/auth/signup` | Public |
| POST | `/api/auth/changepass` | Authenticated users |
| GET | `/api/empl/payment` | User, Accountant |
| POST | `/api/acct/payments` | Accountant |
| PUT | `/api/acct/payments` | Accountant |
| GET | `/api/admin/user` | Administrator |
| PUT | `/api/admin/user/role` | Administrator |
| PUT | `/api/admin/user/access` | Administrator |
| DELETE | `/api/admin/user/{email}` | Administrator |
| GET | `/api/security/events` | Auditor |

## Database

The service uses a persistent H2 file database configured in
`src/main/resources/application.properties`:

```text
spring.datasource.url=jdbc:h2:file:../service_db
```

## Testing

```bash
mvn test
```
