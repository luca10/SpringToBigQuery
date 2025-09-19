package com.example.config;

import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

@Configuration
public class BigQueryConfig {

    @Value("${bigquery.project-id:default-project}")
    private String projectId;

    @Value("${bigquery.credentials-file:classpath:APIGoogleBQ.json}")
    private String credentialsFile;

    @Bean
    public BigQuery bigQuery() {
        try {
            // Prova a caricare le credenziali dal file
            ClassPathResource resource = new ClassPathResource(credentialsFile.replace("classpath:", ""));
            
            if (resource.exists()) {
                System.out.println("✅ Caricamento credenziali BigQuery da: " + credentialsFile);
                return BigQueryOptions.newBuilder()
                        .setProjectId(projectId)
                        .setCredentials(ServiceAccountCredentials.fromStream(resource.getInputStream()))
                        .build()
                        .getService();
            } else {
                System.out.println("⚠️  File credenziali non trovato: " + credentialsFile);
                System.out.println("🔧 Uso credenziali di default (per testing)");
                return BigQueryOptions.newBuilder()
                        .setProjectId(projectId)
                        .build()
                        .getService();
            }
        } catch (IOException e) {
            System.err.println("❌ Errore nel caricamento delle credenziali BigQuery: " + e.getMessage());
            System.err.println("💡 Soluzioni possibili:");
            System.err.println("   1. Crea un Service Account su Google Cloud Console");
            System.err.println("   2. Scarica il file JSON delle credenziali");
            System.err.println("   3. Posizionalo in src/main/resources/APIGoogleBQ.json");
            System.err.println("   4. Oppure imposta la variabile d'ambiente GOOGLE_APPLICATION_CREDENTIALS");
            System.err.println("   5. Oppure usa il profilo 'dev' per testing senza credenziali reali");
            
            // Fallback: crea un'istanza di default
            return BigQueryOptions.newBuilder()
                    .setProjectId(projectId)
                    .build()
                    .getService();
        }
    }
}


