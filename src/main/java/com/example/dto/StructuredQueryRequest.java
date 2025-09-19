package com.example.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

@Schema(description = "Richiesta strutturata per costruire query BigQuery senza SQL esplicito")
public class StructuredQueryRequest {

    @NotBlank(message = "Table reference is required. E.g. project.dataset.table")
    @Schema(description = "Tabella target in formato project.dataset.table",
            example = "my-project.sales.orders", required = true)
    private String table;

    @Schema(description = "Elenco colonne da selezionare. Se vuoto, usa *",
            example = "[\"order_id\", \"customer_id\", \"total\"]")
    private List<String> select;

    @Schema(description = "Filtri semplici campo -> valore. Operatore di default '='",
            example = "{\"status\": \"PAID\", \"country\": \"IT\"}")
    private Map<String, Object> filters;

    @Schema(description = "Ordinamenti es. ['-created_at', 'total'] dove '-' indica DESC",
            example = "[-created_at, total]")
    private List<String> orderBy;

    @Min(1)
    @Schema(description = "Numero massimo righe", example = "100", defaultValue = "100")
    private Integer limit = 100;

    @Schema(description = "Esporta i risultati in CSV", example = "false", defaultValue = "false")
    private boolean export = false;

    // Getters & Setters
    public String getTable() { return table; }
    public void setTable(String table) { this.table = table; }

    public List<String> getSelect() { return select; }
    public void setSelect(List<String> select) { this.select = select; }

    public Map<String, Object> getFilters() { return filters; }
    public void setFilters(Map<String, Object> filters) { this.filters = filters; }

    public List<String> getOrderBy() { return orderBy; }
    public void setOrderBy(List<String> orderBy) { this.orderBy = orderBy; }

    public Integer getLimit() { return limit; }
    public void setLimit(Integer limit) { this.limit = limit; }

    public boolean isExport() { return export; }
    public void setExport(boolean export) { this.export = export; }
}


