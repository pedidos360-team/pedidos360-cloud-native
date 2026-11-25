package com.pedidos360.servicio_producto.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consumidor del tópico de tracking de pedidos: le permite a servicio-producto
 * llevar un historial de la actividad de pedidos sin llamar directamente a servicio-pedido.
 */
@Component
public class PedidoTrackingEventListener {

    private static final Logger log = LoggerFactory.getLogger(PedidoTrackingEventListener.class);

    @KafkaListener(topics = "${kafka.topic.pedidos-tracking}", groupId = "servicio-producto-group")
    public void escuchar(PedidoTrackingEvent evento) {
        if (evento == null || evento.pedidoId() == null) {
            // Payload inválido: se deja que el error handler del container registre y reintente.
            throw new IllegalArgumentException("Evento de tracking inválido, falta pedidoId");
        }
        log.info("[TRACKING] Pedido {} de {} cambió a estado: {}", evento.pedidoId(), evento.cliente(), evento.estado());
    }
}
