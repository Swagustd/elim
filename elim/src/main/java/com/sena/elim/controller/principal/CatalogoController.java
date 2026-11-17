package com.sena.elim.controller.principal;

import com.sena.elim.model.Producto;
import com.sena.elim.service.ProductoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/catalogo")
public class CatalogoController {

    @Autowired
    private ProductoService productoService;

    // Mira si hay un usuario logeado para acceder a los catalogos
    private boolean usuarioNoAutenticado(HttpSession session) {
        return session == null || session.getAttribute("usuario") == null;
    }

    // metodo para desplegar las vistas de el catalogo
    private List<Map<String, String>> obtenerCategorias(String categoriaExcluida) {
        List<Map<String, String>> todas = List.of(
                Map.of("nombre_categoria", "Niños", "url", "/catalogo/ninos"),
                Map.of("nombre_categoria", "Damas", "url", "/catalogo/damas"),
                Map.of("nombre_categoria", "Caballeros", "url", "/catalogo/caballeros"),
                Map.of("nombre_categoria", "Bebés", "url", "/catalogo/bebes")
        );

        return todas.stream()
                .filter(c -> !c.get("nombre_categoria").equalsIgnoreCase(categoriaExcluida))
                .toList();
    }

    @GetMapping("/ninos")
    public String catalogoNinos(HttpSession session, Model model) {
        if (usuarioNoAutenticado(session)) {
            return "redirect:/login";
        }

        List<Producto> productos = productoService.obtenerProductosPorCategoria("Niños");
        List<Map<String, String>> categorias = obtenerCategorias("Niños");

        List<?> carrito = (List<?>) session.getAttribute("carrito");
        int contadorCarrito = (carrito != null) ? carrito.size() : 0;

        model.addAttribute("productos", productos);
        model.addAttribute("categorias", categorias);
        model.addAttribute("contadorCarrito", contadorCarrito);

        return "principal/ninos";
    }

    @GetMapping("/damas")
    public String catalogoDamas(HttpSession session, Model model) {
        if (usuarioNoAutenticado(session)) {
            return "redirect:/login";
        }

        List<Producto> productos = productoService.obtenerProductosPorCategoria("Damas");
        List<Map<String, String>> categorias = obtenerCategorias("Damas");

        List<?> carrito = (List<?>) session.getAttribute("carrito");
        int contadorCarrito = (carrito != null) ? carrito.size() : 0;

        model.addAttribute("productos", productos);
        model.addAttribute("categorias", categorias);
        model.addAttribute("contadorCarrito", contadorCarrito);

        return "principal/damas";
    }

    @GetMapping("/caballeros")
    public String catalogoCaballeros(HttpSession session, Model model) {
        if (usuarioNoAutenticado(session)) {
            return "redirect:/login";
        }

        List<Producto> productos = productoService.obtenerProductosPorCategoria("Caballeros");
        List<Map<String, String>> categorias = obtenerCategorias("Caballeros");

        List<?> carrito = (List<?>) session.getAttribute("carrito");
        int contadorCarrito = (carrito != null) ? carrito.size() : 0;

        model.addAttribute("productos", productos);
        model.addAttribute("categorias", categorias);
        model.addAttribute("contadorCarrito", contadorCarrito);

        return "principal/caballeros";
    }

    @GetMapping("/bebes")
    public String catalogoBebes(HttpSession session, Model model) {
        if (usuarioNoAutenticado(session)) {
            return "redirect:/login";
        }

        List<Producto> productos = productoService.obtenerProductosPorCategoria("Bebés");
        List<Map<String, String>> categorias = obtenerCategorias("Bebés");

        List<?> carrito = (List<?>) session.getAttribute("carrito");
        int contadorCarrito = (carrito != null) ? carrito.size() : 0;

        model.addAttribute("productos", productos);
        model.addAttribute("categorias", categorias);
        model.addAttribute("contadorCarrito", contadorCarrito);

        return "principal/bebes";
    }
}