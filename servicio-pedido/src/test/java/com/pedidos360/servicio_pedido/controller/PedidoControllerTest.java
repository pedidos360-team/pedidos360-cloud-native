package com.pedidos360.servicio_pedido.controller;

import com.pedidos360.servicio_pedido.SecurityConfig;
import com.pedidos360.servicio_pedido.entity.Pedido;
import com.pedidos360.servicio_pedido.repository.PedidoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PedidoController.class)
@Import(SecurityConfig.class)
class PedidoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PedidoRepository pedidoRepository;

    @MockBean
    private JwtDecoder jwtDecoder;

    @Test
    void listarSinTokenDevuelve401() throws Exception {
        mockMvc.perform(get("/api/pedidos"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listarConTokenValidoDevuelvePedidos() throws Exception {
        when(pedidoRepository.findAll())
                .thenReturn(List.of(new Pedido(1L, "Juan Perez", 849.98, "COMPLETADO")));

        mockMvc.perform(get("/api/pedidos").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].cliente").value("Juan Perez"));
    }
}
