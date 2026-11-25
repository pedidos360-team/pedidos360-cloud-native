package com.pedidos360.servicio_pedido.controller;

import com.pedidos360.servicio_pedido.entity.Pedido;
import com.pedidos360.servicio_pedido.kafka.PedidoTrackingEventPublisher;
import com.pedidos360.servicio_pedido.messaging.PedidoEventPublisher;
import com.pedidos360.servicio_pedido.repository.PedidoRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoRepository pedidoRepository;
    private final PedidoEventPublisher pedidoEventPublisher;
    private final PedidoTrackingEventPublisher pedidoTrackingEventPublisher;

    public PedidoController(PedidoRepository pedidoRepository,
                             PedidoEventPublisher pedidoEventPublisher,
                             PedidoTrackingEventPublisher pedidoTrackingEventPublisher) {
        this.pedidoRepository = pedidoRepository;
        this.pedidoEventPublisher = pedidoEventPublisher;
        this.pedidoTrackingEventPublisher = pedidoTrackingEventPublisher;
    }

    @PostConstruct
    public void initData() {
        if (pedidoRepository.count() == 0) {
            pedidoRepository.save(new Pedido(null, "Juan Perez", 849.98, "COMPLETADO"));
            pedidoRepository.save(new Pedido(null, "Maria Soto", 49.99, "PENDIENTE"));
        }
    }

    @GetMapping
    public List<Pedido> listar() {
        return pedidoRepository.findAll();
    }

    @PostMapping
    public Pedido crear(@RequestBody Pedido pedido) {
        pedido.setId(null);
        Pedido guardado = pedidoRepository.save(pedido);
        pedidoEventPublisher.publicarPedidoCreado(guardado);
        return guardado;
    }

    @PatchMapping("/{id}/estado")
    public Pedido cambiarEstado(@PathVariable Long id, @RequestBody CambiarEstadoRequest request) {
        if (request.estado() == null || request.estado().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El estado no puede estar vacío");
        }
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido no encontrado"));
        pedido.setEstado(request.estado());
        Pedido actualizado = pedidoRepository.save(pedido);
        // Evento de tracking para Kafka: distinto del evento de RabbitMQ, que solo se dispara al crear.
        pedidoTrackingEventPublisher.publicarCambioEstado(actualizado);
        return actualizado;
    }

    public record CambiarEstadoRequest(String estado) {
    }
}