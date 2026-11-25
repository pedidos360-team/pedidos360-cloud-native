package com.pedidos360.servicio_kafka_admin.controller;

import com.pedidos360.servicio_kafka_admin.dto.ConsumerGroupDetail;
import com.pedidos360.servicio_kafka_admin.dto.ConsumerGroupSummary;
import com.pedidos360.servicio_kafka_admin.dto.CreateTopicRequest;
import com.pedidos360.servicio_kafka_admin.dto.TopicSummary;
import com.pedidos360.servicio_kafka_admin.dto.UpdateTopicRequest;
import com.pedidos360.servicio_kafka_admin.service.KafkaAdminService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class KafkaAdminController {

    private final KafkaAdminService kafkaAdminService;

    public KafkaAdminController(KafkaAdminService kafkaAdminService) {
        this.kafkaAdminService = kafkaAdminService;
    }

    @PostMapping("/topics")
    public ResponseEntity<Void> crearTopico(@Valid @RequestBody CreateTopicRequest request) {
        kafkaAdminService.crearTopico(request);
        return ResponseEntity.status(201).build();
    }

    @GetMapping("/topics")
    public List<String> listarTopicos() {
        return kafkaAdminService.listarTopicos();
    }

    @GetMapping("/topics/{nombre}")
    public ResponseEntity<TopicSummary> obtenerTopico(@PathVariable String nombre) {
        TopicSummary resumen = kafkaAdminService.obtenerTopico(nombre);
        return resumen != null ? ResponseEntity.ok(resumen) : ResponseEntity.notFound().build();
    }

    @PatchMapping("/topics/{nombre}")
    public ResponseEntity<Void> actualizarTopico(@PathVariable String nombre, @RequestBody UpdateTopicRequest request) {
        kafkaAdminService.actualizarTopico(nombre, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/topics/{nombre}")
    public ResponseEntity<Void> eliminarTopico(@PathVariable String nombre) {
        boolean eliminado = kafkaAdminService.eliminarTopico(nombre);
        return eliminado ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @GetMapping("/consumer-groups")
    public List<ConsumerGroupSummary> listarConsumerGroups() {
        return kafkaAdminService.listarConsumerGroups();
    }

    @GetMapping("/consumer-groups/{groupId}")
    public ResponseEntity<ConsumerGroupDetail> describirConsumerGroup(@PathVariable String groupId) {
        ConsumerGroupDetail detalle = kafkaAdminService.describirConsumerGroup(groupId);
        return detalle != null ? ResponseEntity.ok(detalle) : ResponseEntity.notFound().build();
    }
}
