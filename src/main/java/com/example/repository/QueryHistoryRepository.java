package com.example.repository;

import com.example.entity.QueryHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface QueryHistoryRepository extends JpaRepository<QueryHistory, Long> {
    
    // Trova le query eseguite da un IP specifico
    List<QueryHistory> findByUserIpOrderByCreatedAtDesc(String userIp);
    
    // Trova le query eseguite in un periodo di tempo
    List<QueryHistory> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime start, LocalDateTime end);
    
    // Trova le query che contengono una stringa specifica
    @Query("SELECT q FROM QueryHistory q WHERE q.sqlQuery LIKE %:searchTerm% ORDER BY q.createdAt DESC")
    List<QueryHistory> findBySqlQueryContaining(@Param("searchTerm") String searchTerm);
    
    // Trova le query di successo
    List<QueryHistory> findBySuccessTrueOrderByCreatedAtDesc();
    
    // Trova le query fallite
    List<QueryHistory> findBySuccessFalseOrderByCreatedAtDesc();
    
    // Statistiche delle query per IP
    @Query("SELECT q.userIp, COUNT(q) as totalQueries, " +
           "SUM(CASE WHEN q.success = true THEN 1 ELSE 0 END) as successfulQueries, " +
           "AVG(q.executionTimeMs) as avgExecutionTime " +
           "FROM QueryHistory q " +
           "GROUP BY q.userIp " +
           "ORDER BY totalQueries DESC")
    List<Object[]> getQueryStatisticsByIp();
    
    // Query più lente
    @Query("SELECT q FROM QueryHistory q WHERE q.success = true ORDER BY q.executionTimeMs DESC")
    List<QueryHistory> findSlowestQueries();
    
    // Query più frequenti
    @Query("SELECT q.sqlQuery, COUNT(q) as frequency " +
           "FROM QueryHistory q " +
           "WHERE q.success = true " +
           "GROUP BY q.sqlQuery " +
           "ORDER BY frequency DESC")
    List<Object[]> findMostFrequentQueries();
}

