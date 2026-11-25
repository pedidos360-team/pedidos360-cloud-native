package com.pedidos360.servicio_pedido.kafka;

import java.time.Instant;

public record StockEvent(Long productoId, String nombre, Integer stock, Instant timestamp) {
}
