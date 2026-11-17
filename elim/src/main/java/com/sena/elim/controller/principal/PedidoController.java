package com.sena.elim.controller.principal;

import com.sena.elim.model.Pedido;
import com.sena.elim.repository.PedidoRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Controller
public class PedidoController {

    @Autowired
    private PedidoRepository pedidoRepository;

    @GetMapping("/mis-pedidos")
    public String verMisPedidos(HttpSession session, Model model) {
        // validar que el usuario este loggeado
        @SuppressWarnings("unchecked")
        Map<String, Object> usuarioSesion = (Map<String, Object>) session.getAttribute("usuario");
        if (usuarioSesion == null) {
            return "redirect:/login";
        }

        // obtener el documento desde la sesion iniciada
        String documentoUsuario = (String) usuarioSesion.get("documento");

        // buscar los pedidos que pertenezcan a este usuario
        List<Pedido> misPedidos = pedidoRepository.findByDocumentoUsuarioFk(documentoUsuario);

        model.addAttribute("pedidos", misPedidos);

        // retornar a la vista de pedidos
        return "principal/mis-pedidos";
    }

    // Método para cancelar un pedido del cliente
    @PostMapping("/pedidos/cancelar")
    public String cancelarPedido(@RequestParam("id_pedido") Integer idPedido,
                                 HttpSession session) {
        // Validar que el usuario esté logueado
        @SuppressWarnings("unchecked")
        Map<String, Object> usuarioSesion = (Map<String, Object>) session.getAttribute("usuario");
        if (usuarioSesion == null) {
            return "redirect:/login";
        }

        // Buscar el pedido en la base de datos de forma segura
        Pedido pedido = pedidoRepository.findById(idPedido).orElse(null);

        if (pedido != null) {
            String documentoUsuario = (String) usuarioSesion.get("documento");

            // Validar que el pedido pertenezca al usuario y su estado sea cancelable
            if (pedido.getDocumentoUsuarioFk() != null && pedido.getDocumentoUsuarioFk().equals(documentoUsuario)) {
                if ("Pendiente".equals(pedido.getEstadoPedido()) || "Pagado".equals(pedido.getEstadoPedido())) {
                    pedido.setEstadoPedido("Cancelado");
                    pedidoRepository.save(pedido);
                    return "redirect:/mis-pedidos?success=El_pedido_ha_sido_cancelado_correctamente";
                }
            }
        }

        return "redirect:/mis-pedidos?error=No_se_pudo_cancelar_el_pedido";
    }
}