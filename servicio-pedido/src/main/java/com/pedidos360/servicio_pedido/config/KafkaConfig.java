package com.pedidos360.servicio_pedido.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConfig {

    @Value("${kafka.topic.pedidos-tracking}")
    private String pedidosTrackingTopicName;

    @Value("${kafka.topic.productos-stock}")
    private String productosStockTopicName;

    // ========== Tópico: tracking de pedidos (productor en este servicio) ==========
    // Flujo de alto volumen tipo "log de eventos": se conserva el historial completo
    // por 7 días para poder hacer analítica de tiempos de preparación.
    @Bean
    public NewTopic pedidosTrackingTopic() {
        return TopicBuilder.name(pedidosTrackingTopicName)
                .partitions(3)
                .replicas(2)
                .config("cleanup.policy", "delete")
                .config("retention.ms", String.valueOf(7 * 24 * 60 * 60 * 1000L))
                .build();
    }

    // ========== Tópico: stock de productos (consumido en este servicio; se declara
    // también aquí para que exista apenas levante cualquiera de los dos servicios) ==========
    // Solo interesa el último valor de stock por producto, no el historial completo,
    // por eso usa compactación en vez de retención por tiempo.
    @Bean
    public NewTopic productosStockTopic() {
        return TopicBuilder.name(productosStockTopicName)
                .partitions(3)
                .replicas(2)
                .config("cleanup.policy", "compact")
                .config("min.cleanable.dirty.ratio", "0.1")
                .build();
    }

    // Reintenta 2 veces con 1s de espera ante un payload inválido o una falla transitoria;
    // si sigue fallando, registra el error en el log y continúa con el siguiente mensaje.
    @Bean
    public DefaultErrorHandler kafkaErrorHandler() {
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(new FixedBackOff(1000L, 2));
        errorHandler.setCommitRecovered(true);
        return errorHandler;
    }
}
