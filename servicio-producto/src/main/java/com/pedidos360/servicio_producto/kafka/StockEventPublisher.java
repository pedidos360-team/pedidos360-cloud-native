package com.pedidos360.servicio_producto.kafka;

import com.pedidos360.servicio_producto.entity.Producto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class StockEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(StockEventPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topic.productos-stock}")
    private String productosStockTopicName;

    public StockEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publicarCambioStock(Producto producto) {
        StockEvent evento = new StockEvent(
                producto.getId(), producto.getNombre(), producto.getStock(), Instant.now());
        // Key = id del producto: tópico compactado, se queda con el último valor por clave.
        kafkaTemplate.send(productosStockTopicName, String.valueOf(producto.getId()), evento);
        log.info("Evento 'producto.stock' publicado: {}", evento);
    }
}
