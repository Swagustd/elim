package com.sena.elim.repository;

import com.sena.elim.model.Envio;
import com.sena.elim.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EnvioRepository extends JpaRepository<Envio, Integer> {
    // buscar envio vinculado a un envio especifico
    Optional<Envio> findByPedido(Pedido pedido);
}