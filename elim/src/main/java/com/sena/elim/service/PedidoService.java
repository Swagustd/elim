package com.sena.elim.service;

import com.sena.elim.model.DetallePedido;
import com.sena.elim.model.MetodoPago;
import com.sena.elim.model.Pedido;
import com.sena.elim.model.Producto;
import com.sena.elim.model.ProductoTalla;
import com.sena.elim.repository.DetallePedidoRepository;
import com.sena.elim.repository.MetodoPagoRepository;
import com.sena.elim.repository.PedidoRepository;
import com.sena.elim.repository.ProductoRepository; // <- 1. Import necesario
import com.sena.elim.repository.ProductoTallaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class PedidoService {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private DetallePedidoRepository detallePedidoRepository;

    @Autowired
    private ProductoTallaRepository productoTallaRepository;

    @Autowired
    private MetodoPagoRepository metodoPagoRepository;

    @Autowired
    private ProductoRepository productoRepository; // <- conexion con el repositorio de productosd

    @Transactional
    public boolean procesarCompra(String documentoUsuario, Integer idMetodoPago, List<Map<String, Object>> carrito) {
        if (carrito == null || carrito.isEmpty()) {
            return false;
        }

        // 1.calcular el total del pedido
        double total = carrito.stream().mapToDouble(i -> (Double) i.get("subtotal")).sum();

        // 2.crear el pedido y con estado predeterminado con pendiente
        Pedido pedido = new Pedido();
        pedido.setDocumentoUsuarioFk(documentoUsuario);
        pedido.setTotalPago(total);
        pedido.setEstadoPedido("Pendiente");

        Optional<MetodoPago> metodoPagoOpt = metodoPagoRepository.findById(idMetodoPago);
        metodoPagoOpt.ifPresent(pedido::setMetodoPago);

        Pedido pedidoGuardado = pedidoRepository.save(pedido);

        // 3.crear el detalle y descontar el stoxk
        for (Map<String, Object> item : carrito) {
            Integer idProd = (Integer) item.get("id");
            Integer cantidad = (Integer) item.get("cantidad");
            Double precio = (Double) item.get("precio");
            String talla = (String) item.get("talla");

            DetallePedido detalle = new DetallePedido();
            detalle.setIdPedidoFk(pedidoGuardado.getIdPedido());
            detalle.setIdProductoFk(idProd);
            detalle.setCantidad(cantidad);
            detalle.setPrecioUnitario(precio);
            detalle.setSubtotal(cantidad * precio);
            detallePedidoRepository.save(detalle);

            // A.descontar de la tbla de tallas
            Optional<ProductoTalla> ptOpt = productoTallaRepository.findByProductoIdProductoAndTalla(idProd, talla);
            if (ptOpt.isPresent()) {
                ProductoTalla pt = ptOpt.get();
                int nuevoStockTalla = Math.max(0, pt.getStock() - cantidad);
                pt.setStock(nuevoStockTalla);
                productoTallaRepository.save(pt);
            }

            // B.desconytar el stock de la tabla de prodcutos
            Optional<Producto> productoOpt = productoRepository.findById(idProd);
            if (productoOpt.isPresent()) {
                Producto producto = productoOpt.get();
                // Si el producto maneja stock general null o numérico, lo protegemos
                int stockActual = producto.getStock() != null ? producto.getStock() : 0;
                int nuevoStockProducto = Math.max(0, stockActual - cantidad);
                producto.setStock(nuevoStockProducto);
                productoRepository.save(producto);
            }
        }

        return true;
    }
}