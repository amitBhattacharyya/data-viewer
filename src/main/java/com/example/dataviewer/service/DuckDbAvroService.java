package com.example.dataviewer.service;

import com.example.dataviewer.model.QueryResult;
import com.example.dataviewer.util.AvroGeneratorUtil;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.sql.*;
import java.util.*;

@Service
public class DuckDbAvroService {

    private static final Logger log = LoggerFactory.getLogger(DuckDbAvroService.class);
    private static final String AVRO_FILE_PATH = "sample_records.avro";
    private Connection connection;

    @PostConstruct
    public void init() {
        try {
            Class.forName("org.duckdb.DuckDBDriver");
            connection = DriverManager.getConnection("jdbc:duckdb:");
            log.info("Initialized DuckDB connection.");

            // Check if sample_records.avro exists, if not generate it
            File avroFile = new File(AVRO_FILE_PATH);
            if (!avroFile.exists()) {
                log.info("sample_records.avro does not exist. Generating with 100 records...");
                AvroGeneratorUtil.generateSampleAvro(avroFile, 100);
            }

            // Install & load DuckDB's avro extension to query the file directly
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("INSTALL avro FROM community;");
                stmt.execute("LOAD avro;");
                log.info("Successfully loaded DuckDB community avro extension.");

                // Register a view directly pointing to read_avro('<file>') with zero data copying
                String normalizedPath = avroFile.getAbsolutePath().replace("\\", "/");
                stmt.execute("CREATE OR REPLACE VIEW sample_records AS SELECT * FROM read_avro('" + normalizedPath + "');");
                stmt.execute("CREATE OR REPLACE VIEW avro_data AS SELECT * FROM read_avro('" + normalizedPath + "');");
                stmt.execute(" CREATE OR REPLACE VIEW parsed_avro AS\r\n" + 
                                        "SELECT\r\n" + 
                                        "    id,\r\n" + 
                                        "    name,\r\n" + 
                                        "    status,\r\n" + 
                                        "    /* parse the two JSON columns */\r\n" + 
                                        "    json(metadataJson)   AS meta,\r\n" + 
                                        "    json(payloadJson)   AS payload\r\n" + 
                                        "FROM avro_data ");
                stmt.execute(" CREATE OR REPLACE VIEW flattened_avro AS\r\n" + //
                                        "SELECT\r\n" + //
                                        "    id,\r\n" + //
                                        "    name,\r\n" + //
                                        "    status,\r\n" + //
                                        "\r\n" + //
                                        "    /* ----- metadataJson fields ----- */\r\n" + //
                                        "    json_extract_string(meta, '$.source') AS meta_source,\r\n" + //
                                        "    json_extract_string(meta, '$.version') AS meta_version,\r\n" + //
                                        "    CAST(json_extract_string(meta, '$.retryCount') AS INTEGER) AS meta_retry_count,\r\n" + //
                                        "    CAST(json_extract(meta, '$.tags') AS VARCHAR[]) AS meta_tags,\r\n" + //
                                        "    /* ----- payloadJson fields ----- */\r\n" + //
                                        "    CAST(json_extract_string(payload, '$.userId') AS BIGINT) AS payload_user_id,\r\n" + //
                                        "    json_extract_string(payload, '$.department') AS payload_department,\r\n" + //
                                        "    CAST(json_extract_string(payload, '$.score') AS DOUBLE) AS payload_score,\r\n" + //
                                        "    CAST(json_extract_string(payload, '$.preferences.newsletter') AS BOOLEAN) AS payload_pref_newsletter,\r\n" + //
                                        "    json_extract_string(payload, '$.preferences.theme') AS payload_pref_theme\r\n" + //
                                        "FROM parsed_avro ");
                log.info("Registered views 'sample_records' and 'avro_data' over read_avro('{}')", normalizedPath);
            }
        } catch (Exception e) {
            log.error("Failed to initialize DuckDbAvroService with direct avro extension", e);
        }
    }

    public synchronized QueryResult executeQuery(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            return QueryResult.error("SQL query cannot be empty.");
        }

        long start = System.currentTimeMillis();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            ResultSetMetaData meta = rs.getMetaData();
            int columnCount = meta.getColumnCount();
            List<String> columns = new ArrayList<>();
            for (int i = 1; i <= columnCount; i++) {
                String label = meta.getColumnLabel(i);
                if (label == null || label.trim().isEmpty()) {
                    label = meta.getColumnName(i);
                }
                columns.add(label);
            }

            List<Map<String, Object>> rows = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    Object val = rs.getObject(i);
                    row.put(columns.get(i - 1), String.valueOf(val));
                }
                rows.add(row);
            }

            long elapsed = System.currentTimeMillis() - start;
            return QueryResult.success(columns, rows, elapsed);
        } catch (SQLException e) {
            log.warn("SQL Query execution failed: {}", e.getMessage());
            return QueryResult.error("Query Error: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error executing query", e);
            return QueryResult.error("Unexpected Error: " + e.getMessage());
        }
    }

    @PreDestroy
    public void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ignored) {
            }
        }
    }
}
