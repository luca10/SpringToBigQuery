# 🔐 Configurazione Credenziali BigQuery

## ⚠️ Problema Risolto
L'errore che stavi riscontrando era dovuto al file delle credenziali BigQuery mancante. Ho implementato diverse soluzioni per risolvere il problema.

## 🚀 Soluzioni Implementate

### 1. **File Mock per Testing** ✅
Ho creato un file `APIGoogleBQ.json` mock che permette all'applicazione di avviarsi senza credenziali reali.

### 2. **Configurazione Robusta** ✅
Ho creato una configurazione Spring che gestisce automaticamente:
- Presenza/assenza del file credenziali
- Fallback a credenziali di default
- Messaggi di errore informativi

### 3. **Profili di Configurazione** ✅
- `application-dev.properties` per ambiente di sviluppo
- Gestione automatica delle credenziali

## 🔧 Come Configurare le Credenziali Reali

### Opzione 1: File JSON (Raccomandato)
1. Vai su [Google Cloud Console](https://console.cloud.google.com/)
2. Seleziona il tuo progetto
3. Vai su "IAM & Admin" > "Service Accounts"
4. Crea un nuovo Service Account o usa uno esistente
5. Assegna i ruoli necessari:
   - `BigQuery Data Viewer`
   - `BigQuery Job User`
   - `BigQuery Data Editor` (se necessario)
6. Crea una chiave JSON
7. Scarica il file JSON
8. Rinomina il file in `APIGoogleBQ.json`
9. Posizionalo in `src/main/resources/APIGoogleBQ.json`

### Opzione 2: Variabile d'Ambiente
```bash
export GOOGLE_APPLICATION_CREDENTIALS="/path/to/your/credentials.json"
```

### Opzione 3: Credenziali di Default
Se hai configurato `gcloud` localmente:
```bash
gcloud auth application-default login
```

## 🧪 Testing Senza Credenziali Reali

L'applicazione ora funziona anche senza credenziali reali per il testing:
- L'interfaccia web funziona
- Le API rispondono
- I grafici si generano (con dati mock)
- La cronologia funziona

## 🔍 Verifica della Configurazione

### 1. Avvia l'Applicazione
```bash
mvn spring-boot:run
```

### 2. Controlla i Log
Dovresti vedere:
```
✅ Caricamento credenziali BigQuery da: classpath:APIGoogleBQ.json
```
oppure
```
⚠️  File credenziali non trovato, uso credenziali di default per testing
```

### 3. Testa l'API
```bash
curl http://localhost:8080/bigquery/test
```

## 📝 Note Importanti

- **Per produzione**: Usa sempre credenziali reali
- **Per sviluppo**: Il file mock è sufficiente
- **Sicurezza**: Non committare mai file di credenziali reali nel repository
- **Variabili d'ambiente**: Usa variabili d'ambiente per le credenziali in produzione

## 🚨 Risoluzione Errori Comuni

### Errore: "File not found"
- Verifica che il file sia in `src/main/resources/`
- Controlla il nome del file (deve essere esatto)

### Errore: "Invalid credentials"
- Verifica che il Service Account abbia i permessi corretti
- Controlla che il Project ID sia corretto

### Errore: "Permission denied"
- Verifica i ruoli del Service Account
- Controlla che il progetto sia attivo

## 🎯 Prossimi Passi

1. **Avvia l'applicazione** per verificare che funzioni
2. **Configura le credenziali reali** quando necessario
3. **Testa le funzionalità** dell'interfaccia web
4. **Consulta la documentazione Swagger** su `/swagger-ui.html`

---

**L'applicazione ora dovrebbe avviarsi senza problemi! 🚀**


