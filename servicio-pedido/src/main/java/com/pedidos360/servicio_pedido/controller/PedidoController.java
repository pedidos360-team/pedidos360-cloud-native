package com.pedidos360.servicio_pedido.controller;

import com.pedidos360.servicio_pedido.entity.Pedido;
import com.pedidos360.servicio_pedido.repository.PedidoRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoRepository pedidoRepository;

    public PedidoController(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
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
}