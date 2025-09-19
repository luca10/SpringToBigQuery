package com.example.controller;

import com.example.service.PerformanceMonitoringService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/bigquery/performance")
@Tag(name = "Monitoraggio Performance", description = "API per il monitoraggio e l'analisi delle performance delle query BigQuery")
public class PerformanceMonitoringController {

    @Autowired
    private PerformanceMonitoringService performanceMonitoringService;

    @Operation(
        summary = "Ottieni statistiche performance", 
        description = "Restituisce statistiche complete sulle performance delle query"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Statistiche recuperate con successo"),
        @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStatistichePerformance() {
        try {
            Map<String, Object> stats = performanceMonitoringService.getStatistichePerformance();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "errore", "Errore durante il recupero delle statistiche: " + e.getMessage()
            ));
        }
    }

    @Operation(
        summary = "Analizza trend temporali", 
        description = "Analizza i trend delle performance in un periodo di tempo specifico"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Analisi trend completata"),
        @ApiResponse(responseCode = "400", description = "Parametri di data non validi"),
        @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    @GetMapping("/trend")
    public ResponseEntity<Map<String, Object>> getAnalisiTrend(
            @Parameter(description = "Data di inizio (formato: yyyy-MM-ddTHH:mm:ss)", required = true)
            @RequestParam String inizio,
            @Parameter(description = "Data di fine (formato: yyyy-MM-ddTHH:mm:ss)", required = true)
            @RequestParam String fine) {
        try {
            LocalDateTime dataInizio = LocalDateTime.parse(inizio);
            LocalDateTime dataFine = LocalDateTime.parse(fine);
            
            Map<String, Object> trend = performanceMonitoringService.getAnalisiTrend(dataInizio, dataFine);
            return ResponseEntity.ok(trend);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "errore", "Errore nei parametri di data: " + e.getMessage()
            ));
        }
    }

    @Operation(
        summary = "Analizza utilizzo per utenti", 
        description = "Analizza l'utilizzo delle query per indirizzo IP"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Analisi utenti completata"),
        @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    @GetMapping("/users")
    public ResponseEntity<Map<String, Object>> getAnalisiUtenti() {
        try {
            Map<String, Object> analisi = performanceMonitoringService.getAnalisiUtenti();
            return ResponseEntity.ok(analisi);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "errore", "Errore durante l'analisi utenti: " + e.getMessage()
            ));
        }
    }

    @Operation(
        summary = "Ottieni raccomandazioni di ottimizzazione", 
        description = "Genera raccomandazioni per migliorare le performance delle query"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Raccomandazioni generate con successo"),
        @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    @GetMapping("/recommendations")
    public ResponseEntity<Map<String, Object>> getRaccomandazioniOttimizzazione() {
        try {
            Map<String, Object> raccomandazioni = performanceMonitoringService.getRaccomandazioniOttimizzazione();
            return ResponseEntity.ok(raccomandazioni);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "errore", "Errore durante la generazione delle raccomandazioni: " + e.getMessage()
            ));
        }
    }

    @Operation(
        summary = "Dashboard performance completa", 
        description = "Restituisce tutti i dati necessari per una dashboard delle performance"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Dashboard generata con successo"),
        @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardPerformance() {
        try {
            Map<String, Object> dashboard = Map.of(
                "statistiche", performanceMonitoringService.getStatistichePerformance(),
                "raccomandazioni", performanceMonitoringService.getRaccomandazioniOttimizzazione(),
                "analisiUtenti", performanceMonitoringService.getAnalisiUtenti(),
                "timestamp", LocalDateTime.now()
            );
            return ResponseEntity.ok(dashboard);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "errore", "Errore durante la generazione della dashboard: " + e.getMessage()
            ));
        }
    }
}


