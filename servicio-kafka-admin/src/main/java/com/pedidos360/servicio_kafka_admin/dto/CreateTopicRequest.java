package com.pedidos360.servicio_kafka_admin.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.util.Map;

public class CreateTopicRequest {

    @NotBlank(message = "El nombre del tópico no puede estar vacío")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "El nombre del tópico contiene caracteres inválidos")
    private String name;

    @Min(value = 1, message = "El tópico debe tener al menos 1 partición")
    private int partitions = 1;

    @Min(value = 1, message = "El factor de réplica debe ser al menos 1")
    private short replicationFactor = 1;

    private Map<String, String> config = Map.of();

    public CreateTopicRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPartitions() {
        return partitions;
    }

    public void setPartitions(int partitions) {
        this.partitions = partitions;
    }

    public short getReplicationFactor() {
        return replicationFactor;
    }

    public void setReplicationFactor(short replicationFactor) {
        this.replicationFactor = replicationFactor;
    }

    public Map<String, String> getConfig() {
        return config;
    }

    public void setConfig(Map<String, String> config) {
        this.config = config;
    }
}
