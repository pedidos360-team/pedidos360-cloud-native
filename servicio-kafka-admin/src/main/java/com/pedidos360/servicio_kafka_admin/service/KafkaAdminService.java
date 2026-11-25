package com.pedidos360.servicio_kafka_admin.service;

import com.pedidos360.servicio_kafka_admin.dto.ConsumerGroupDetail;
import com.pedidos360.servicio_kafka_admin.dto.ConsumerGroupSummary;
import com.pedidos360.servicio_kafka_admin.dto.CreateTopicRequest;
import com.pedidos360.servicio_kafka_admin.dto.TopicSummary;
import com.pedidos360.servicio_kafka_admin.dto.UpdateTopicRequest;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.Config;
import org.apache.kafka.clients.admin.ConfigEntry;
import org.apache.kafka.clients.admin.ConsumerGroupDescription;
import org.apache.kafka.clients.admin.ConsumerGroupListing;
import org.apache.kafka.clients.admin.NewPartitions;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.config.ConfigResource;
import org.apache.kafka.common.errors.TopicExistsException;
import org.apache.kafka.common.errors.UnknownTopicOrPartitionException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * Encapsula toda la lógica de administración de Kafka (creación, actualización,
 * eliminación e inspección de tópicos y consumer groups) para mantenerla
 * separada de los controladores REST.
 */
@Service
public class KafkaAdminService {

    private final AdminClient adminClient;

    public KafkaAdminService(AdminClient adminClient) {
        this.adminClient = adminClient;
    }

    public void crearTopico(CreateTopicRequest request) {
        NewTopic nuevoTopico = new NewTopic(request.getName(), request.getPartitions(), request.getReplicationFactor())
                .configs(request.getConfig());
        esperar(adminClient.createTopics(List.of(nuevoTopico)).all(), "crear el tópico");
    }

    public List<String> listarTopicos() {
        Set<String> nombres = esperar(adminClient.listTopics().names(), "listar los tópicos");
        return nombres.stream()
                .filter(nombre -> !nombre.startsWith("__"))
                .sorted()
                .collect(Collectors.toList());
    }

    public TopicSummary obtenerTopico(String nombre) {
        try {
            Map<String, TopicDescription> descripciones = esperar(
                    adminClient.describeTopics(List.of(nombre)).allTopicNames(), "describir el tópico");
            TopicDescription descripcion = descripciones.get(nombre);

            ConfigResource recurso = new ConfigResource(ConfigResource.Type.TOPIC, nombre);
            Map<ConfigResource, Config> configs = esperar(
                    adminClient.describeConfigs(List.of(recurso)).all(), "obtener la configuración del tópico");
            Map<String, String> configMap = new LinkedHashMap<>();
            for (ConfigEntry entry : configs.get(recurso).entries()) {
                configMap.put(entry.name(), entry.value());
            }

            int particiones = descripcion.partitions().size();
            int factorReplica = descripcion.partitions().isEmpty() ? 0 : descripcion.partitions().get(0).replicas().size();
            return new TopicSummary(nombre, particiones, factorReplica, configMap);
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    public void actualizarTopico(String nombre, UpdateTopicRequest request) {
        if (request.getPartitions() != null) {
            esperar(adminClient.createPartitions(Map.of(nombre, NewPartitions.increaseTo(request.getPartitions()))).all(),
                    "aumentar las particiones del tópico");
        }
        if (request.getConfig() != null && !request.getConfig().isEmpty()) {
            ConfigResource recurso = new ConfigResource(ConfigResource.Type.TOPIC, nombre);
            Map<ConfigResource, org.apache.kafka.clients.admin.Config> configs = Map.of(recurso,
                    new org.apache.kafka.clients.admin.Config(request.getConfig().entrySet().stream()
                            .map(e -> new ConfigEntry(e.getKey(), e.getValue()))
                            .collect(Collectors.toList())));
            esperar(adminClient.alterConfigs(configs).all(), "actualizar la configuración del tópico");
        }
    }

    public boolean eliminarTopico(String nombre) {
        try {
            esperar(adminClient.deleteTopics(List.of(nombre)).all(), "eliminar el tópico");
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    public List<ConsumerGroupSummary> listarConsumerGroups() {
        Collection<ConsumerGroupListing> listados = esperar(adminClient.listConsumerGroups().all(), "listar los consumer groups");
        return listados.stream()
                .map(g -> new ConsumerGroupSummary(g.groupId(), g.state().map(Enum::toString).orElse("DESCONOCIDO")))
                .collect(Collectors.toList());
    }

    public ConsumerGroupDetail describirConsumerGroup(String groupId) {
        try {
            Map<String, ConsumerGroupDescription> descripciones = esperar(
                    adminClient.describeConsumerGroups(List.of(groupId)).all(), "describir el consumer group");
            ConsumerGroupDescription descripcion = descripciones.get(groupId);
            if (descripcion == null || descripcion.members().isEmpty() && descripcion.state().toString().equals("DEAD")) {
                return null;
            }

            Set<String> topicos = new LinkedHashSet<>();
            descripcion.members().forEach(miembro ->
                    miembro.assignment().topicPartitions().forEach(tp -> topicos.add(tp.topic())));

            return new ConsumerGroupDetail(groupId, descripcion.state().toString(), descripcion.members().size(),
                    topicos.stream().sorted().collect(Collectors.toList()));
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    private <T> T esperar(KafkaFuture<T> future, String accion) {
        try {
            return future.get(10, TimeUnit.SECONDS);
        } catch (ExecutionException e) {
            throw traducirError(e.getCause(), accion);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Operación interrumpida al " + accion, e);
        } catch (TimeoutException e) {
            throw new IllegalStateException("Tiempo de espera agotado al " + accion, e);
        }
    }

    private RuntimeException traducirError(Throwable causa, String accion) {
        if (causa instanceof TopicExistsException) {
            return new IllegalArgumentException("El tópico ya existe");
        }
        if (causa instanceof UnknownTopicOrPartitionException) {
            return new NoSuchElementException("No se encontró el recurso solicitado");
        }
        return new IllegalStateException("Error al " + accion + ": " + causa.getMessage(), causa);
    }
}
