package com.sena.elim.repository;

import com.sena.elim.model.Devolucion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface DevolucionRepository extends JpaRepository<Devolucion, Integer> {

    @Query("SELECT d FROM Devolucion d WHERE d.id_pedido = :idPedido")
    Optional<Devolucion> buscarPorIdPedido(@Param("idPedido") Integer idPedido);
}