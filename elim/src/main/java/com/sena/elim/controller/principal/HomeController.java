package com.sena.elim.controller.principal;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    // Mmaneja la ruta de raiz
    @GetMapping({"/", "/index"})
    public String index(HttpSession session, Model model) {

        // si hay sesion activa, se vea la info del usuario visible
        if (session.getAttribute("usuario") != null) {
            model.addAttribute("usuarioSesion", session.getAttribute("usuario"));
        }

      //retorno al index
        return "index";
    }
}