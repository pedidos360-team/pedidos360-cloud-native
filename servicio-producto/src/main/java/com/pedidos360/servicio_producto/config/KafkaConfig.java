package com.pedidos360.servicio_producto.config;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

/**
 * Spring Boot 4 (usado en este servicio) ya no trae autoconfiguración de Kafka,
 * así que el cliente (productor, consumidor y administración de tópicos) se
 * declara aquí de forma explícita en vez de depender de application.properties.
 * @EnableKafka es necesario para que Spring procese los métodos @KafkaListener;
 * normalmente lo activa la autoconfiguración de Boot, que aquí no existe.
 */
@Configuration
@EnableKafka
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${kafka.topic.pedidos-tracking}")
    private String pedidosTrackingTopicName;

    @Value("${kafka.topic.productos-stock}")
    private String productosStockTopicName;

    private static final String TYPE_MAPPINGS =
            "pedidoTrackingEvent:com.pedidos360.servicio_producto.kafka.PedidoTrackingEvent,"
                    + "stockEvent:com.pedidos360.servicio_producto.kafka.StockEvent";

    // ========== Administración de tópicos (crea los @Bean NewTopic de más abajo al iniciar) ==========
    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return new KafkaAdmin(configs);
    }

    // ========== Tópico: stock de productos (productor en este servicio) ==========
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

    // ========== Tópico: tracking de pedidos (consumido en este servicio; se declara
    // también aquí para que exista apenas levante cualquiera de los dos servicios) ==========
    @Bean
    public NewTopic pedidosTrackingTopic() {
        return TopicBuilder.name(pedidosTrackingTopicName)
                .partitions(3)
                .replicas(2)
                .config("cleanup.policy", "delete")
                .config("retention.ms", String.valueOf(7 * 24 * 60 * 60 * 1000L))
                .build();
    }

    // ========== Productor ==========
    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JacksonJsonSerializer.class);
        configProps.put(JacksonJsonSerializer.TYPE_MAPPINGS, TYPE_MAPPINGS);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    // ========== Consumidor ==========
    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "servicio-producto-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JacksonJsonDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(JacksonJsonDeserializer.TRUSTED_PACKAGES, "com.pedidos360.servicio_producto.kafka");
        props.put(JacksonJsonDeserializer.TYPE_MAPPINGS, TYPE_MAPPINGS);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(
            ConsumerFactory<String, Object> consumerFactory, DefaultErrorHandler kafkaErrorHandler) {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(kafkaErrorHandler);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
        return factory;
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
