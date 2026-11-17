package com.sena.elim.repository;

import com.sena.elim.model.ProductoTalla;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ProductoTallaRepository extends JpaRepository<ProductoTalla, Integer> {

    // metodo para conexion con producto
    Optional<ProductoTalla> findByProductoIdProductoAndTalla(Integer idProducto, String talla);
}