package com.example.service;

import com.example.entity.QueryHistory;
import com.example.repository.QueryHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PerformanceMonitoringService {
    
    @Autowired
    private QueryHistoryRepository queryHistoryRepository;
    
    public Map<String, Object> getStatistichePerformance() {
        Map<String, Object> stats = new HashMap<>();
        
        // Query totali e successo
        long totalQueries = queryHistoryRepository.count();
        long successfulQueries = queryHistoryRepository.findBySuccessTrueOrderByCreatedAtDesc().size();
        long failedQueries = totalQueries - successfulQueries;
        
        stats.put("queryTotali", totalQueries);
        stats.put("queryRiuscite", successfulQueries);
        stats.put("queryFallite", failedQueries);
        stats.put("percentualeSuccesso", totalQueries > 0 ? (double) successfulQueries / totalQueries * 100 : 0);
        
        // Tempo di esecuzione medio
        List<QueryHistory> queryRiuscite = queryHistoryRepository.findBySuccessTrueOrderByCreatedAtDesc();
        if (!queryRiuscite.isEmpty()) {
            double tempoMedio = queryRiuscite.stream()
                    .filter(q -> q.getExecutionTimeMs() != null)
                    .mapToLong(QueryHistory::getExecutionTimeMs)
                    .average()
                    .orElse(0.0);
            stats.put("tempoEsecuzioneMedio", Math.round(tempoMedio));
            
            // Tempo massimo e minimo
            long tempoMassimo = queryRiuscite.stream()
                    .filter(q -> q.getExecutionTimeMs() != null)
                    .mapToLong(QueryHistory::getExecutionTimeMs)
                    .max()
                    .orElse(0L);
            long tempoMinimo = queryRiuscite.stream()
                    .filter(q -> q.getExecutionTimeMs() != null)
                    .mapToLong(QueryHistory::getExecutionTimeMs)
                    .min()
                    .orElse(0L);
            
            stats.put("tempoEsecuzioneMassimo", tempoMassimo);
            stats.put("tempoEsecuzioneMinimo", tempoMinimo);
        }
        
        // Query più lente
        List<QueryHistory> queryLente = queryHistoryRepository.findSlowestQueries();
        if (!queryLente.isEmpty()) {
            stats.put("queryPiuLente", queryLente.stream()
                    .limit(5)
                    .map(this::convertiQueryInMap)
                    .collect(Collectors.toList()));
        }
        
        // Query più frequenti
        List<Object[]> queryFrequenti = queryHistoryRepository.findMostFrequentQueries();
        if (!queryFrequenti.isEmpty()) {
            stats.put("queryPiuFrequenti", queryFrequenti.stream()
                    .limit(5)
                    .map(row -> Map.of(
                        "query", row[0],
                        "frequenza", row[1]
                    ))
                    .collect(Collectors.toList()));
        }
        
        // Statistiche per ora del giorno
        Map<Integer, Long> queryPerOra = queryHistoryRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                    q -> q.getCreatedAt().getHour(),
                    Collectors.counting()
                ));
        stats.put("queryPerOra", queryPerOra);
        
        // Statistiche per giorno della settimana
        Map<String, Long> queryPerGiorno = queryHistoryRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                    q -> q.getCreatedAt().getDayOfWeek().toString(),
                    Collectors.counting()
                ));
        stats.put("queryPerGiorno", queryPerGiorno);
        
        return stats;
    }
    
    public Map<String, Object> getAnalisiTrend(LocalDateTime inizio, LocalDateTime fine) {
        Map<String, Object> trend = new HashMap<>();
        
        List<QueryHistory> queryNelPeriodo = queryHistoryRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(inizio, fine);
        
        if (queryNelPeriodo.isEmpty()) {
            trend.put("messaggio", "Nessuna query trovata nel periodo specificato");
            return trend;
        }
        
        // Trend temporale
        Map<String, Long> queryPerGiorno = queryNelPeriodo.stream()
                .collect(Collectors.groupingBy(
                    q -> q.getCreatedAt().toLocalDate().toString(),
                    Collectors.counting()
                ));
        trend.put("queryPerGiorno", queryPerGiorno);
        
        // Performance nel tempo
        Map<String, Double> tempoMedioPerGiorno = queryNelPeriodo.stream()
                .filter(q -> q.getSuccess() && q.getExecutionTimeMs() != null)
                .collect(Collectors.groupingBy(
                    q -> q.getCreatedAt().toLocalDate().toString(),
                    Collectors.averagingLong(QueryHistory::getExecutionTimeMs)
                ));
        trend.put("tempoMedioPerGiorno", tempoMedioPerGiorno);
        
        // Tasso di successo nel tempo
        Map<String, Map<String, Long>> successoPerGiorno = queryNelPeriodo.stream()
                .collect(Collectors.groupingBy(
                    q -> q.getCreatedAt().toLocalDate().toString(),
                    Collectors.groupingBy(
                        q -> q.getSuccess() ? "riuscite" : "fallite",
                        Collectors.counting()
                    )
                ));
        trend.put("successoPerGiorno", successoPerGiorno);
        
        return trend;
    }
    
    public Map<String, Object> getAnalisiUtenti() {
        Map<String, Object> analisi = new HashMap<>();
        
        // Statistiche per IP
        List<Object[]> statsPerIp = queryHistoryRepository.getQueryStatisticsByIp();
        analisi.put("statistichePerIp", statsPerIp.stream()
                .map(row -> Map.of(
                    "ip", row[0],
                    "queryTotali", row[1],
                    "queryRiuscite", row[2],
                    "tempoMedio", row[3]
                ))
                .collect(Collectors.toList()));
        
        // IP più attivi
        List<Object[]> ipAttivi = statsPerIp.stream()
                .sorted((a, b) -> ((Long) b[1]).compareTo((Long) a[1]))
                .limit(10)
                .collect(Collectors.toList());
        analisi.put("ipPiuAttivi", ipAttivi);
        
        return analisi;
    }
    
    public Map<String, Object> getRaccomandazioniOttimizzazione() {
        Map<String, Object> raccomandazioni = new HashMap<>();
        List<String> suggerimenti = new ArrayList<>();
        
        // Analizza query lente
        List<QueryHistory> queryLente = queryHistoryRepository.findSlowestQueries();
        if (!queryLente.isEmpty()) {
            double tempoMedioLente = queryLente.stream()
                    .limit(10)
                    .mapToLong(q -> q.getExecutionTimeMs() != null ? q.getExecutionTimeMs() : 0)
                    .average()
                    .orElse(0.0);
            
            if (tempoMedioLente > 30000) { // > 30 secondi
                suggerimenti.add("Considera l'ottimizzazione delle query che impiegano più di 30 secondi");
            }
        }
        
        // Analizza query fallite
        List<QueryHistory> queryFallite = queryHistoryRepository.findBySuccessFalseOrderByCreatedAtDesc();
        if (!queryFallite.isEmpty()) {
            long queryFalliteRecenti = queryFallite.stream()
                    .filter(q -> q.getCreatedAt().isAfter(LocalDateTime.now().minusDays(7)))
                    .count();
            
            if (queryFalliteRecenti > 10) {
                suggerimenti.add("Alto numero di query fallite negli ultimi 7 giorni. Verifica la sintassi SQL");
            }
        }
        
        // Analizza pattern di utilizzo
        Map<Integer, Long> queryPerOra = queryHistoryRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                    q -> q.getCreatedAt().getHour(),
                    Collectors.counting()
                ));
        
        long piccoUtilizzo = queryPerOra.values().stream().mapToLong(Long::longValue).max().orElse(0);
        int oraPicco = queryPerOra.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(0);
        
        if (piccoUtilizzo > 100) {
            suggerimenti.add("Picco di utilizzo rilevato alle ore " + oraPicco + ". Considera il bilanciamento del carico");
        }
        
        raccomandazioni.put("suggerimenti", suggerimenti);
        raccomandazioni.put("numeroSuggerimenti", suggerimenti.size());
        
        return raccomandazioni;
    }
    
    private Map<String, Object> convertiQueryInMap(QueryHistory query) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", query.getId());
        map.put("sql", query.getSqlQuery().length() > 100 ? 
                query.getSqlQuery().substring(0, 100) + "..." : 
                query.getSqlQuery());
        map.put("tempoEsecuzione", query.getExecutionTimeMs());
        map.put("righeRestituite", query.getRowsReturned());
        map.put("dataEsecuzione", query.getCreatedAt());
        return map;
    }
}


