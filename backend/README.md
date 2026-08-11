# FactoryFlow Backend

## Local prerequisites

- Java 21
- PostgreSQL with a `factoryflow` database
- `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, and `JWT_SECRET` set in the shell environment

PowerShell example (replace the password locally; do not commit it):

```powershell
$env:JAVA_HOME='D:\Java JDK 21'
$env:DB_URL='jdbc:postgresql://localhost:5432/factoryflow'
$env:DB_USERNAME='postgres'
$env:DB_PASSWORD='<local-password>'
$env:JWT_SECRET='<at-least-32-random-characters>'
$env:REPORT_STORAGE_ROOT='.\data\generated-reports'
```

Run tests:

```powershell
.\mvnw.cmd test
```

Foundation integration tests use the PostgreSQL database configured by the same
environment variables. Test-created user rows run inside transactions and roll back.
Never point this test configuration at a production database.

Run the backend:

```powershell
.\mvnw.cmd spring-boot:run
```

After startup:

- Health: `http://localhost:8080/actuator/health`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
