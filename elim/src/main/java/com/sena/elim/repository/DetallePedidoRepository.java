package com.sena.elim.repository;

import com.sena.elim.model.DetallePedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DetallePedidoRepository extends JpaRepository<DetallePedido, Integer> {

    @Query("SELECT d FROM DetallePedido d WHERE d.idPedidoFk = :idPedidoFk")
    List<DetallePedido> findByIdPedidoFk(@Param("idPedidoFk") Integer idPedidoFk);
}