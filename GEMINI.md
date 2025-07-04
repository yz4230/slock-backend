/c# Gemini Project Overview: slock

## Project Description

This project is a web service built with the Ktor framework in Kotlin. It appears to be a chat application or a service
with similar features, given the name "slock" and concepts like "Channels" and "Users" in the codebase. The project uses
a PostgreSQL database with Flyway for migrations and the Exposed library for database access.

The API is defined using TypeSpec and OpenAPI, with a code generation step to create Kotlin data classes and API
definitions from the schema.

## Technologies Used

- **Backend:** Kotlin, Ktor
- **Database:** PostgreSQL, Flyway (migrations), Exposed (ORM/DSL)
- **API Specification:** TypeSpec, OpenAPI
- **Build Tool:** Gradle
- **Dependency Management:** Gradle, npm (for TypeSpec)
- **Task Runner:** Taskfile

## Project Structure

The project is a multi-module Gradle project:

- **`slock-openapi/`**: A submodule containing the TypeSpec (`.tsp`) files that define the API. Compiling this generates
  an `openapi.yaml` schema.
- **`codegen/`**: A Gradle subproject responsible for generating Kotlin source code from the `openapi.yaml` schema. The
  generated code is placed in `src/main/kotlin/dev/ishiyama/slock/generated`.
- **`src/`**: The main application source code.
    - **`src/main/kotlin`**: The Kotlin source code for the Ktor application.
        - `Application.kt`: The main entry point of the application.
        - `core/`: Core business logic, including repositories and use cases.
        - `generated/`: Kotlin code generated from the OpenAPI schema.
        - `petstore/`: Example code, possibly from the Ktor generator.
        - `scripts/`: Scripts for database migrations.
    - **`src/main/resources`**: Application configuration files (`application.yaml`, `logback.xml`).
- **`migrations/`**: SQL migration files managed by Flyway.

## Database Migrations

This project uses [Flyway](https://flywaydb.org/) to manage database migrations. Migration files are located in the
`migrations/` directory.

The workflow for updating the database schema is as follows:

1.  **Update Table Definitions:** Modify the table definition code in `src/main/kotlin/dev/ishiyama/slock/core/repository/Tables.kt`.
2.  **Generate Migration Script:** Run the following command to generate a new migration script:

    ```bash
    ./gradlew generateMigration --args=<migration_name>
    ```
    Replace `<migration_name>` with a descriptive name for your migration (e.g., `add_new_column_to_users_table`). This will create a new SQL file in the `migrations/` directory.

3.  **Apply Migrations:** Apply the pending migrations to the database by running:

    ```bash
    ./gradlew migrateDatabase
    ```
    This command executes any new migration scripts that haven't been applied yet.

## Common Commands

- **`./gradlew build`**: Compiles the entire project.
- **`./gradlew run`**: Starts the Ktor application.
- **`./gradlew test`**: Runs the test suite.
- **`task generateOpenApi`**: A custom task that runs the full API code generation process. This involves:
    1. Updating git submodules.
    2. Running `npm install` and `npm run build` in the `slock-openapi` directory to generate the `openapi.yaml` file.
    3. Running the `codegen` module to generate Kotlin files from the `openapi.yaml` schema.
