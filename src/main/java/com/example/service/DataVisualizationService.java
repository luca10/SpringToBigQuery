package com.example.service;

import com.google.cloud.bigquery.TableResult;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DataVisualizationService {
    
    public Map<String, Object> generaConfigurazioneGrafico(TableResult result, String tipoGrafico) {
        Map<String, Object> config = new HashMap<>();
        
        if (result == null || !result.iterateAll().iterator().hasNext()) {
            config.put("errore", "Nessun dato disponibile per la visualizzazione");
            return config;
        }
        
        List<Map<String, Object>> dati = convertiRisultatiInLista(result);
        
        switch (tipoGrafico.toLowerCase()) {
            case "bar":
                return generaConfigurazioneBarChart(dati);
            case "line":
                return generaConfigurazioneLineChart(dati);
            case "pie":
                return generaConfigurazionePieChart(dati);
            case "scatter":
                return generaConfigurazioneScatterPlot(dati);
            case "histogram":
                return generaConfigurazioneHistogram(dati);
            default:
                return generaConfigurazioneTabella(dati);
        }
    }
    
    private Map<String, Object> generaConfigurazioneBarChart(List<Map<String, Object>> dati) {
        Map<String, Object> config = new HashMap<>();
        config.put("tipo", "bar");
        config.put("titolo", "Grafico a Barre");
        
        if (dati.isEmpty()) {
            config.put("errore", "Nessun dato disponibile");
            return config;
        }
        
        // Prendi le prime due colonne per x e y
        List<String> colonne = new ArrayList<>(dati.get(0).keySet());
        String colonnaX = colonne.get(0);
        String colonnaY = colonne.size() > 1 ? colonne.get(1) : colonne.get(0);
        
        List<Object> labels = dati.stream()
                .map(row -> row.get(colonnaX))
                .collect(Collectors.toList());
        
        List<Object> values = dati.stream()
                .map(row -> {
                    Object val = row.get(colonnaY);
                    if (val instanceof Number) {
                        return val;
                    } else if (val instanceof String) {
                        try {
                            return Double.parseDouble((String) val);
                        } catch (NumberFormatException e) {
                            return 0.0;
                        }
                    }
                    return 0.0;
                })
                .collect(Collectors.toList());
        
        config.put("labels", labels);
        config.put("datasets", Arrays.asList(Map.of(
            "label", colonnaY,
            "data", values,
            "backgroundColor", "rgba(54, 162, 235, 0.6)",
            "borderColor", "rgba(54, 162, 235, 1)",
            "borderWidth", 1
        )));
        
        return config;
    }
    
    private Map<String, Object> generaConfigurazioneLineChart(List<Map<String, Object>> dati) {
        Map<String, Object> config = new HashMap<>();
        config.put("tipo", "line");
        config.put("titolo", "Grafico a Linee");
        
        if (dati.isEmpty()) {
            config.put("errore", "Nessun dato disponibile");
            return config;
        }
        
        List<String> colonne = new ArrayList<>(dati.get(0).keySet());
        String colonnaX = colonne.get(0);
        String colonnaY = colonne.size() > 1 ? colonne.get(1) : colonne.get(0);
        
        List<Object> labels = dati.stream()
                .map(row -> row.get(colonnaX))
                .collect(Collectors.toList());
        
        List<Object> values = dati.stream()
                .map(row -> {
                    Object val = row.get(colonnaY);
                    if (val instanceof Number) {
                        return val;
                    } else if (val instanceof String) {
                        try {
                            return Double.parseDouble((String) val);
                        } catch (NumberFormatException e) {
                            return 0.0;
                        }
                    }
                    return 0.0;
                })
                .collect(Collectors.toList());
        
        config.put("labels", labels);
        config.put("datasets", Arrays.asList(Map.of(
            "label", colonnaY,
            "data", values,
            "borderColor", "rgba(75, 192, 192, 1)",
            "backgroundColor", "rgba(75, 192, 192, 0.2)",
            "tension", 0.1
        )));
        
        return config;
    }
    
    private Map<String, Object> generaConfigurazionePieChart(List<Map<String, Object>> dati) {
        Map<String, Object> config = new HashMap<>();
        config.put("tipo", "pie");
        config.put("titolo", "Grafico a Torta");
        
        if (dati.isEmpty()) {
            config.put("errore", "Nessun dato disponibile");
            return config;
        }
        
        List<String> colonne = new ArrayList<>(dati.get(0).keySet());
        String colonnaLabel = colonne.get(0);
        String colonnaValue = colonne.size() > 1 ? colonne.get(1) : colonne.get(0);
        
        List<Object> labels = dati.stream()
                .map(row -> row.get(colonnaLabel))
                .collect(Collectors.toList());
        
        List<Object> values = dati.stream()
                .map(row -> {
                    Object val = row.get(colonnaValue);
                    if (val instanceof Number) {
                        return val;
                    } else if (val instanceof String) {
                        try {
                            return Double.parseDouble((String) val);
                        } catch (NumberFormatException e) {
                            return 0.0;
                        }
                    }
                    return 0.0;
                })
                .collect(Collectors.toList());
        
        // Genera colori per ogni segmento
        List<String> colors = generaColori(dati.size());
        
        config.put("labels", labels);
        config.put("datasets", Arrays.asList(Map.of(
            "data", values,
            "backgroundColor", colors,
            "borderWidth", 2
        )));
        
        return config;
    }
    
    private Map<String, Object> generaConfigurazioneScatterPlot(List<Map<String, Object>> dati) {
        Map<String, Object> config = new HashMap<>();
        config.put("tipo", "scatter");
        config.put("titolo", "Scatter Plot");
        
        if (dati.isEmpty()) {
            config.put("errore", "Nessun dato disponibile");
            return config;
        }
        
        List<String> colonne = new ArrayList<>(dati.get(0).keySet());
        String colonnaX = colonne.get(0);
        String colonnaY = colonne.size() > 1 ? colonne.get(1) : colonne.get(0);
        
        List<Map<String, Object>> scatterData = dati.stream()
                .map(row -> {
                    Map<String, Object> point = new HashMap<>();
                    Object xVal = row.get(colonnaX);
                    Object yVal = row.get(colonnaY);
                    
                    point.put("x", convertiInNumero(xVal));
                    point.put("y", convertiInNumero(yVal));
                    return point;
                })
                .collect(Collectors.toList());
        
        config.put("datasets", Arrays.asList(Map.of(
            "label", colonnaX + " vs " + colonnaY,
            "data", scatterData,
            "backgroundColor", "rgba(255, 99, 132, 0.6)",
            "borderColor", "rgba(255, 99, 132, 1)"
        )));
        
        return config;
    }
    
    private Map<String, Object> generaConfigurazioneHistogram(List<Map<String, Object>> dati) {
        Map<String, Object> config = new HashMap<>();
        config.put("tipo", "histogram");
        config.put("titolo", "Istogramma");
        
        if (dati.isEmpty()) {
            config.put("errore", "Nessun dato disponibile");
            return config;
        }
        
        List<String> colonne = new ArrayList<>(dati.get(0).keySet());
        String colonna = colonne.get(0);
        
        List<Double> valori = dati.stream()
                .map(row -> convertiInNumero(row.get(colonna)))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        
        if (valori.isEmpty()) {
            config.put("errore", "Nessun valore numerico trovato");
            return config;
        }
        
        // Calcola bins per l'istogramma
        double min = Collections.min(valori);
        double max = Collections.max(valori);
        int numBins = Math.min(10, valori.size());
        double binWidth = (max - min) / numBins;
        
        Map<String, Integer> bins = new HashMap<>();
        for (double val : valori) {
            int binIndex = (int) Math.min((val - min) / binWidth, numBins - 1);
            String binLabel = String.format("%.2f-%.2f", min + binIndex * binWidth, min + (binIndex + 1) * binWidth);
            bins.put(binLabel, bins.getOrDefault(binLabel, 0) + 1);
        }
        
        config.put("labels", new ArrayList<>(bins.keySet()));
        config.put("datasets", Arrays.asList(Map.of(
            "label", "Frequenza",
            "data", new ArrayList<>(bins.values()),
            "backgroundColor", "rgba(153, 102, 255, 0.6)",
            "borderColor", "rgba(153, 102, 255, 1)",
            "borderWidth", 1
        )));
        
        return config;
    }
    
    private Map<String, Object> generaConfigurazioneTabella(List<Map<String, Object>> dati) {
        Map<String, Object> config = new HashMap<>();
        config.put("tipo", "table");
        config.put("titolo", "Tabella Dati");
        config.put("dati", dati);
        
        if (!dati.isEmpty()) {
            config.put("colonne", new ArrayList<>(dati.get(0).keySet()));
        }
        
        return config;
    }
    
    private List<Map<String, Object>> convertiRisultatiInLista(TableResult result) {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (com.google.cloud.bigquery.FieldValueList row : result.iterateAll()) {
            Map<String, Object> map = new HashMap<>();
            com.google.cloud.bigquery.Schema schema = result.getSchema();
            for (com.google.cloud.bigquery.Field field : schema.getFields()) {
                map.put(field.getName(), row.get(field.getName()).getValue());
            }
            rows.add(map);
        }
        return rows;
    }
    
    private Double convertiInNumero(Object valore) {
        if (valore instanceof Number) {
            return ((Number) valore).doubleValue();
        } else if (valore instanceof String) {
            try {
                return Double.parseDouble((String) valore);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
    
    private List<String> generaColori(int numeroColori) {
        String[] coloriBase = {
            "rgba(255, 99, 132, 0.6)",
            "rgba(54, 162, 235, 0.6)",
            "rgba(255, 205, 86, 0.6)",
            "rgba(75, 192, 192, 0.6)",
            "rgba(153, 102, 255, 0.6)",
            "rgba(255, 159, 64, 0.6)",
            "rgba(199, 199, 199, 0.6)",
            "rgba(83, 102, 255, 0.6)"
        };
        
        List<String> colori = new ArrayList<>();
        for (int i = 0; i < numeroColori; i++) {
            colori.add(coloriBase[i % coloriBase.length]);
        }
        return colori;
    }
}


