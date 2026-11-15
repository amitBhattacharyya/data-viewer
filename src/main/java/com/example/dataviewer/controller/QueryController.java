package com.example.dataviewer.controller;

import com.example.dataviewer.model.QueryRequest;
import com.example.dataviewer.model.QueryResult;
import com.example.dataviewer.service.DuckDbAvroService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/query")
public class QueryController {

    private final DuckDbAvroService duckDbAvroService;

    public QueryController(DuckDbAvroService duckDbAvroService) {
        this.duckDbAvroService = duckDbAvroService;
    }

    @PostMapping
    public ResponseEntity<QueryResult> runQuery(@RequestBody QueryRequest request) {
        String sql = request != null ? request.getQuery() : null;
        QueryResult result = duckDbAvroService.executeQuery(sql);
        return ResponseEntity.ok(result);
    }
}
