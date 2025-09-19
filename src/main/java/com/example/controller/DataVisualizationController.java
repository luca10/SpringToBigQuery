package com.example.controller;

import com.example.service.DataVisualizationService;
import com.example.service.BigQueryService;
import com.example.dto.QueryRequest;
import com.google.cloud.bigquery.TableResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/bigquery/visualization")
@Tag(name = "Visualizzazione Dati", description = "API per la creazione di grafici e visualizzazioni dai dati BigQuery")
public class DataVisualizationController {

    @Autowired
    private DataVisualizationService dataVisualizationService;
    
    @Autowired
    private BigQueryService bigQueryService;

    @Operation(
        summary = "Genera configurazione grafico", 
        description = "Genera la configurazione per un grafico basato sui risultati di una query BigQuery"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Configurazione grafico generata con successo"),
        @ApiResponse(responseCode = "400", description = "Parametri non validi"),
        @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    @PostMapping("/chart")
    public ResponseEntity<Map<String, Object>> generaGrafico(
            @Parameter(description = "Query SQL e tipo di grafico", required = true)
            @RequestBody Map<String, String> request) {
        
        String sql = request.get("sql");
        String tipoGrafico = request.getOrDefault("tipo", "bar");
        
        if (sql == null || sql.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                "errore", "Query SQL non fornita"
            ));
        }
        
        try {
            // Esegui la query
            TableResult result = bigQueryService.runQuery(sql);
            
            // Genera la configurazione del grafico
            Map<String, Object> config = dataVisualizationService.generaConfigurazioneGrafico(result, tipoGrafico);
            
            return ResponseEntity.ok(config);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "errore", "Errore durante la generazione del grafico: " + e.getMessage()
            ));
        }
    }

    @Operation(
        summary = "Ottieni tipi di grafico disponibili", 
        description = "Restituisce la lista dei tipi di grafico supportati"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista tipi grafico recuperata con successo")
    })
    @GetMapping("/tipi")
    public ResponseEntity<Map<String, Object>> getTipiGrafico() {
        Map<String, Object> tipi = Map.of(
            "tipi", new String[]{
                "bar", "line", "pie", "scatter", "histogram", "table"
            },
            "descrizioni", Map.of(
                "bar", "Grafico a Barre",
                "line", "Grafico a Linee", 
                "pie", "Grafico a Torta",
                "scatter", "Scatter Plot",
                "histogram", "Istogramma",
                "table", "Tabella Dati"
            )
        );
        
        return ResponseEntity.ok(tipi);
    }

    @Operation(
        summary = "Genera grafico con parametri avanzati", 
        description = "Genera un grafico con parametri di personalizzazione avanzati"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Configurazione grafico generata con successo"),
        @ApiResponse(responseCode = "400", description = "Parametri non validi"),
        @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    @PostMapping("/chart-advanced")
    public ResponseEntity<Map<String, Object>> generaGraficoAvanzato(
            @Parameter(description = "Parametri avanzati per la generazione del grafico", required = true)
            @RequestBody Map<String, Object> request) {
        
        String sql = (String) request.get("sql");
        String tipoGrafico = (String) request.getOrDefault("tipo", "bar");
        String titolo = (String) request.getOrDefault("titolo", "");
        
        if (sql == null || sql.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                "errore", "Query SQL non fornita"
            ));
        }
        
        try {
            // Esegui la query
            TableResult result = bigQueryService.runQuery(sql);
            
            // Genera la configurazione del grafico
            Map<String, Object> config = dataVisualizationService.generaConfigurazioneGrafico(result, tipoGrafico);
            
            // Personalizza il titolo se fornito
            if (!titolo.isEmpty()) {
                config.put("titolo", titolo);
            }
            
            // Aggiungi metadati
            config.put("sql", sql);
            config.put("timestamp", java.time.LocalDateTime.now());
            config.put("numeroRighe", result.getTotalRows());
            
            return ResponseEntity.ok(config);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "errore", "Errore durante la generazione del grafico: " + e.getMessage()
            ));
        }
    }
}


