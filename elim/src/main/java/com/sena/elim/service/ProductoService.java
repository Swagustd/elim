package com.sena.elim.service;

import com.sena.elim.model.Producto;
import com.sena.elim.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    private final String uploadDir = "src/main/resources/static/images/";

    public List<Producto> obtenerTodos() {
        return productoRepository.findAll();
    }

    public Optional<Producto> obtenerPorId(Integer id) {
        return productoRepository.findById(id);
    }

    public List<Producto> obtenerProductosPorCategoria(String nombreCategoria) {
        // Mmetodo para que cuadre con prodcutorepository
        return productoRepository.findByCategoriaNombreCategoriaIgnoreCase(nombreCategoria);
    }

    public Producto guardarProducto(Producto producto, MultipartFile imagen) throws IOException {
        if (imagen != null && !imagen.isEmpty()) {
            String nombreImagen = UUID.randomUUID().toString() + "_" + imagen.getOriginalFilename();
            Path path = Paths.get(uploadDir + nombreImagen);
            Files.copy(imagen.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
            producto.setImagenUrl(nombreImagen);
        }

        // Aajuste del stock automatico
        if (producto.getStock() != null && producto.getStock() == 0) {
            producto.setEstado("Agotado");
        } else {
            producto.setEstado("Disponible");
        }

        return productoRepository.save(producto);
    }

    public void eliminarProducto(Integer id) {
        productoRepository.deleteById(id);
    }
}