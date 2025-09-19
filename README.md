# 🚀 BigQuery API - Applicazione Spring Boot

Un'applicazione Spring Boot completa per l'esecuzione di query su Google BigQuery con funzionalità avanzate di visualizzazione, monitoraggio e gestione.

## ✨ Caratteristiche Principali

### 🔍 Esecuzione Query
- **Esecuzione sicura** di query SQL su Google BigQuery
- **Validazione automatica** delle query per sicurezza e sintassi
- **Esportazione CSV** dei risultati
- **Gestione errori** completa con messaggi dettagliati

### 📊 Visualizzazione Dati
- **Grafici interattivi** (Barre, Linee, Torta, Scatter Plot, Istogramma)
- **Tabella dati** responsive
- **Integrazione Chart.js** per visualizzazioni moderne
- **Interfaccia web** intuitiva e user-friendly

### 📈 Monitoraggio Performance
- **Cronologia query** completa con tracking IP
- **Statistiche performance** in tempo reale
- **Analisi trend** temporali
- **Raccomandazioni ottimizzazione** automatiche
- **Dashboard analytics** completa

### 🔒 Sicurezza e Validazione
- **Validazione SQL** avanzata con controlli di sicurezza
- **Prevenzione SQL injection** e query pericolose
- **Tracking IP** per audit e sicurezza
- **Gestione errori** robusta

### 📚 Documentazione API
- **Swagger/OpenAPI** integrato
- **Documentazione completa** di tutti gli endpoint
- **Esempi di utilizzo** per ogni API
- **Interfaccia interattiva** per test

## 🛠️ Tecnologie Utilizzate

- **Spring Boot 3.5.3** - Framework principale
- **Google Cloud BigQuery** - Database cloud
- **Spring Data JPA** - Persistenza dati
- **H2 Database** - Database in-memory per cronologia
- **Swagger/OpenAPI** - Documentazione API
- **Chart.js** - Visualizzazioni grafiche
- **HTML5/CSS3/JavaScript** - Frontend web
- **Maven** - Gestione dipendenze

## 🚀 Avvio Rapido

### Prerequisiti
- Java 17 o superiore
- Maven 3.6 o superiore
- Credenziali Google Cloud BigQuery (file JSON)

### Installazione

1. **Clona il repository**
```bash
git clone <repository-url>
cd SpringToBigQuery
```

2. **Configura le credenziali BigQuery**
   - Posiziona il file delle credenziali JSON in `src/main/resources/APIGoogleBQ.json`
   - Aggiorna il Project ID nel file `BigQueryService.java` se necessario

3. **Compila e avvia l'applicazione**
```bash
mvn clean install
mvn spring-boot:run
```

4. **Accedi all'applicazione**
   - Interfaccia web: http://localhost:8080
   - Swagger UI: http://localhost:8080/swagger-ui.html
   - API Docs: http://localhost:8080/api-docs

## 📖 Utilizzo

### Interfaccia Web
1. Apri http://localhost:8080 nel browser
2. Inserisci la tua query SQL nel campo di testo
3. Scegli se esportare come CSV
4. Clicca "Esegui Query" per eseguire la query
5. Usa la sezione "Visualizzazioni" per creare grafici
6. Consulta le "Statistiche" per monitorare le performance

### API REST

#### Eseguire una Query
```bash
curl -X POST http://localhost:8080/bigquery/query \
  -H "Content-Type: application/json" \
  -d '{
    "sql": "SELECT * FROM `project.dataset.table` LIMIT 10",
    "export": false
  }'
```

#### Testare la Connessione
```bash
curl http://localhost:8080/bigquery/test
```

#### Validare una Query
```bash
curl -X POST http://localhost:8080/bigquery/validation/validate \
  -H "Content-Type: application/json" \
  -d '{"sql": "SELECT * FROM table"}'
```

#### Generare un Grafico
```bash
curl -X POST http://localhost:8080/bigquery/visualization/chart \
  -H "Content-Type: application/json" \
  -d '{
    "sql": "SELECT category, COUNT(*) as count FROM table GROUP BY category",
    "tipo": "bar"
  }'
```

## 🔧 Configurazione

### File di Configurazione (`application.properties`)
```properties
# Configurazione BigQuery
bigquery.project-id=your-project-id
bigquery.credentials-file=classpath:APIGoogleBQ.json

# Configurazione Swagger
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/swagger-ui.html

# Configurazione Server
server.port=8080
server.servlet.context-path=/

# Logging
logging.level.com.example=DEBUG
logging.level.com.google.cloud.bigquery=INFO
```

### Credenziali BigQuery
1. Vai al [Google Cloud Console](https://console.cloud.google.com/)
2. Crea un Service Account
3. Assegna i ruoli necessari per BigQuery
4. Scarica il file JSON delle credenziali
5. Posiziona il file in `src/main/resources/APIGoogleBQ.json`

## 📊 Endpoint API

### BigQuery
- `POST /bigquery/query` - Esegui query
- `GET /bigquery/test` - Testa connessione

### Cronologia
- `GET /bigquery/history/ip/{userIp}` - Cronologia per IP
- `GET /bigquery/history/periodo` - Cronologia per periodo
- `GET /bigquery/history/statistiche` - Statistiche generali
- `GET /bigquery/history/riuscite` - Query riuscite
- `GET /bigquery/history/fallite` - Query fallite

### Validazione
- `POST /bigquery/validation/validate` - Valida query
- `POST /bigquery/validation/validate-advanced` - Validazione avanzata

### Visualizzazione
- `POST /bigquery/visualization/chart` - Genera grafico
- `GET /bigquery/visualization/tipi` - Tipi grafico disponibili
- `POST /bigquery/visualization/chart-advanced` - Grafico avanzato

### Performance
- `GET /bigquery/performance/stats` - Statistiche performance
- `GET /bigquery/performance/trend` - Analisi trend
- `GET /bigquery/performance/users` - Analisi utenti
- `GET /bigquery/performance/recommendations` - Raccomandazioni
- `GET /bigquery/performance/dashboard` - Dashboard completa

## 🔒 Sicurezza

### Validazione Query
L'applicazione include un sistema di validazione avanzato che:
- Controlla parole chiave pericolose (DROP, DELETE, etc.)
- Verifica la sintassi SQL base
- Controlla la lunghezza delle query
- Valida caratteri speciali
- Previene statement multipli

### Tracking e Audit
- Registrazione di tutte le query eseguite
- Tracking dell'IP utente
- Monitoraggio dei tempi di esecuzione
- Log degli errori dettagliati

## 📈 Monitoraggio

### Dashboard Performance
La dashboard include:
- Statistiche generali (query totali, riuscite, fallite)
- Tempi di esecuzione (medio, massimo, minimo)
- Query più lente e frequenti
- Analisi per ora del giorno e giorno della settimana
- Raccomandazioni di ottimizzazione

### Metriche Disponibili
- **Query Totali**: Numero totale di query eseguite
- **Tasso di Successo**: Percentuale di query riuscite
- **Tempo di Esecuzione**: Statistiche sui tempi
- **Utilizzo per IP**: Analisi dell'utilizzo per utente
- **Trend Temporali**: Analisi dei pattern di utilizzo

## 🎨 Interfaccia Utente

### Caratteristiche UI
- **Design moderno** con gradienti e animazioni
- **Responsive** per tutti i dispositivi
- **Grafici interattivi** con Chart.js
- **Validazione in tempo reale** dei form
- **Feedback visivo** per tutte le operazioni

### Sezioni Disponibili
1. **Esecuzione Query** - Interfaccia principale per le query
2. **Visualizzazioni** - Creazione di grafici e chart
3. **Statistiche** - Monitoraggio delle performance
4. **Cronologia** - Consultazione delle query precedenti

## 🚀 Sviluppo

### Struttura del Progetto
```
src/
├── main/
│   ├── java/com/example/
│   │   ├── controller/     # Controller REST
│   │   ├── service/        # Servizi business
│   │   ├── entity/         # Entità JPA
│   │   ├── repository/     # Repository JPA
│   │   ├── dto/           # Data Transfer Objects
│   │   └── config/        # Configurazioni
│   └── resources/
│       ├── static/        # File statici (HTML, CSS, JS)
│       └── application.properties
└── test/                  # Test unitari
```

### Aggiungere Nuove Funzionalità
1. Crea il servizio in `service/`
2. Implementa il controller in `controller/`
3. Aggiungi la documentazione Swagger
4. Aggiorna l'interfaccia web se necessario
5. Aggiungi test unitari

## 🤝 Contribuire

1. Fork del repository
2. Crea un branch per la feature (`git checkout -b feature/nuova-feature`)
3. Commit delle modifiche (`git commit -am 'Aggiunge nuova feature'`)
4. Push del branch (`git push origin feature/nuova-feature`)
5. Crea una Pull Request

## 📝 Licenza

Questo progetto è rilasciato sotto licenza MIT. Vedi il file `LICENSE` per i dettagli.

## 🆘 Supporto

Per problemi o domande:
1. Controlla la documentazione Swagger all'indirizzo `/swagger-ui.html`
2. Verifica i log dell'applicazione
3. Controlla la configurazione delle credenziali BigQuery
4. Apri una issue su GitHub

## 🔄 Changelog

### v1.0.0
- ✅ Esecuzione query BigQuery
- ✅ Validazione SQL avanzata
- ✅ Interfaccia web moderna
- ✅ Visualizzazioni grafiche
- ✅ Cronologia query
- ✅ Monitoraggio performance
- ✅ Documentazione Swagger completa
- ✅ Esportazione CSV
- ✅ Dashboard analytics

---

**Sviluppato con ❤️ usando Spring Boot e Google BigQuery**


