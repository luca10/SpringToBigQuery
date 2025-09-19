package com.example.controller;

import com.example.service.BigQueryService;
import com.example.service.QueryValidationService;
import com.example.service.QueryValidationService.ValidationResult;
import com.google.cloud.bigquery.TableResult;
import com.example.dto.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

@RestController
@RequestMapping("/bigquery")
@Tag(name = "BigQuery", description = "API per l'esecuzione di query su Google BigQuery")
public class BigQueryController {

    private final BigQueryService bigQueryService;
    private final QueryValidationService queryValidationService;

    @Autowired
    public BigQueryController(BigQueryService bigQueryService, QueryValidationService queryValidationService) {
        this.bigQueryService = bigQueryService;
        this.queryValidationService = queryValidationService;
    }

    @Operation(
        summary = "Esegui query BigQuery", 
        description = "Esegue una query SQL su Google BigQuery e restituisce i risultati in formato JSON o CSV"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Query eseguita con successo", 
                    content = @Content(schema = @Schema(implementation = Object.class))),
        @ApiResponse(responseCode = "400", description = "Richiesta non valida - query SQL mancante o non valida"),
        @ApiResponse(responseCode = "500", description = "Errore interno del server durante l'esecuzione della query")
    })
    @PostMapping("/query")
    public ResponseEntity<?> eseguiQuery(
            @Parameter(description = "Dettagli della query da eseguire", required = true)
            @Valid @RequestBody QueryRequest request, 
            HttpServletRequest httpRequest,
            HttpServletResponse response) {
        
        try {
            // Valida la query prima dell'esecuzione
            ValidationResult validationResult = queryValidationService.validaQuery(request.getSql());
            if (!validationResult.isValida()) {
                return ResponseEntity.badRequest().body(creaMessaggioErrore(
                    "Query non valida: " + String.join(", ", validationResult.getErrori())
                ));
            }
            
            // Mostra avvisi se presenti
            if (!validationResult.getAvvisi().isEmpty()) {
                System.out.println("Avvisi per la query: " + String.join(", ", validationResult.getAvvisi()));
            }
            
            // Ottieni l'IP dell'utente
            String userIp = ottieniIpUtente(httpRequest);
            
            // Esegui la query con cronologia
            TableResult result = bigQueryService.runQueryWithHistory(request.getSql(), userIp);
            
            if (request.isExport()) {
                response.setHeader("Content-Disposition", "attachment; filename=\"export.csv\"");
                response.setContentType("text/csv");
                try {
                    bigQueryService.writeCsv(result, response.getWriter());
                } catch (IOException e) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(creaMessaggioErrore("Errore durante l'esportazione CSV: " + e.getMessage()));
                }
                return null;
            } else {
                return ResponseEntity.ok(bigQueryService.toList(result));
            }
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(creaMessaggioErrore("Query interrotta: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(creaMessaggioErrore("Errore durante l'esecuzione della query: " + e.getMessage()));
        }
    }

    @Operation(
        summary = "Testa la connessione BigQuery", 
        description = "Verifica che la connessione a BigQuery funzioni correttamente"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Connessione BigQuery funzionante"),
        @ApiResponse(responseCode = "500", description = "Errore di connessione a BigQuery")
    })
    @GetMapping("/test")
    public ResponseEntity<?> testaConnessione() {
        try {
            TableResult result = bigQueryService.runQuery("SELECT CURRENT_DATE() as data_corrente, CURRENT_TIME() as ora_corrente");
            Map<String, Object> response = new HashMap<>();
            response.put("stato", "connesso");
            response.put("messaggio", "Connessione a BigQuery funzionante");
            response.put("dati_test", bigQueryService.toList(result));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(creaMessaggioErrore("Errore di connessione a BigQuery: " + e.getMessage()));
        }
    }

    @Operation(
        summary = "Esegui query strutturata (no-SQL)",
        description = "Costruisce ed esegue una query su BigQuery a partire da campi strutturati"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Query eseguita con successo",
                    content = @Content(schema = @Schema(implementation = Object.class))),
        @ApiResponse(responseCode = "400", description = "Richiesta non valida"),
        @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    @PostMapping("/query-structured")
    public ResponseEntity<?> eseguiQueryStrutturata(
            @Valid @RequestBody StructuredQueryRequest request,
            HttpServletResponse response,
            HttpServletRequest httpRequest) {
        try {
            TableResult result = bigQueryService.runStructuredQueryWithHistory(request, ottieniIpUtente(httpRequest));

            if (request.isExport()) {
                response.setHeader("Content-Disposition", "attachment; filename=export.csv");
                response.setContentType("text/csv");
                try {
                    bigQueryService.writeCsv(result, response.getWriter());
                } catch (IOException e) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(creaMessaggioErrore("Errore durante l'esportazione CSV: " + e.getMessage()));
                }
                return null;
            }

            return ResponseEntity.ok(bigQueryService.toList(result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(creaMessaggioErrore(e.getMessage()));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(creaMessaggioErrore("Query interrotta: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(creaMessaggioErrore("Errore durante l'esecuzione della query: " + e.getMessage()));
        }
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> gestisciErroriValidazione(MethodArgumentNotValidException ex) {
        Map<String, String> errori = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String nomeCampo = ((FieldError) error).getField();
            String messaggioErrore = error.getDefaultMessage();
            errori.put(nomeCampo, messaggioErrore);
        });
        return ResponseEntity.badRequest().body(creaMessaggioErrore("Errori di validazione", errori));
    }

    private Map<String, Object> creaMessaggioErrore(String messaggio) {
        Map<String, Object> errore = new HashMap<>();
        errore.put("errore", true);
        errore.put("messaggio", messaggio);
        errore.put("timestamp", java.time.LocalDateTime.now());
        return errore;
    }

    private Map<String, Object> creaMessaggioErrore(String messaggio, Map<String, String> dettagli) {
        Map<String, Object> errore = creaMessaggioErrore(messaggio);
        errore.put("dettagli", dettagli);
        return errore;
    }
    
    private String ottieniIpUtente(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}
