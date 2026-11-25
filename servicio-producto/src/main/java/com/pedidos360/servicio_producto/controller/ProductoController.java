package com.pedidos360.servicio_producto.controller;

import com.pedidos360.servicio_producto.entity.Producto;
import com.pedidos360.servicio_producto.kafka.StockEventPublisher;
import com.pedidos360.servicio_producto.repository.ProductoRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoRepository productoRepository;
    private final StockEventPublisher stockEventPublisher;

    public ProductoController(ProductoRepository productoRepository, StockEventPublisher stockEventPublisher) {
        this.productoRepository = productoRepository;
        this.stockEventPublisher = stockEventPublisher;
    }

    @PostConstruct
    public void initData() {
        if (productoRepository.count() == 0) {
            productoRepository.save(new Producto(null, "Laptop HP Cloud", 799.99, 10));
            productoRepository.save(new Producto(null, "Teclado Mecánico RGB", 49.99, 25));
        }
    }

    @GetMapping
    public List<Producto> listar() {
        return productoRepository.findAll();
    }

    @PostMapping
    public Producto crear(@RequestBody Producto producto) {
        producto.setId(null);
        return productoRepository.save(producto);
    }

    @PatchMapping("/{id}/stock")
    public Producto actualizarStock(@PathVariable Long id, @RequestBody ActualizarStockRequest request) {
        if (request.stock() == null || request.stock() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El stock debe ser un número igual o mayor a 0");
        }
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
        producto.setStock(request.stock());
        Producto actualizado = productoRepository.save(producto);
        stockEventPublisher.publicarCambioStock(actualizado);
        return actualizado;
    }

    public record ActualizarStockRequest(Integer stock) {
    }
}
