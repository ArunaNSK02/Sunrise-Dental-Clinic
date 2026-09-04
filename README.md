# Sunrise Dental Clinic — Appointment & Patient Management System

CIS6003 Advanced Programming coursework (Cardiff Met). A 3-tier
Java Servlet application — presentation (Servlets) / business (services)
/ data access (DAO) — deployed to Apache Tomcat, backed by MySQL.

## Stack

- Java 17, Maven, packaged as a WAR
- Jakarta Servlet 6.0 (Apache Tomcat 11) — `@WebServlet` annotations, no Spring
- MySQL 8, plain JDBC via a Singleton `DBConnectionManager`
- JUnit 5

## Project structure

```
src/main/java/com/sunrisedental/
  model/      domain entities + enums (class diagram)
  dao/        DAO interfaces (data access contracts)
  dao/impl/   JDBC DAO implementations
  db/         DBConnectionManager (Singleton)
  service/    business logic tier
  web/        Servlets (presentation tier)
src/main/webapp/       JSPs, static welcome page, web.xml
src/main/resources/    schema.sql, db.properties.example
src/test/java/         JUnit 5 tests
docs/                  DESIGN.md, HANDOFF.md, UML sources
```

## Running locally

1. Create the database and load the schema:
   ```
   mysql -u root -p -e "CREATE DATABASE sunrise_dental"
   mysql -u root -p sunrise_dental < src/main/resources/schema.sql
   ```
2. Copy `src/main/resources/db.properties.example` to
   `src/main/resources/db.properties` and fill in your local MySQL
   username/password (this file is gitignored — never commit real
   credentials).
3. Build: `mvn package` — produces `target/sunrise-dental-clinic.war`.
4. Deploy the WAR to Apache Tomcat 11 (e.g. drop it in `webapps/`), or
   run `mvn test` to run the automated test suite on its own.
