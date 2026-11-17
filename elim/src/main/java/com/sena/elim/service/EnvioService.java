package com.sena.elim.service;

import com.sena.elim.model.Envio;
import com.sena.elim.model.Pedido;
import com.sena.elim.repository.EnvioRepository;
import com.sena.elim.repository.PedidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class EnvioService {

    @Autowired
    private EnvioRepository envioRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    public List<Envio> listarTodosLosEnvios() {
        return envioRepository.findAll();
    }

    // metodo para buscar el pedido por Id
    public Envio buscarEnvioPorId(Integer idEnvio) {
        return envioRepository.findById(idEnvio).orElse(null);
    }

    // metodo para obtener solo los pedidos listos para envio
    public List<Pedido> listarPedidosListosParaEnvio() {
        return pedidoRepository.findByEstadoPedido("Enviado");
    }

    // metodo para que el adminpedidocontroler procese el estado
    public void procesarCambioEstadoPedido(Pedido pedido, String nuevoEstado) {
        pedido.setEstadoPedido(nuevoEstado);
    }

    // Método para guardar un nuevo envío desde el formulario de la vista de envíos
    public void guardarEnvio(Envio envio) {
        if (envio.getFecha_envio() == null) {
            envio.setFecha_envio(LocalDate.now());
        }
        envioRepository.save(envio);
    }
}