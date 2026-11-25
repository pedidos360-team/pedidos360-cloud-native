package com.pedidos360.servicio_pedido.messaging;

import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class TicketGenerationListenerTest {

    private final TicketGenerationListener listener = new TicketGenerationListener();

    @Test
    void mensajeValidoHaceAckManual() throws Exception {
        Channel channel = Mockito.mock(Channel.class);

        listener.generarTicket("Pedido #1 | Cliente: Juan | Total: 10.0 | Estado: PENDIENTE", channel, 1L);

        verify(channel).basicAck(1L, false);
        verify(channel, never()).basicNack(Mockito.anyLong(), Mockito.anyBoolean(), Mockito.anyBoolean());
    }

    @Test
    void mensajeInvalidoHaceNackSinRequeueParaIrALaDlq() throws Exception {
        Channel channel = Mockito.mock(Channel.class);

        listener.generarTicket("", channel, 1L);

        verify(channel).basicNack(1L, false, false);
        verify(channel, never()).basicAck(Mockito.anyLong(), Mockito.anyBoolean());
    }
}
