package com.example.controller;

import com.example.service.QueryValidationService;
import com.example.service.QueryValidationService.ValidationResult;
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
@RequestMapping("/bigquery/validation")
@Tag(name = "Validazione Query", description = "API per la validazione delle query SQL")
public class QueryValidationController {

    @Autowired
    private QueryValidationService queryValidationService;

    @Operation(
        summary = "Valida query SQL", 
        description = "Valida una query SQL per controllare sintassi, sicurezza e best practices"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Validazione completata"),
        @ApiResponse(responseCode = "400", description = "Richiesta non valida")
    })
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validaQuery(
            @Parameter(description = "Query SQL da validare", required = true)
            @RequestBody Map<String, String> request) {
        
        String sql = request.get("sql");
        if (sql == null || sql.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                "valida", false,
                "errori", new String[]{"Query SQL non fornita"},
                "avvisi", new String[0]
            ));
        }
        
        ValidationResult result = queryValidationService.validaQuery(sql);
        return ResponseEntity.ok(result.toMap());
    }

    @Operation(
        summary = "Valida query SQL con parametri", 
        description = "Valida una query SQL con parametri aggiuntivi per la validazione"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Validazione completata"),
        @ApiResponse(responseCode = "400", description = "Richiesta non valida")
    })
    @PostMapping("/validate-advanced")
    public ResponseEntity<Map<String, Object>> validaQueryAvanzata(
            @Parameter(description = "Parametri di validazione", required = true)
            @RequestBody Map<String, Object> request) {
        
        String sql = (String) request.get("sql");
        if (sql == null || sql.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                "valida", false,
                "errori", new String[]{"Query SQL non fornita"},
                "avvisi", new String[0]
            ));
        }
        
        ValidationResult result = queryValidationService.validaQuery(sql);
        
        // Aggiungi informazioni aggiuntive
        Map<String, Object> response = result.toMap();
        response.put("lunghezzaQuery", sql.length());
        response.put("numeroRighe", sql.lines().count());
        response.put("timestamp", java.time.LocalDateTime.now());
        
        return ResponseEntity.ok(response);
    }
}


