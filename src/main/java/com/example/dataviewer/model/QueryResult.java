package com.example.dataviewer.model;

import java.util.List;
import java.util.Map;

public class QueryResult {
    private boolean success;
    private String error;
    private List<String> columns;
    private List<Map<String, Object>> rows;
    private int rowCount;
    private long executionTimeMs;

    public QueryResult() {
    }

    public static QueryResult success(List<String> columns, List<Map<String, Object>> rows, long executionTimeMs) {
        QueryResult result = new QueryResult();
        result.success = true;
        result.columns = columns;
        result.rows = rows;
        result.rowCount = rows != null ? rows.size() : 0;
        result.executionTimeMs = executionTimeMs;
        return result;
    }

    public static QueryResult error(String error) {
        QueryResult result = new QueryResult();
        result.success = false;
        result.error = error;
        return result;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public List<String> getColumns() {
        return columns;
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }

    public List<Map<String, Object>> getRows() {
        return rows;
    }

    public void setRows(List<Map<String, Object>> rows) {
        this.rows = rows;
    }

    public int getRowCount() {
        return rowCount;
    }

    public void setRowCount(int rowCount) {
        this.rowCount = rowCount;
    }

    public long getExecutionTimeMs() {
        return executionTimeMs;
    }

    public void setExecutionTimeMs(long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }
}
