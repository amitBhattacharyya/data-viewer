package com.example.dataviewer;

import com.example.dataviewer.model.QueryResult;
import com.example.dataviewer.service.DuckDbAvroService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DataViewerApplicationTests {

    @Autowired
    private DuckDbAvroService duckDbAvroService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testDuckDbAvroServiceQuery() {
        QueryResult result = duckDbAvroService.executeQuery("SELECT * FROM sample_records LIMIT 5");
        assertTrue(result.isSuccess());
        assertEquals(5, result.getColumns().size());
        assertEquals(5, result.getRowCount());
        assertTrue(result.getColumns().contains("id"));
        assertTrue(result.getColumns().contains("metadataJson"));
        assertTrue(result.getColumns().contains("payloadJson"));
    }

    @Test
    void testDynamicColumnsAndJsonFunctions() {
        QueryResult result = duckDbAvroService.executeQuery(
                "SELECT id, name, json_extract_string(metadataJson, '$.source') AS source, " +
                "json_extract_string(payloadJson, '$.department') AS dept FROM sample_records LIMIT 3"
        );
        assertTrue(result.isSuccess());
        assertEquals(4, result.getColumns().size());
        assertEquals(3, result.getRowCount());
        assertEquals("source", result.getColumns().get(2));
        assertEquals("dept", result.getColumns().get(3));
    }

    @Test
    void testFlattenedAvroMetadataFilter() {
        QueryResult result = duckDbAvroService.executeQuery(
                "SELECT * FROM flattened_avro WHERE meta_source = 'WEB_PORTAL'"
        );
        assertTrue(result.isSuccess(), result.getError());
        assertEquals(100, result.getRowCount());
    }

    @Test
    void testFlattenedAvroFieldsCanBeFilteredByTheirNativeTypes() {
        QueryResult result = duckDbAvroService.executeQuery(
                "SELECT * FROM flattened_avro " +
                "WHERE meta_version = '1.0.0' " +
                "AND meta_retry_count = 1 " +
                "AND list_contains(meta_tags, 'avro') " +
                "AND payload_user_id = 1 " +
                "AND payload_department = 'Engineering' " +
                "AND payload_score = 71.5 " +
                "AND payload_pref_newsletter = false " +
                "AND payload_pref_theme = 'light'"
        );
        assertTrue(result.isSuccess(), result.getError());
        assertEquals(1, result.getRowCount());
    }

    @Test
    void testQueryControllerApi() throws Exception {
        mockMvc.perform(post("/api/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"query\":\"SELECT id, status FROM sample_records WHERE id <= 2\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.rowCount").value(2))
                .andExpect(jsonPath("$.columns[0]").value("id"))
                .andExpect(jsonPath("$.columns[1]").value("status"));
    }
}
