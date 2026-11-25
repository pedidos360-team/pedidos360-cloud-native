package com.pedidos360.servicio_kafka_admin.dto;

import java.util.Map;

public record TopicSummary(String name, int partitions, int replicationFactor, Map<String, String> config) {
}
