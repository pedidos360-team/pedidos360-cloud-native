package com.pedidos360.servicio_pedido.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.exchange.pedidos}")
    private String pedidosExchangeName;

    @Value("${rabbitmq.exchange.dlx}")
    private String dlxExchangeName;

    @Value("${rabbitmq.queue.notifications}")
    private String notificationsQueueName;

    @Value("${rabbitmq.queue.ticket-generation}")
    private String ticketGenerationQueueName;

    @Value("${rabbitmq.queue.dlq}")
    private String dlqName;

    // ========== Exchange de eventos de pedidos (productor en este servicio) ==========
    @Bean
    public FanoutExchange pedidosEventsExchange() {
        return new FanoutExchange(pedidosExchangeName, true, false);
    }

    // ========== Dead Letter Exchange / Queue (compartido entre servicios) ==========
    @Bean
    public FanoutExchange deadLetterExchange() {
        return new FanoutExchange(dlxExchangeName, true, false);
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(dlqName).build();
    }

    @Bean
    public Binding deadLetterBinding(Queue deadLetterQueue, FanoutExchange deadLetterExchange) {
        return BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange);
    }

    // ========== Cola: generación de tickets (consumida en este servicio) ==========
    @Bean
    public Queue ticketGenerationQueue() {
        return QueueBuilder.durable(ticketGenerationQueueName)
                .withArgument("x-dead-letter-exchange", dlxExchangeName)
                .build();
    }

    @Bean
    public Binding ticketGenerationBinding(Queue ticketGenerationQueue, FanoutExchange pedidosEventsExchange) {
        return BindingBuilder.bind(ticketGenerationQueue).to(pedidosEventsExchange);
    }

    // ========== Cola: notificaciones (consumida en servicio-producto; se declara
    // también aquí para que exista apenas levante cualquiera de los dos servicios) ==========
    @Bean
    public Queue notificationsQueue() {
        return QueueBuilder.durable(notificationsQueueName)
                .withArgument("x-dead-letter-exchange", dlxExchangeName)
                .build();
    }

    @Bean
    public Binding notificationsBinding(Queue notificationsQueue, FanoutExchange pedidosEventsExchange) {
        return BindingBuilder.bind(notificationsQueue).to(pedidosEventsExchange);
    }
}
