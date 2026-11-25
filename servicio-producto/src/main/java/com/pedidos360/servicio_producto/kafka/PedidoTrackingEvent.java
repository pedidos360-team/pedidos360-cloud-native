package com.pedidos360.servicio_producto.kafka;

import java.time.Instant;

public record PedidoTrackingEvent(Long pedidoId, String cliente, String estado, Instant timestamp) {
}
