package com.sena.elim.controller.principal;

import com.sena.elim.service.DevolucionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class DevolucionController {

    @Autowired
    private DevolucionService devolucionService;

    @PostMapping("/cliente/solicitar-devolucion")
    public String solicitarDevolucion(@RequestParam("idPedido") Integer idPedido,
                                      @RequestParam("motivo") String motivo) {
        boolean exito = devolucionService.solicitarDevolucion(idPedido, motivo);

        if (exito) {
            return "redirect:/mis-pedidos?success=Solicitud+de+devolucion+enviada+correctamente";
        } else {
            return "redirect:/mis-pedidos?error=No+se+pudo+procesar+la+solicitud";
        }
    }
}