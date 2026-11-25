package com.pedidos360.servicio_pedido.kafka;

import com.pedidos360.servicio_pedido.entity.Pedido;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class PedidoTrackingEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(PedidoTrackingEventPublisher.class);

    private final KafkaTemplate<String, PedidoTrackingEvent> kafkaTemplate;

    @Value("${kafka.topic.pedidos-tracking}")
    private String pedidosTrackingTopicName;

    public PedidoTrackingEventPublisher(KafkaTemplate<String, PedidoTrackingEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publicarCambioEstado(Pedido pedido) {
        PedidoTrackingEvent evento = new PedidoTrackingEvent(
                pedido.getId(), pedido.getCliente(), pedido.getEstado(), Instant.now());
        // Key = id del pedido: los eventos de un mismo pedido quedan en la misma partición y orden.
        kafkaTemplate.send(pedidosTrackingTopicName, String.valueOf(pedido.getId()), evento);
        log.info("Evento 'pedido.tracking' publicado: {}", evento);
    }
}
