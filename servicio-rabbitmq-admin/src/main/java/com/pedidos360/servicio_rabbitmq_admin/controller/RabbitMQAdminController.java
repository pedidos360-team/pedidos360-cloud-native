package com.pedidos360.servicio_rabbitmq_admin.controller;

import com.pedidos360.servicio_rabbitmq_admin.dto.CreateBindingRequest;
import com.pedidos360.servicio_rabbitmq_admin.dto.CreateExchangeRequest;
import com.pedidos360.servicio_rabbitmq_admin.dto.CreateQueueRequest;
import com.pedidos360.servicio_rabbitmq_admin.service.RabbitAdminService;
import jakarta.validation.Valid;
import org.springframework.amqp.core.QueueInformation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class RabbitMQAdminController {

    private final RabbitAdminService rabbitAdminService;

    public RabbitMQAdminController(RabbitAdminService rabbitAdminService) {
        this.rabbitAdminService = rabbitAdminService;
    }

    @PostMapping("/queues")
    public ResponseEntity<Void> crearCola(@Valid @RequestBody CreateQueueRequest request) {
        rabbitAdminService.crearCola(request);
        return ResponseEntity.status(201).build();
    }

    @DeleteMapping("/queues/{nombre}")
    public ResponseEntity<Void> eliminarCola(@PathVariable String nombre) {
        boolean eliminada = rabbitAdminService.eliminarCola(nombre);
        return eliminada ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @GetMapping("/queues/{nombre}")
    public ResponseEntity<QueueInformation> obtenerCola(@PathVariable String nombre) {
        QueueInformation info = rabbitAdminService.obtenerInfoCola(nombre);
        return info != null ? ResponseEntity.ok(info) : ResponseEntity.notFound().build();
    }

    @PostMapping("/exchanges")
    public ResponseEntity<Void> crearExchange(@Valid @RequestBody CreateExchangeRequest request) {
        rabbitAdminService.crearExchange(request);
        return ResponseEntity.status(201).build();
    }

    @DeleteMapping("/exchanges/{nombre}")
    public ResponseEntity<Void> eliminarExchange(@PathVariable String nombre) {
        boolean eliminado = rabbitAdminService.eliminarExchange(nombre);
        return eliminado ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @PostMapping("/bindings")
    public ResponseEntity<Void> crearBinding(@Valid @RequestBody CreateBindingRequest request) {
        rabbitAdminService.crearBinding(request);
        return ResponseEntity.status(201).build();
    }

    @DeleteMapping("/bindings")
    public ResponseEntity<Void> eliminarBinding(@Valid @RequestBody CreateBindingRequest request) {
        rabbitAdminService.eliminarBinding(request);
        return ResponseEntity.noContent().build();
    }
}
