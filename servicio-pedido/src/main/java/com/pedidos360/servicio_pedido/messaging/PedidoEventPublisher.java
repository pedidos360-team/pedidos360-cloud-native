package com.pedidos360.servicio_pedido.messaging;

import com.pedidos360.servicio_pedido.entity.Pedido;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PedidoEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(PedidoEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.pedidos}")
    private String pedidosExchangeName;

    public PedidoEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publicarPedidoCreado(Pedido pedido) {
        String mensaje = String.format("Pedido #%d | Cliente: %s | Total: %.2f | Estado: %s",
                pedido.getId(), pedido.getCliente(), pedido.getTotal(), pedido.getEstado());
        // Exchange fanout: la routing key se ignora, se entrega a todas las colas enlazadas.
        rabbitTemplate.convertAndSend(pedidosExchangeName, "", mensaje);
        log.info("Evento 'pedido.creado' publicado: {}", mensaje);
    }
}
