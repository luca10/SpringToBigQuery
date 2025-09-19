package com.example.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "query_history")
public class QueryHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "sql_query", columnDefinition = "TEXT")
    private String sqlQuery;
    
    @Column(name = "execution_time_ms")
    private Long executionTimeMs;
    
    @Column(name = "rows_returned")
    private Integer rowsReturned;
    
    @Column(name = "success")
    private Boolean success;
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "user_ip")
    private String userIp;
    
    // Costruttori
    public QueryHistory() {
        this.createdAt = LocalDateTime.now();
    }
    
    public QueryHistory(String sqlQuery, String userIp) {
        this();
        this.sqlQuery = sqlQuery;
        this.userIp = userIp;
    }
    
    // Getters e Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getSqlQuery() {
        return sqlQuery;
    }
    
    public void setSqlQuery(String sqlQuery) {
        this.sqlQuery = sqlQuery;
    }
    
    public Long getExecutionTimeMs() {
        return executionTimeMs;
    }
    
    public void setExecutionTimeMs(Long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }
    
    public Integer getRowsReturned() {
        return rowsReturned;
    }
    
    public void setRowsReturned(Integer rowsReturned) {
        this.rowsReturned = rowsReturned;
    }
    
    public Boolean getSuccess() {
        return success;
    }
    
    public void setSuccess(Boolean success) {
        this.success = success;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getUserIp() {
        return userIp;
    }
    
    public void setUserIp(String userIp) {
        this.userIp = userIp;
    }
}

