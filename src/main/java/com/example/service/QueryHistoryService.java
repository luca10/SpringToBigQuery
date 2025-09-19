package com.example.service;

import com.example.entity.QueryHistory;
import com.example.repository.QueryHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
public class QueryHistoryService {
    
    @Autowired
    private QueryHistoryRepository queryHistoryRepository;
    
    public QueryHistory salvaQuery(String sqlQuery, String userIp) {
        QueryHistory queryHistory = new QueryHistory(sqlQuery, userIp);
        return queryHistoryRepository.save(queryHistory);
    }
    
    public void aggiornaQueryCompletata(Long queryId, Long executionTimeMs, Integer rowsReturned, boolean success, String errorMessage) {
        QueryHistory queryHistory = queryHistoryRepository.findById(queryId).orElse(null);
        if (queryHistory != null) {
            queryHistory.setExecutionTimeMs(executionTimeMs);
            queryHistory.setRowsReturned(rowsReturned);
            queryHistory.setSuccess(success);
            queryHistory.setErrorMessage(errorMessage);
            queryHistoryRepository.save(queryHistory);
        }
    }
    
    public List<QueryHistory> getCronologiaQuery(String userIp) {
        return queryHistoryRepository.findByUserIpOrderByCreatedAtDesc(userIp);
    }
    
    public List<QueryHistory> getCronologiaQuery(LocalDateTime start, LocalDateTime end) {
        return queryHistoryRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(start, end);
    }
    
    public List<QueryHistory> cercaQuery(String searchTerm) {
        return queryHistoryRepository.findBySqlQueryContaining(searchTerm);
    }
    
    public List<QueryHistory> getQueryRiuscite() {
        return queryHistoryRepository.findBySuccessTrueOrderByCreatedAtDesc();
    }
    
    public List<QueryHistory> getQueryFallite() {
        return queryHistoryRepository.findBySuccessFalseOrderByCreatedAtDesc();
    }
    
    public List<Object[]> getStatistichePerIp() {
        return queryHistoryRepository.getQueryStatisticsByIp();
    }
    
    public List<QueryHistory> getQueryPiuLente() {
        return queryHistoryRepository.findSlowestQueries();
    }
    
    public List<Object[]> getQueryPiuFrequenti() {
        return queryHistoryRepository.findMostFrequentQueries();
    }
    
    public Map<String, Object> getStatisticheGenerali() {
        Map<String, Object> stats = new HashMap<>();
        
        long totalQueries = queryHistoryRepository.count();
        long successfulQueries = queryHistoryRepository.findBySuccessTrueOrderByCreatedAtDesc().size();
        long failedQueries = queryHistoryRepository.findBySuccessFalseOrderByCreatedAtDesc().size();
        
        stats.put("queryTotali", totalQueries);
        stats.put("queryRiuscite", successfulQueries);
        stats.put("queryFallite", failedQueries);
        stats.put("percentualeSuccesso", totalQueries > 0 ? (double) successfulQueries / totalQueries * 100 : 0);
        
        return stats;
    }
    
    public void eliminaQuery(Long id) {
        queryHistoryRepository.deleteById(id);
    }
    
    public void eliminaCronologiaCompleta() {
        queryHistoryRepository.deleteAll();
    }
}

