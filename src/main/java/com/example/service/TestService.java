package com.example.service;

import com.example.entity.QueryHistory;
import com.example.repository.QueryHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
public class TestService {
    
    @Autowired
    private QueryHistoryRepository queryHistoryRepository;
    
    @PostConstruct
    public void testDatabaseConnection() {
        try {
            // Test di connessione al database
            QueryHistory testQuery = new QueryHistory();
            testQuery.setSqlQuery("SELECT 1");
            testQuery.setSuccess(true);
            testQuery.setExecutionTimeMs(100L);
            testQuery.setRowsReturned(1);
            
            QueryHistory saved = queryHistoryRepository.save(testQuery);
            System.out.println("✅ Test database connessione riuscita! ID: " + saved.getId());
            
            // Verifica che possiamo recuperare i dati
            long count = queryHistoryRepository.count();
            System.out.println("✅ Numero di record nel database: " + count);
            
        } catch (Exception e) {
            System.err.println("❌ Errore nel test del database: " + e.getMessage());
            e.printStackTrace();
        }
    }
}


