package com.sena.elim.controller.admin;

import com.sena.elim.service.DevolucionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class DevolucionAdminController {

    @Autowired
    private DevolucionService devolucionService;

    @GetMapping("/admin/devoluciones")
    public String listarDevoluciones(Model model) {
        model.addAttribute("devoluciones", devolucionService.listarTodasLasDevoluciones());
        return "admin/devoluciones";
    }

    @PostMapping("/admin/actualizar-devolucion")
    public String actualizarDevolucion(@RequestParam("id_devolucion") Integer idDevolucion,
                                       @RequestParam("estado_devolucion") String estadoDevolucion) {
        devolucionService.actualizarEstadoDevolucion(idDevolucion, estadoDevolucion);
        return "redirect:/admin/devoluciones?success=Estado+de+devolucion+actualizado+correctamente";
    }
}