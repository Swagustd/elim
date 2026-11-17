package com.sena.elim.controller.admin;

import com.sena.elim.model.Pedido;
import com.sena.elim.repository.MetodoPagoRepository;
import com.sena.elim.repository.PedidoRepository;
import com.sena.elim.service.EnvioService; // <- 1. Import necesario
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/admin/pedidos")
public class AdminPedidoController {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private MetodoPagoRepository metodoPagoRepository;

    @Autowired
    private EnvioService envioService; // <- 2.conectamos el servicio

    private boolean usuarioNoAutorizado(HttpSession session) {
        Map<String, Object> usuario = (Map<String, Object>) session.getAttribute("usuario");
        return usuario == null;
    }

    // 1.mostrar los pedidos con sus metosdos de pago
    @GetMapping({"", "/"})
    public String listarPedidosAdmin(HttpSession session, Model model) {
        if (usuarioNoAutorizado(session)) {
            return "redirect:/login";
        }

        List<Pedido> pedidos = pedidoRepository.findAll();
        model.addAttribute("pedidos", pedidos);
        model.addAttribute("metodosPago", metodoPagoRepository.findAll());

        return "admin/pedidos";
    }

    // 2.actualizar estado y gestionar el envio
    @PostMapping("/actualizar-estado")
    public String actualizarEstadoPedido(@RequestParam("id_pedido") Integer idPedido,
                                         @RequestParam("estado_pedido") String nuevoEstado,
                                         HttpSession session) {
        if (usuarioNoAutorizado(session)) {
            return "redirect:/login";
        }

        Optional<Pedido> pedidoOpt = pedidoRepository.findById(idPedido);
        if (pedidoOpt.isPresent()) {
            Pedido pedido = pedidoOpt.get();

            // 3.delegamos el servicio
            envioService.procesarCambioEstadoPedido(pedido, nuevoEstado);

            // guardar en la base de datos
            pedidoRepository.save(pedido);
        }

        return "redirect:/admin/pedidos?exito=Estado+actualizado+correctamente";
    }
}