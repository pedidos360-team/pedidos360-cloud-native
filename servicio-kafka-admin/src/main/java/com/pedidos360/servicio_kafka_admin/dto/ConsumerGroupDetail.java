package com.pedidos360.servicio_kafka_admin.dto;

import java.util.List;

public record ConsumerGroupDetail(String groupId, String state, int memberCount, List<String> topics) {
}
