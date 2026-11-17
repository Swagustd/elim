package com.sena.elim.controller.admin;

import com.sena.elim.model.Envio;
import com.sena.elim.service.EnvioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
public class EnvioAdminController {

    @Autowired
    private EnvioService envioService;

    @GetMapping("/admin/envios")
    public String listarEnvios(Model model, @RequestParam(required = false, defaultValue = "0") Integer pedidoId) {
        model.addAttribute("envios", envioService.listarTodosLosEnvios());
        model.addAttribute("pedidosForForm", envioService.listarPedidosListosParaEnvio());
        model.addAttribute("pedidoId", pedidoId);
        return "admin/envios";
    }

    // metodo para procesar y guardar
    @PostMapping("/admin/crear-envio")
    public String crearEnvio(@RequestParam("id_pedido_fk") Integer idPedidoFk,
                             @RequestParam("empresa_mensajeria") String empresaMensajeria,
                             @RequestParam("numero_guia") String numeroGuia,
                             @RequestParam("estado_envio") String estadoEnvio) {

        Envio nuevoEnvio = new Envio();
        nuevoEnvio.setId_pedido_fk(idPedidoFk);
        nuevoEnvio.setEmpresa_mensajeria(empresaMensajeria);
        nuevoEnvio.setNumero_guia(numeroGuia);
        nuevoEnvio.setEstado_envio(estadoEnvio);
        nuevoEnvio.setFecha_envio(LocalDate.now());

        envioService.guardarEnvio(nuevoEnvio);

        return "redirect:/admin/envios?success=Envio+generado+correctamente";
    }

    // metodo para actualizar envio
    @PostMapping("/admin/actualizar-envio")
    public String actualizarEnvio(@RequestParam("id_envio") Integer idEnvio,
                                  @RequestParam("estado_envio") String estadoEnvio) {

        // Buscamos el envío de forma segura mediante el servicio
        Envio envio = envioService.buscarEnvioPorId(idEnvio);
        if (envio != null) {
            envio.setEstado_envio(estadoEnvio);
            envioService.guardarEnvio(envio);
        }

        return "redirect:/admin/envios?success=Estado+del+envio+actualizado+correctamente";
    }
}