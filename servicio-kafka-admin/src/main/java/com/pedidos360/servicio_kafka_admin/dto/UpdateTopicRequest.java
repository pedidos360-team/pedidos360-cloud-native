package com.pedidos360.servicio_kafka_admin.dto;

import jakarta.validation.constraints.Min;

import java.util.Map;

public class UpdateTopicRequest {

    // Opcional: si se envía y es mayor al actual, aumenta las particiones (Kafka no permite reducirlas).
    @Min(value = 1, message = "El número de particiones debe ser al menos 1")
    private Integer partitions;

    // Opcional: pares clave-valor de configuración a modificar, p. ej. retention.ms, cleanup.policy.
    private Map<String, String> config = Map.of();

    public UpdateTopicRequest() {
    }

    public Integer getPartitions() {
        return partitions;
    }

    public void setPartitions(Integer partitions) {
        this.partitions = partitions;
    }

    public Map<String, String> getConfig() {
        return config;
    }

    public void setConfig(Map<String, String> config) {
        this.config = config;
    }
}
