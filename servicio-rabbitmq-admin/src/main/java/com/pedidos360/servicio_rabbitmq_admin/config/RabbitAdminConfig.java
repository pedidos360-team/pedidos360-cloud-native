package com.pedidos360.servicio_rabbitmq_admin.config;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de administración de RabbitMQ, separada de la lógica de
 * negocio: expone el RabbitAdmin usado por RabbitAdminService para
 * crear/eliminar colas, exchanges y bindings en tiempo de ejecución.
 */
@Configuration
public class RabbitAdminConfig {

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }
}
