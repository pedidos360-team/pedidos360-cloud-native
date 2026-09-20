package com.pedidos360.servicio_producto.controller;

import com.pedidos360.servicio_producto.SecurityConfig;
import com.pedidos360.servicio_producto.entity.Producto;
import com.pedidos360.servicio_producto.repository.ProductoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductoController.class)
@Import(SecurityConfig.class)
class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductoRepository productoRepository;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void listarSinTokenDevuelve401() throws Exception {
        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listarConTokenValidoDevuelveProductos() throws Exception {
        when(productoRepository.findAll())
                .thenReturn(List.of(new Producto(1L, "Mouse", 9990.0, 15)));

        mockMvc.perform(get("/api/productos").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Mouse"));
    }

    @Test
    void crearIgnoraIdEnviadoPorElCliente() throws Exception {
        when(productoRepository.save(any(Producto.class)))
                .thenAnswer(invocation -> {
                    Producto producto = invocation.getArgument(0);
                    producto.setId(5L);
                    return producto;
                });

        mockMvc.perform(post("/api/productos")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":99,\"nombre\":\"Teclado\",\"precio\":19990.0,\"stock\":3}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.nombre").value("Teclado"));
    }
}
