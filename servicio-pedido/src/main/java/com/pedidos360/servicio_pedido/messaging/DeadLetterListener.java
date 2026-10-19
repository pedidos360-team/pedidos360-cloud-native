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
 * Consumidor de la Dead Letter Queue compartida: solo registra los mensajes
 * que no pudieron procesarse para su posterior revisión/auditoría.
 */
@Component
public class DeadLetterListener {

    private static final Logger log = LoggerFactory.getLogger(DeadLetterListener.class);

    @RabbitListener(queues = "${rabbitmq.queue.dlq}")
    public void registrarMensajeMuerto(String mensaje, Channel channel,
                                        @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        log.warn("[DLQ] Mensaje descartado recibido en dead letter queue: {}", mensaje);
        channel.basicAck(deliveryTag, false);
    }
}
