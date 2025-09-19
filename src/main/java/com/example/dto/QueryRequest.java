package com.example.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request object for BigQuery operations")
public class QueryRequest {
    
    @NotBlank(message = "SQL query cannot be empty")
    @Size(min = 1, max = 10000, message = "SQL query must be between 1 and 10000 characters")
    @Schema(description = "SQL query to execute", example = "SELECT * FROM `project.dataset.table` LIMIT 10", required = true)
    private String sql;
    
    @Schema(description = "Whether to export results as CSV", example = "false", defaultValue = "false")
    private boolean export = false;

    // Constructors
    public QueryRequest() {}
    
    public QueryRequest(String sql, boolean export) {
        this.sql = sql;
        this.export = export;
    }

    // Getters and Setters
    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }
    
    public boolean isExport() {
        return export;
    }

    public void setExport(boolean export) {
        this.export = export;
    }
}