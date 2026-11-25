package com.pedidos360.servicio_producto.kafka;

import java.time.Instant;

public record StockEvent(Long productoId, String nombre, Integer stock, Instant timestamp) {
}
