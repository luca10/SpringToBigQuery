package com.example.service;

import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.bigquery.*;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
public class BigQueryService {

    private final BigQuery bigQuery;

    public BigQueryService() {
    	try {
            ClassPathResource resource = new ClassPathResource("APIGoogleBQ.json");

            this.bigQuery = BigQueryOptions.newBuilder()
                    .setProjectId("api-project-720362412105")
                    .setCredentials(ServiceAccountCredentials.fromStream(resource.getInputStream()))
                    .build()
                    .getService();

        } catch (IOException e) {
            throw new RuntimeException("Errore nel caricamento delle credenziali BigQuery", e);
        }
    }

    public TableResult runQuery(String sql) throws InterruptedException {
        QueryJobConfiguration queryConfig = QueryJobConfiguration.newBuilder(sql).build();
        Job queryJob = bigQuery.create(JobInfo.newBuilder(queryConfig).build());

        // Attendi che la query finisca
        queryJob = queryJob.waitFor();

        if (queryJob == null) {
            throw new RuntimeException("La query è stata interrotta");
        } else if (queryJob.getStatus().getError() != null) {
            throw new RuntimeException(queryJob.getStatus().getError().toString());
        }

        return queryJob.getQueryResults();
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
}