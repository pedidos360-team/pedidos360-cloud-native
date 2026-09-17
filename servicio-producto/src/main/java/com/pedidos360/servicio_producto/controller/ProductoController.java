package com.pedidos360.servicio_producto.controller;

import com.pedidos360.servicio_producto.entity.Producto;
import com.pedidos360.servicio_producto.repository.ProductoRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoRepository productoRepository;

    public ProductoController(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
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
        return productoRepository.save(producto);
    }
}
