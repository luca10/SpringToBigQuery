package com.example.controller;

import com.example.service.BigQueryService;
import com.google.cloud.bigquery.TableResult;
import com.example.dto.*;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/bigquery")
public class BigQueryController {

    private final BigQueryService bigQueryService;

    @Autowired
    public BigQueryController(BigQueryService bigQueryService) {
        this.bigQueryService = bigQueryService;
    }

    // @GetMapping("/query")
    // public String runQuery(@RequestParam(defaultValue = "SELECT CURRENT_DATE()") String sql) {
    //     try {
    //         TableResult result = bigQueryService.runQuery(sql);

    //         // Per esempio: ritorna la prima riga del primo campo
    //         StringBuilder output = new StringBuilder();
    //         result.iterateAll().forEach(row -> {
    //             row.forEach(val -> output.append(val.getValue()).append("\n"));
    //         });


    //         return output.toString();

    //     } catch (Exception e) {
    //         return "Errore durante l'esecuzione della query: " + e.getMessage();
    //     }
    // }
    @PostMapping("/query")
       public Object query(@RequestBody QueryRequest request, HttpServletResponse response) throws InterruptedException{
              TableResult result = bigQueryService.runQuery(request.getSql());
         if (request.isExport()) {
            response.setHeader("Content-Disposition", "attachment; filename=\"export.csv\"");
            response.setContentType("text/csv");
            try {
                bigQueryService.writeCsv(result, response.getWriter());
            } catch (IOException e) {
                throw new RuntimeException("Errore durante l'esportazione CSV", e);
            }
            return null;
        } else {
            return bigQueryService.toList(result);
        }
    }
    
}
