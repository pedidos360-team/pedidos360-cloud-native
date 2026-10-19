package com.pedidos360.servicio_rabbitmq_admin.service;

import com.pedidos360.servicio_rabbitmq_admin.dto.CreateBindingRequest;
import com.pedidos360.servicio_rabbitmq_admin.dto.CreateExchangeRequest;
import com.pedidos360.servicio_rabbitmq_admin.dto.CreateQueueRequest;
import org.springframework.amqp.core.AbstractExchange;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.HeadersExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.QueueInformation;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.stereotype.Service;

/**
 * Encapsula toda la lógica de administración de RabbitMQ (creación y
 * eliminación de colas, exchanges y bindings) para mantenerla separada de
 * los controladores REST.
 */
@Service
public class RabbitAdminService {

    private final RabbitAdmin rabbitAdmin;

    public RabbitAdminService(RabbitAdmin rabbitAdmin) {
        this.rabbitAdmin = rabbitAdmin;
    }

    public void crearCola(CreateQueueRequest request) {
        Queue queue = QueueBuilder.durable(request.getName())
                .build();
        if (!request.isDurable()) {
            queue = new Queue(request.getName(), false);
        }
        rabbitAdmin.declareQueue(queue);
    }

    public boolean eliminarCola(String nombre) {
        return rabbitAdmin.deleteQueue(nombre);
    }

    public QueueInformation obtenerInfoCola(String nombre) {
        return rabbitAdmin.getQueueInfo(nombre);
    }

    public void crearExchange(CreateExchangeRequest request) {
        AbstractExchange exchange = switch (request.getType()) {
            case "direct" -> new DirectExchange(request.getName(), request.isDurable(), false);
            case "fanout" -> new FanoutExchange(request.getName(), request.isDurable(), false);
            case "topic" -> new TopicExchange(request.getName(), request.isDurable(), false);
            case "headers" -> new HeadersExchange(request.getName(), request.isDurable(), false);
            default -> throw new IllegalArgumentException("Tipo de exchange no soportado: " + request.getType());
        };
        rabbitAdmin.declareExchange(exchange);
    }

    public boolean eliminarExchange(String nombre) {
        return rabbitAdmin.deleteExchange(nombre);
    }

    public void crearBinding(CreateBindingRequest request) {
        Binding binding = new Binding(
                request.getQueue(),
                Binding.DestinationType.QUEUE,
                request.getExchange(),
                request.getRoutingKey() == null ? "" : request.getRoutingKey(),
                null);
        rabbitAdmin.declareBinding(binding);
    }

    public void eliminarBinding(CreateBindingRequest request) {
        Binding binding = new Binding(
                request.getQueue(),
                Binding.DestinationType.QUEUE,
                request.getExchange(),
                request.getRoutingKey() == null ? "" : request.getRoutingKey(),
                null);
        rabbitAdmin.removeBinding(binding);
    }
}
