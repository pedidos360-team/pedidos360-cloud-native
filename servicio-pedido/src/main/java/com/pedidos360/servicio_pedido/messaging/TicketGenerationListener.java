package com.pedidos360.servicio_pedido.messaging;

import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Consumidor del dominio "generación de tickets": simula la creación de un
 * comprobante/ticket cada vez que se crea un pedido.
 */
@Component
public class TicketGenerationListener {

    private static final Logger log = LoggerFactory.getLogger(TicketGenerationListener.class);

    @RabbitListener(queues = "${rabbitmq.queue.ticket-generation}")
    public void generarTicket(String mensaje, Channel channel,
                               @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        try {
            log.info("[TICKET] Generando comprobante para: {}", mensaje);

            if (mensaje == null || mensaje.isBlank()) {
                // Error no recuperable: el mensaje no trae datos válidos para generar el ticket.
                throw new IllegalArgumentException("Mensaje vacío, no se puede generar el ticket");
            }

            // Simulación de generación de ticket/comprobante.
            log.info("[TICKET] Comprobante generado correctamente para: {}", mensaje);
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("[TICKET] Error no recuperable al generar comprobante, enviando a DLQ: {}", e.getMessage());
            channel.basicNack(deliveryTag, false, false);
        }
    }
}
