package com.sena.elim.controller.principal;

import com.sena.elim.model.Producto;
import com.sena.elim.service.PedidoService;
import com.sena.elim.repository.ProductoRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class CarritoController {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private PedidoService pedidoService; // Servicio transaccional para descontar stock

    // 1.para ver el carrito
    @GetMapping("/carrito")
    public String verCarrito(HttpSession session, Model model) {
        Map<String, Object> usuarioSesion = (Map<String, Object>) session.getAttribute("usuario");
        if (usuarioSesion == null) {
            return "redirect:/login";
        }

        List<Map<String, Object>> carrito = (List<Map<String, Object>>) session.getAttribute("carrito");
        if (carrito == null) {
            carrito = new ArrayList<>();
        }

        double subtotal = carrito.stream()
                .mapToDouble(item -> (Double) item.get("subtotal"))
                .sum();

        model.addAttribute("items", carrito);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("contadorCarrito", carrito.size());

        return "principal/carrito";
    }

    // 2.agregar al carrito con la talla
    @PostMapping("/carrito/agregar")
    public String agregarAlCarrito(@RequestParam("id_producto") Integer idProducto,
                                   @RequestParam("talla") String talla,
                                   @RequestParam(value = "redirect", defaultValue = "/") String redirectUrl,
                                   HttpSession session) {

        Optional<Producto> productoOpt = productoRepository.findById(idProducto);
        if (productoOpt.isEmpty()) {
            return "redirect:" + redirectUrl + "?error=Producto+no+encontrado";
        }

        Producto producto = productoOpt.get();

        List<Map<String, Object>> carrito = (List<Map<String, Object>>) session.getAttribute("carrito");
        if (carrito == null) {
            carrito = new ArrayList<>();
        }

        // condicional por si ya esta ese prodcuto en el carrito con la misma talla
        boolean encontrado = false;
        for (Map<String, Object> item : carrito) {
            if (item.get("id").equals(idProducto) && item.get("talla").equals(talla)) {
                Integer cantidadActual = (Integer) item.get("cantidad");
                int nuevaCantidad = cantidadActual + 1;

                item.put("cantidad", nuevaCantidad);

                double precioUnitario = ((Number) item.get("precio")).doubleValue();
                item.put("subtotal", nuevaCantidad * precioUnitario);

                encontrado = true;
                break;
            }
        }

        // si no esta, agregarlo
        if (!encontrado) {
            Map<String, Object> nuevoItem = new HashMap<>();
            nuevoItem.put("id", producto.getIdProducto());
            nuevoItem.put("nombre", producto.getNombreProducto());
            nuevoItem.put("precio", producto.getPrecio());
            nuevoItem.put("imagen", producto.getImagenUrl());
            nuevoItem.put("talla", talla);
            nuevoItem.put("cantidad", 1);
            nuevoItem.put("subtotal", producto.getPrecio());
            carrito.add(nuevoItem);
        }

        session.setAttribute("carrito", carrito);
        return "redirect:" + redirectUrl;
    }

    // 3.eliminar pijama del carrito
    @PostMapping("/carrito/eliminar")
    public String eliminarDelCarrito(@RequestParam("id_producto") Integer idProducto,
                                     @RequestParam("talla") String talla,
                                     HttpSession session) {
        List<Map<String, Object>> carrito = (List<Map<String, Object>>) session.getAttribute("carrito");
        if (carrito != null) {
            carrito.removeIf(item -> item.get("id").equals(idProducto) && item.get("talla").equals(talla));
            session.setAttribute("carrito", carrito);
        }
        return "redirect:/carrito";
    }

    // 4.finalizar comprqa conndescuento de estock
    @PostMapping("/carrito/procesar-compra")
    public String procesarCompra(@RequestParam("id_metodo_pago") Integer idMetodoPago,
                                 HttpSession session) {
        Map<String, Object> usuarioSesion = (Map<String, Object>) session.getAttribute("usuario");
        if (usuarioSesion == null) {
            return "redirect:/login";
        }

        List<Map<String, Object>> carrito = (List<Map<String, Object>>) session.getAttribute("carrito");
        if (carrito == null || carrito.isEmpty()) {
            return "redirect:/carrito?error=El+carrito+está+vacío";
        }

        String documentoUsuario = (String) usuarioSesion.get("documento");

        // llamamos el servicio
        boolean exito = pedidoService.procesarCompra(documentoUsuario, idMetodoPago, carrito);

        if (!exito) {
            return "redirect:/carrito?error=No+se+pudo+procesar+la+compra";
        }

        // vaciar el carrito cuando se finaliza la compra
        session.removeAttribute("carrito");

        return "redirect:/mis-pedidos?exito=Compra+realizada+con+exito";
    }
}