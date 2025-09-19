package com.example.service;

import com.google.cloud.bigquery.*;
import com.example.dto.StructuredQueryRequest;
import com.example.entity.QueryHistory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class BigQueryService {

    private final BigQuery bigQuery;
    
    @Value("${bigquery.location:US}")
    private String jobLocation;
    
    @Autowired
    private QueryHistoryService queryHistoryService;

    public BigQueryService(BigQuery bigQuery) {
        this.bigQuery = bigQuery;
    }

    public TableResult runQuery(String sql) throws InterruptedException {
        QueryJobConfiguration queryConfig = QueryJobConfiguration.newBuilder(sql).build();
        JobId jobId = JobId.newBuilder().setLocation(jobLocation).build();
        Job queryJob = bigQuery.create(JobInfo.newBuilder(queryConfig).setJobId(jobId).build());

        // Attendi che la query finisca
        queryJob = queryJob.waitFor();

        if (queryJob == null) {
            throw new RuntimeException("La query è stata interrotta");
        } else {
            JobStatus status = queryJob.getStatus();
            if (status != null && status.getError() != null) {
                throw new RuntimeException(status.getError().toString());
            }
        }

        return queryJob.getQueryResults();
    }

    public TableResult runQuery(String sql, Map<String, QueryParameterValue> namedParameters) throws InterruptedException {
        QueryJobConfiguration.Builder builder = QueryJobConfiguration.newBuilder(sql);
        if (namedParameters != null && !namedParameters.isEmpty()) {
            builder.setNamedParameters(namedParameters);
        }
        JobId jobId = JobId.newBuilder().setLocation(jobLocation).build();
        Job queryJob = bigQuery.create(JobInfo.newBuilder(builder.build()).setJobId(jobId).build());

        queryJob = queryJob.waitFor();

        if (queryJob == null) {
            throw new RuntimeException("La query è stata interrotta");
        } else {
            JobStatus status = queryJob.getStatus();
            if (status != null && status.getError() != null) {
                throw new RuntimeException(status.getError().toString());
            }
        }

        return queryJob.getQueryResults();
    }
    
    public TableResult runQueryWithHistory(String sql, String userIp) throws InterruptedException {
        long startTime = System.currentTimeMillis();
        QueryHistory queryHistory = null;
        
        try {
            // Salva la query nella cronologia
            queryHistory = queryHistoryService.salvaQuery(sql, userIp);
            
            // Esegui la query
            TableResult result = runQuery(sql);
            
            // Calcola il tempo di esecuzione
            long executionTime = System.currentTimeMillis() - startTime;
            
            // Conta le righe restituite
            int rowCount = (int) result.getTotalRows();
            
            // Aggiorna la cronologia con i risultati
            queryHistoryService.aggiornaQueryCompletata(
                queryHistory.getId(), 
                executionTime, 
                rowCount, 
                true, 
                null
            );
            
            return result;
            
        } catch (Exception e) {
            // Calcola il tempo di esecuzione anche in caso di errore
            long executionTime = System.currentTimeMillis() - startTime;
            
            // Aggiorna la cronologia con l'errore
            if (queryHistory != null) {
                queryHistoryService.aggiornaQueryCompletata(
                    queryHistory.getId(), 
                    executionTime, 
                    0, 
                    false, 
                    e.getMessage()
                );
            }
            
            throw e;
        }
    }
    
     public List<Map<String, Object>> toList(TableResult result) {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (FieldValueList row : result.iterateAll()) {
            Map<String, Object> map = new HashMap<>();
            Schema schema = result.getSchema();
            for (Field field : schema.getFields()) {
                map.put(field.getName(), row.get(field.getName()).getValue());
            }
            rows.add(map);
        }
        return rows;
    }

     public void writeCsv(TableResult result, Writer writer) {
        try (BufferedWriter bw = new BufferedWriter(writer)) {
            Schema schema = result.getSchema();
            List<String> headers = new ArrayList<>();
            for (Field field : schema.getFields()) {
                headers.add(field.getName());
            }
            bw.write(String.join(",", headers));
            bw.newLine();

            for (FieldValueList row : result.iterateAll()) {
                List<String> values = new ArrayList<>();
                for (String h : headers) {
                    FieldValue value = row.get(h);
                    values.add(value.isNull() ? "" : value.getValue().toString());
                }
                bw.write(String.join(",", values));
                bw.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("Errore durante la scrittura del CSV", e);
        }
    }

    // ===== Query strutturata (no-SQL per l'utente) =====

    public static class BuiltQuery {
        private final String sql;
        private final Map<String, QueryParameterValue> params;

        public BuiltQuery(String sql, Map<String, QueryParameterValue> params) {
            this.sql = sql;
            this.params = params;
        }

        public String getSql() { return sql; }
        public Map<String, QueryParameterValue> getParams() { return params; }
    }

    public BuiltQuery buildQuery(StructuredQueryRequest request) {
        String tableRef = request.getTable();
        if (tableRef == null || tableRef.isBlank()) {
            throw new IllegalArgumentException("Tabella non specificata");
        }

        // Sanitize identifier parts and quote with backticks
        String quotedTable = quoteTableIdentifier(tableRef);

        List<String> selectCols = request.getSelect();
        String selectClause;
        if (selectCols == null || selectCols.isEmpty()) {
            selectClause = "*";
        } else {
            List<String> safeCols = new ArrayList<>();
            for (String c : selectCols) {
                safeCols.add(quoteIdentifier(c));
            }
            selectClause = String.join(", ", safeCols);
        }

        StringBuilder where = new StringBuilder();
        Map<String, QueryParameterValue> params = new LinkedHashMap<>();
        if (request.getFilters() != null && !request.getFilters().isEmpty()) {
            where.append(" WHERE ");
            int i = 0;
            for (Map.Entry<String, Object> e : request.getFilters().entrySet()) {
                if (i > 0) where.append(" AND ");
                String fieldName = e.getKey();
                String paramName = "p_" + i;
                where.append(quoteIdentifier(fieldName)).append(" = @").append(paramName);
                params.put(paramName, toQueryParameterValue(e.getValue()));
                i++;
            }
        }

        StringBuilder order = new StringBuilder();
        if (request.getOrderBy() != null && !request.getOrderBy().isEmpty()) {
            order.append(" ORDER BY ");
            List<String> parts = new ArrayList<>();
            for (String raw : request.getOrderBy()) {
                if (raw == null || raw.isBlank()) {
                    continue;
                }
                boolean desc = raw.startsWith("-");
                String col = desc ? raw.substring(1) : raw;
                parts.add(quoteIdentifier(col) + (desc ? " DESC" : " ASC"));
            }
            order.append(String.join(", ", parts));
        }

        Integer limit = request.getLimit();
        if (limit == null || limit <= 0) {
            limit = 100;
        }

        String sql = "SELECT " + selectClause +
                " FROM " + quotedTable +
                where.toString() +
                order.toString() +
                " LIMIT " + limit;

        return new BuiltQuery(sql, params);
    }

    public TableResult runStructuredQueryWithHistory(StructuredQueryRequest req, String userIp) throws InterruptedException {
        long startTime = System.currentTimeMillis();
        QueryHistory queryHistory = null;

        BuiltQuery built = buildQuery(req);
        try {
            // Salva SQL generato nella cronologia
            queryHistory = queryHistoryService.salvaQuery(built.getSql(), userIp);

            TableResult result = runQuery(built.getSql(), built.getParams());

            long executionTime = System.currentTimeMillis() - startTime;
            int rowCount = (int) result.getTotalRows();

            queryHistoryService.aggiornaQueryCompletata(
                queryHistory.getId(),
                executionTime,
                rowCount,
                true,
                null
            );

            return result;
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            if (queryHistory != null) {
                queryHistoryService.aggiornaQueryCompletata(
                    queryHistory.getId(),
                    executionTime,
                    0,
                    false,
                    e.getMessage()
                );
            }
            throw e;
        }
    }

    private String quoteTableIdentifier(String tableRef) {
        // Accept forms: project.dataset.table OR dataset.table
        String[] parts = tableRef.split("\\.");
        if (parts.length < 2 || parts.length > 3) {
            throw new IllegalArgumentException("Table deve essere 'dataset.table' o 'project.dataset.table'");
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) sb.append(".");
            sb.append("`").append(parts[i]).append("`");
        }
        return sb.toString();
    }

    private String quoteIdentifier(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            throw new IllegalArgumentException("Nome colonna non valido");
        }
        // BigQuery consente backtick quoting, evitiamo injection su identifier
        return "`" + identifier.replace("`", "") + "`";
    }

    private QueryParameterValue toQueryParameterValue(Object value) {
        if (value == null) return QueryParameterValue.of(null, StandardSQLTypeName.STRING);
        if (value instanceof Number) {
            return QueryParameterValue.int64(((Number) value).longValue());
        }
        if (value instanceof Boolean) {
            return QueryParameterValue.bool((Boolean) value);
        }
        // default string
        return QueryParameterValue.string(String.valueOf(value));
    }
}