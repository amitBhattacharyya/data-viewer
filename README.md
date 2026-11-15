<<<<<<< HEAD
# Data Viewer

A simple web application for exploring data stored in files with SQL. The project uses DuckDB to query file-based data formats such as Avro, Parquet, and CSV, and presents query results in a browser-based data grid.

## Current implementation

The query console currently loads `sample_records.avro` and provides example SQL queries for viewing its records and extracting fields from JSON columns. If the file is missing, the application generates it with sample records at startup. Avro is the format wired into the application today; Parquet and CSV are examples of formats the project aims to support, not currently selectable inputs in the UI.

The Avro dataset is exposed through these DuckDB views:

- `sample_records` and `avro_data` — the source records
- `parsed_avro` — records with JSON columns parsed
- `flattened_avro` — selected JSON fields extracted into columns

## Requirements

- JDK 17 or newer

## Run

On Windows:

```powershell
.\gradlew.bat bootRun
```

On macOS or Linux:

```shell
./gradlew bootRun
```

Open [http://localhost:8080](http://localhost:8080) in your browser. Enter a SQL query in the query console or choose one of the presets, then select **Execute Query** to view the results.

## Test

On Windows:

```powershell
.\gradlew.bat test
```

On macOS or Linux:

```shell
./gradlew test
```

## Technology

- Java 17
- Spring Boot
- DuckDB JDBC
- Apache Avro
- Thymeleaf
=======
# data-viewer
>>>>>>> f890461ebd76acb532fa860ee63ba96adb8e9c7e
