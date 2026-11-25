package com.pedidos360.servicio_rabbitmq_admin.controller;

import com.pedidos360.servicio_rabbitmq_admin.service.RabbitAdminService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RabbitMQAdminController.class)
class RabbitMQAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RabbitAdminService rabbitAdminService;

    @Test
    void crearColaConNombreVacioDevuelve400() throws Exception {
        mockMvc.perform(post("/api/admin/queues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void crearColaValidaDevuelve201() throws Exception {
        mockMvc.perform(post("/api/admin/queues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"cola.prueba\",\"durable\":true}"))
                .andExpect(status().isCreated());

        verify(rabbitAdminService).crearCola(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void crearExchangeConTipoInvalidoDevuelve400() throws Exception {
        mockMvc.perform(post("/api/admin/exchanges")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"ex.prueba\",\"type\":\"inexistente\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void eliminarBindingConDatosValidosDevuelve204() throws Exception {
        mockMvc.perform(delete("/api/admin/bindings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"queue\":\"cola.prueba\",\"exchange\":\"ex.prueba\",\"routingKey\":\"\"}"))
                .andExpect(status().isNoContent());
    }
}
