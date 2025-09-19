package com.example.service;

import org.springframework.stereotype.Service;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class QueryValidationService {
    
    // Parole chiave SQL pericolose che potrebbero causare problemi
    private static final Set<String> DANGEROUS_KEYWORDS = Set.of(
        "DROP", "DELETE", "TRUNCATE", "ALTER", "CREATE", "INSERT", "UPDATE", 
        "GRANT", "REVOKE", "EXEC", "EXECUTE", "SP_", "XP_", "SHUTDOWN", 
        "RESTORE", "BACKUP", "BULK", "OPENROWSET", "OPENDATASOURCE"
    );
    
    // Parole chiave SQL valide per BigQuery
    private static final Set<String> VALID_KEYWORDS = Set.of(
        "SELECT", "FROM", "WHERE", "GROUP", "BY", "HAVING", "ORDER", "LIMIT", 
        "OFFSET", "JOIN", "INNER", "LEFT", "RIGHT", "FULL", "OUTER", "ON", 
        "AS", "AND", "OR", "NOT", "IN", "EXISTS", "BETWEEN", "LIKE", "IS", 
        "NULL", "CASE", "WHEN", "THEN", "ELSE", "END", "DISTINCT", "COUNT", 
        "SUM", "AVG", "MIN", "MAX", "UNION", "ALL", "INTERSECT", "EXCEPT",
        "WITH", "WINDOW", "PARTITION", "OVER", "ROW_NUMBER", "RANK",
        "DENSE_RANK", "LEAD", "LAG", "FIRST_VALUE", "LAST_VALUE", "NTILE"
    );
    
    // Pattern per identificare commenti SQL
    private static final Pattern COMMENT_PATTERN = Pattern.compile("--.*$|/\\*.*?\\*/", Pattern.MULTILINE | Pattern.DOTALL);
    
    // Pattern per identificare stringhe letterali
    private static final Pattern STRING_PATTERN = Pattern.compile("'[^']*'|\"[^\"]*\"");
    
    public ValidationResult validaQuery(String sql) {
        ValidationResult result = new ValidationResult();
        
        if (sql == null || sql.trim().isEmpty()) {
            result.addErrore("Query SQL non può essere vuota");
            return result;
        }
        
        // Rimuovi commenti e stringhe per l'analisi
        String sqlPulita = rimuoviCommentiEStringhe(sql);
        
        // Controlla parole chiave pericolose
        controllaParoleChiavePericolose(sqlPulita, result);
        
        // Controlla sintassi base
        controllaSintassiBase(sqlPulita, result);
        
        // Controlla dimensioni
        controllaDimensioni(sql, result);
        
        // Controlla caratteri speciali
        controllaCaratteriSpeciali(sql, result);
        
        return result;
    }
    
    private String rimuoviCommentiEStringhe(String sql) {
        // Rimuovi commenti
        String senzaCommenti = COMMENT_PATTERN.matcher(sql).replaceAll(" ");
        
        // Rimuovi stringhe letterali
        return STRING_PATTERN.matcher(senzaCommenti).replaceAll("'STRING'");
    }
    
    private void controllaParoleChiavePericolose(String sql, ValidationResult result) {
        String[] parole = sql.toUpperCase().split("\\s+");
        
        for (String parola : parole) {
            // Rimuovi punteggiatura
            parola = parola.replaceAll("[^a-zA-Z]", "");
            
            if (DANGEROUS_KEYWORDS.contains(parola)) {
                result.addErrore("Parola chiave pericolosa rilevata: " + parola);
            }
        }
    }
    
    private void controllaSintassiBase(String sql, ValidationResult result) {
        String sqlUpper = sql.toUpperCase().trim();
        
        // Deve iniziare con SELECT
        if (!sqlUpper.startsWith("SELECT")) {
            result.addErrore("La query deve iniziare con SELECT");
        }
        
        // Controlla parentesi bilanciate
        if (!parentesiBilanciate(sql)) {
            result.addErrore("Parentesi non bilanciate nella query");
        }
        
        // Controlla che non ci siano statement multipli
        if (contieneStatementMultipli(sql)) {
            result.addErrore("Sono consentite solo query SELECT singole");
        }
    }
    
    private void controllaDimensioni(String sql, ValidationResult result) {
        if (sql.length() > 10000) {
            result.addErrore("La query è troppo lunga (massimo 10000 caratteri)");
        }
        
        // Conta il numero di righe
        long righe = sql.lines().count();
        if (righe > 100) {
            result.addAvviso("La query è molto lunga (" + righe + " righe)");
        }
    }
    
    private void controllaCaratteriSpeciali(String sql, ValidationResult result) {
        // Controlla caratteri non ASCII
        if (!sql.matches(".*[\\x00-\\x7F].*")) {
            result.addAvviso("La query contiene caratteri non ASCII");
        }
        
        // Controlla caratteri di controllo
        if (sql.matches(".*[\\x00-\\x1F\\x7F].*")) {
            result.addErrore("La query contiene caratteri di controllo non validi");
        }
    }
    
    private boolean parentesiBilanciate(String sql) {
        int contatore = 0;
        for (char c : sql.toCharArray()) {
            if (c == '(') contatore++;
            else if (c == ')') contatore--;
            if (contatore < 0) return false;
        }
        return contatore == 0;
    }
    
    private boolean contieneStatementMultipli(String sql) {
        // Rimuovi commenti e stringhe per controllare statement multipli
        String sqlPulita = rimuoviCommentiEStringhe(sql);
        
        // Conta i punti e virgola che non sono all'interno di stringhe
        String[] parti = sqlPulita.split(";");
        return parti.length > 1;
    }
    
    public static class ValidationResult {
        private List<String> errori = new ArrayList<>();
        private List<String> avvisi = new ArrayList<>();
        private boolean valida = true;
        
        public void addErrore(String errore) {
            errori.add(errore);
            valida = false;
        }
        
        public void addAvviso(String avviso) {
            avvisi.add(avviso);
        }
        
        public boolean isValida() {
            return valida;
        }
        
        public List<String> getErrori() {
            return errori;
        }
        
        public List<String> getAvvisi() {
            return avvisi;
        }
        
        public Map<String, Object> toMap() {
            Map<String, Object> result = new HashMap<>();
            result.put("valida", valida);
            result.put("errori", errori);
            result.put("avvisi", avvisi);
            return result;
        }
    }
}
