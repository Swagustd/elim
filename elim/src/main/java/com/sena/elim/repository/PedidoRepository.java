package com.sena.elim.repository;

import com.sena.elim.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Integer> {
    List<Pedido> findByDocumentoUsuarioFk(String documentoUsuarioFk);

    // metodo para filtrar los pedidos por estadoaao
    List<Pedido> findByEstadoPedido(String estadoPedido);
}