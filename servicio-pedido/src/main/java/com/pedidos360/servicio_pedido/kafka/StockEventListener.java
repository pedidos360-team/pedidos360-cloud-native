package com.pedidos360.servicio_pedido.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consumidor del tópico de stock de productos: le permite a servicio-pedido
 * enterarse de la disponibilidad de productos sin llamar directamente a servicio-producto.
 */
@Component
public class StockEventListener {

    private static final Logger log = LoggerFactory.getLogger(StockEventListener.class);

    @KafkaListener(topics = "${kafka.topic.productos-stock}", groupId = "servicio-pedido-group")
    public void escuchar(StockEvent evento) {
        if (evento == null || evento.productoId() == null) {
            // Payload inválido: se deja que el error handler del container registre y reintente.
            throw new IllegalArgumentException("Evento de stock inválido, falta productoId");
        }
        log.info("[STOCK] Producto {} ({}) quedó con stock: {}", evento.productoId(), evento.nombre(), evento.stock());
    }
}
