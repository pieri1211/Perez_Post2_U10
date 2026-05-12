package com.universidad.productosservices.service;

import com.universidad.productosservices.domain.Producto;
import com.universidad.productosservices.repository.ProductoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    public Producto procesarProducto(String nombre, Double precio, Integer stock) {
        validarDatos(nombre, precio, stock);
        Producto producto = new Producto();
        producto.setNombre(nombre.strip());
        producto.setPrecio(precio);
        producto.setStock(stock);
        return productoRepository.save(producto);
    }

    private void validarDatos(String nombre, Double precio, Integer stock) {
        if (nombre == null || nombre.isBlank())
            throw new IllegalArgumentException("El nombre no puede estar vacío");
        if (precio == null || precio <= 0)
            throw new IllegalArgumentException("El precio debe ser mayor a cero");
        if (precio > 999999)
            throw new IllegalArgumentException("El precio excede el máximo permitido");
        if (stock == null || stock < 0)
            throw new IllegalArgumentException("El stock no puede ser negativo");
    }

    public List<Producto> listar() {
        return productoRepository.findAll();
    }

    public Producto buscar(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() ->
                        new NoSuchElementException("Producto no encontrado: " + id));
    }
}