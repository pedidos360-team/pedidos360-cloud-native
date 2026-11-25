package com.pedidos360.servicio_producto.messaging;

import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Consumidor del dominio "notificaciones": simula el envío de una
 * notificación (ej. correo/push) cada vez que se crea un pedido.
 */
@Component
public class NotificationListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationListener.class);

    @RabbitListener(queues = "${rabbitmq.queue.notifications}")
    public void notificar(String mensaje, Channel channel,
                           @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        try {
            log.info("[NOTIFICACION] Enviando notificación para: {}", mensaje);

            if (mensaje == null || mensaje.isBlank()) {
                // Error no recuperable: no hay datos válidos para notificar.
                throw new IllegalArgumentException("Mensaje vacío, no se puede notificar");
            }

            // Simulación de envío de notificación.
            log.info("[NOTIFICACION] Notificación enviada correctamente para: {}", mensaje);
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("[NOTIFICACION] Error no recuperable al notificar, enviando a DLQ: {}", e.getMessage());
            channel.basicNack(deliveryTag, false, false);
        }
    }
}
