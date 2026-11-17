package com.sena.elim.service;

import com.sena.elim.model.Devolucion;
import com.sena.elim.model.Pedido;
import com.sena.elim.repository.DevolucionRepository;
import com.sena.elim.repository.PedidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class DevolucionService {

    @Autowired
    private DevolucionRepository devolucionRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    public List<Devolucion> listarTodasLasDevoluciones() {
        return devolucionRepository.findAll();
    }

    public Devolucion buscarDevolucionPorId(Integer idDevolucion) {
        return devolucionRepository.findById(idDevolucion).orElse(null);
    }

    public boolean solicitarDevolucion(Integer idPedido, String motivo) {
        Pedido pedido = pedidoRepository.findById(idPedido).orElse(null);
        if (pedido == null) {
            return false;
        }

        if (devolucionRepository.buscarPorIdPedido(idPedido).isPresent()) {
            return false;
        }

        Devolucion devolucion = new Devolucion();
        devolucion.setId_pedido(idPedido);
        devolucion.setMotivo(motivo);
        devolucion.setEstado_devolucion("Solicitada");
        devolucion.setFecha_devolucion(LocalDate.now());
        devolucion.setNombre_producto("Pedido #" + idPedido);
        devolucion.setCantidad_devuelta(1);

        // guardar la devolucion
        devolucionRepository.save(devolucion);

        // Aactualizar el estado del pedido
        pedido.setEstadoPedido("Proceso de devolución pendiente");
        pedidoRepository.save(pedido);

        return true;
    }

    public void actualizarEstadoDevolucion(Integer idDevolucion, String nuevoEstado) {
        Devolucion devolucion = buscarDevolucionPorId(idDevolucion);
        if (devolucion != null) {
            devolucion.setEstado_devolucion(nuevoEstado);
            devolucionRepository.save(devolucion);

            Pedido pedido = pedidoRepository.findById(devolucion.getId_pedido()).orElse(null);
            if (pedido != null) {
                if ("Aprobada".equals(nuevoEstado)) {
                    pedido.setEstadoPedido("Devuelto");
                } else if ("Rechazada".equals(nuevoEstado)) {
                    pedido.setEstadoPedido("Cancelado");
                }
                pedidoRepository.save(pedido);
            }
        }
    }
}