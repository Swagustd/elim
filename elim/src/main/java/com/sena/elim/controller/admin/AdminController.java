package com.sena.elim.controller.admin;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // regla para la contraseña segura
    private static final String PASSWORD_PATTERN = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&.#_-])[A-Za-z\\d@$!%*?&.#_-]{8,}$";
    private static final Pattern pattern = Pattern.compile(PASSWORD_PATTERN);

    // regla del telefono
    private static final String PHONE_PATTERN = "^3\\d{9}$";
    private static final Pattern phonePattern = Pattern.compile(PHONE_PATTERN);

    //mapedo de vistas

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        @SuppressWarnings("unchecked")
        Map<String, Object> usuario = (Map<String, Object>) session.getAttribute("usuario");

        Integer rolUsuario = null;
        if (usuario != null && usuario.get("id_rol_fk") != null) {
            rolUsuario = Integer.parseInt(usuario.get("id_rol_fk").toString());
        }

        if (rolUsuario == null || rolUsuario != 1) {
            return "redirect:/";
        }

        Integer totalProductos = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM productos", Integer.class);
        Integer stockBajo = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM productos WHERE stock < 6", Integer.class);
        Integer totalVentas = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM pedidos", Integer.class);
        Double ingresosTotales = jdbcTemplate.queryForObject("SELECT COALESCE(SUM(total_pago), 0) FROM pedidos", Double.class);

        String nombreAdmin = "Administrador";
        if (usuario.get("nombres") != null) {
            nombreAdmin = usuario.get("nombres").toString().trim().split(" ")[0];
        }

        String sqlTabla = "SELECT " +
                "p.id_producto, p.id_producto AS idProducto, " +
                "p.nombre_producto, p.nombre_producto AS nombreProducto, " +
                "p.descripcion, p.precio, p.stock, " +
                "p.imagen_url, p.imagen_url AS imagenUrl, " +
                "p.estado, " +
                "c.nombre_categoria, c.nombre_categoria AS nombreCategoria " +
                "FROM productos p " +
                "LEFT JOIN categorias c ON p.id_categoria_fk = c.id_categoria " +
                "ORDER BY p.id_producto DESC";
        List<Map<String, Object>> productos = jdbcTemplate.queryForList(sqlTabla);

        model.addAttribute("totalProductos", totalProductos != null ? totalProductos : 0);
        model.addAttribute("stockBajo", stockBajo != null ? stockBajo : 0);
        model.addAttribute("totalVentas", totalVentas != null ? totalVentas : 0);
        model.addAttribute("ingresosTotales", ingresosTotales != null ? ingresosTotales.longValue() : 0);
        model.addAttribute("primerNombre", nombreAdmin);
        model.addAttribute("productos", productos);

        return "admin/dashboard";
    }

    @GetMapping("/agregar-pijama")
    public String mostrarAgregarPijama(HttpSession session,
                                       @RequestParam(value = "error", required = false) String error,
                                       @RequestParam(value = "exito", required = false) String exito,
                                       Model model) {

        @SuppressWarnings("unchecked")
        Map<String, Object> usuario = (Map<String, Object>) session.getAttribute("usuario");
        if (usuario == null || usuario.get("id_rol_fk") == null || Integer.parseInt(usuario.get("id_rol_fk").toString()) != 1) {
            return "redirect:/";
        }

        List<Map<String, Object>> categorias = jdbcTemplate.queryForList("SELECT id_categoria, nombre_categoria FROM categorias ORDER BY nombre_categoria");

        model.addAttribute("categorias", categorias);
        model.addAttribute("error", error);
        model.addAttribute("exito", exito);

        return "admin/agregar_pijama";
    }

    @PostMapping("/agregar-pijama")
    public String guardarPijama(@RequestParam("nombre_producto") String nombreProducto,
                                @RequestParam(value = "descripcion", required = false) String descripcion,
                                @RequestParam("precio") Double precio,
                                @RequestParam(value = "stock", required = false, defaultValue = "0") Integer stockInput,
                                @RequestParam Map<String, String> allParams,
                                @RequestParam("id_categoria_fk") Integer idCategoria,
                                @RequestParam(value = "estado", defaultValue = "Disponible") String estado,
                                @RequestParam(value = "imagen", required = false) MultipartFile imagen,
                                HttpSession session) {

        @SuppressWarnings("unchecked")
        Map<String, Object> usuario = (Map<String, Object>) session.getAttribute("usuario");
        if (usuario == null || usuario.get("id_rol_fk") == null || Integer.parseInt(usuario.get("id_rol_fk").toString()) != 1) {
            return "redirect:/";
        }

        String nombreImagen = null;

        if (imagen != null && !imagen.isEmpty()) {
            try {
                nombreImagen = System.currentTimeMillis() + "_" + imagen.getOriginalFilename();
                Path rutaDirectorio = Paths.get("src/main/resources/static/images").toAbsolutePath();
                if (!Files.exists(rutaDirectorio)) {
                    Files.createDirectories(rutaDirectorio);
                }

                Path path = rutaDirectorio.resolve(nombreImagen);
                Files.write(path, imagen.getBytes());
            } catch (IOException e) {
                return "redirect:/admin/agregar-pijama?error=Error+al+subir+la+imagen";
            }
        }

        // extraer unicamente las tallas enviadas
        Map<String, Integer> mapTallas = new HashMap<>();
        for (Map.Entry<String, String> entry : allParams.entrySet()) {
            if (entry.getKey().startsWith("tallas[")) {
                String nombreTalla = entry.getKey().substring(7, entry.getKey().length() - 1);
                try {
                    int cantidad = Integer.parseInt(entry.getValue());
                    if (cantidad >= 0) {
                        mapTallas.put(nombreTalla, cantidad);
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }

        int stockCalculado = mapTallas.values().stream().mapToInt(Integer::intValue).sum();
        int stockTotalFinal = stockCalculado > 0 ? stockCalculado : stockInput;

        try {
            String sqlProducto = "INSERT INTO productos (nombre_producto, descripcion, precio, stock, imagen_url, id_categoria_fk, estado) VALUES (?, ?, ?, ?, ?, ?, ?)";
            KeyHolder keyHolder = new GeneratedKeyHolder();

            final String finalNombreImagen = nombreImagen;
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sqlProducto, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, nombreProducto);
                ps.setString(2, descripcion);
                ps.setDouble(3, precio);
                ps.setInt(4, stockTotalFinal);
                ps.setString(5, finalNombreImagen);
                ps.setInt(6, idCategoria);
                ps.setString(7, estado);
                return ps;
            }, keyHolder);

            Number idGenerado = keyHolder.getKey();

            // Insertar desglosado en producto_tallas para cualquier categoría
            if (idGenerado != null && !mapTallas.isEmpty()) {
                int idProductoNew = idGenerado.intValue();
                String sqlTalla = "INSERT INTO producto_tallas (id_producto_fk, talla, stock) VALUES (?, ?, ?)";

                for (Map.Entry<String, Integer> entry : mapTallas.entrySet()) {
                    jdbcTemplate.update(sqlTalla, idProductoNew, entry.getKey(), entry.getValue());
                }
            }

            return "redirect:/admin/dashboard";
        } catch (Exception e) {
            return "redirect:/admin/agregar-pijama?error=Error+al+guardar+el+producto+y+sus+tallas";
        }
    }

    @GetMapping("/editar-pijama")
    public String mostrarEditarPijama(@RequestParam("id") Integer idProducto,
                                      @RequestParam(value = "error", required = false) String error,
                                      HttpSession session,
                                      Model model) {

        @SuppressWarnings("unchecked")
        Map<String, Object> usuario = (Map<String, Object>) session.getAttribute("usuario");
        if (usuario == null || usuario.get("id_rol_fk") == null || Integer.parseInt(usuario.get("id_rol_fk").toString()) != 1) {
            return "redirect:/";
        }

        String sqlProducto = "SELECT p.*, c.nombre_categoria FROM productos p " +
                "LEFT JOIN categorias c ON p.id_categoria_fk = c.id_categoria " +
                "WHERE p.id_producto = ?";
        List<Map<String, Object>> productos = jdbcTemplate.queryForList(sqlProducto, idProducto);

        if (productos.isEmpty()) {
            return "redirect:/admin/dashboard";
        }

        // consultar las tallas de esa pijama
        String sqlTallas = "SELECT talla, stock FROM producto_tallas WHERE id_producto_fk = ?";
        List<Map<String, Object>> listaTallasBD = jdbcTemplate.queryForList(sqlTallas, idProducto);

        // mapeo de formato de tallas
        Map<String, Integer> tallasActuales = new HashMap<>();
        for (Map<String, Object> t : listaTallasBD) {
            String nombreTalla = (String) t.get("talla");
            Integer stockTalla = ((Number) t.get("stock")).intValue();
            tallasActuales.put(nombreTalla, stockTalla);
        }

        List<Map<String, Object>> categorias = jdbcTemplate.queryForList("SELECT id_categoria, nombre_categoria FROM categorias ORDER BY nombre_categoria");

        model.addAttribute("producto", productos.get(0));
        model.addAttribute("tallasActuales", tallasActuales);
        model.addAttribute("categorias", categorias);
        model.addAttribute("error", error);

        return "admin/editar_pijama";
    }

    @PostMapping("/editar-pijama")
    public String actualizarPijama(@RequestParam("id_producto") Integer idProducto,
                                   @RequestParam("nombre_producto") String nombreProducto,
                                   @RequestParam(value = "descripcion", required = false) String descripcion,
                                   @RequestParam("precio") Double precio,
                                   @RequestParam(value = "stock", required = false, defaultValue = "0") Integer stockInput,
                                   @RequestParam Map<String, String> allParams,
                                   @RequestParam("id_categoria_fk") Integer idCategoria,
                                   @RequestParam(value = "estado", defaultValue = "Disponible") String estado,
                                   @RequestParam(value = "imagen", required = false) MultipartFile imagen,
                                   HttpSession session) {

        @SuppressWarnings("unchecked")
        Map<String, Object> usuario = (Map<String, Object>) session.getAttribute("usuario");
        if (usuario == null || usuario.get("id_rol_fk") == null || Integer.parseInt(usuario.get("id_rol_fk").toString()) != 1) {
            return "redirect:/";
        }

        // extraer individualemte la talla
        Map<String, Integer> mapTallas = new HashMap<>();
        for (Map.Entry<String, String> entry : allParams.entrySet()) {
            if (entry.getKey().startsWith("tallas[")) {
                String nombreTalla = entry.getKey().substring(7, entry.getKey().length() - 1);
                try {
                    int cantidad = Integer.parseInt(entry.getValue());
                    if (cantidad >= 0) {
                        mapTallas.put(nombreTalla, cantidad);
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }

        int stockCalculado = mapTallas.values().stream().mapToInt(Integer::intValue).sum();
        int stockTotalFinal = stockCalculado > 0 ? stockCalculado : stockInput;

        try {
            String nombreImagen = null;
            if (imagen != null && !imagen.isEmpty()) {
                nombreImagen = System.currentTimeMillis() + "_" + imagen.getOriginalFilename();
                Path rutaDirectorio = Paths.get("src/main/resources/static/images").toAbsolutePath();
                if (!Files.exists(rutaDirectorio)) {
                    Files.createDirectories(rutaDirectorio);
                }
                Path path = rutaDirectorio.resolve(nombreImagen);
                Files.write(path, imagen.getBytes());
            }

            // actualizar datos del prodcuto
            if (nombreImagen != null) {
                String sql = "UPDATE productos SET nombre_producto = ?, descripcion = ?, precio = ?, stock = ?, imagen_url = ?, id_categoria_fk = ?, estado = ? WHERE id_producto = ?";
                jdbcTemplate.update(sql, nombreProducto, descripcion, precio, stockTotalFinal, nombreImagen, idCategoria, estado, idProducto);
            } else {
                String sql = "UPDATE productos SET nombre_producto = ?, descripcion = ?, precio = ?, stock = ?, id_categoria_fk = ?, estado = ? WHERE id_producto = ?";
                jdbcTemplate.update(sql, nombreProducto, descripcion, precio, stockTotalFinal, idCategoria, estado, idProducto);
            }

            // Reemplazar únicamente el stock de las tallas modificadas en la base de datos
            if (!mapTallas.isEmpty()) {
                jdbcTemplate.update("DELETE FROM producto_tallas WHERE id_producto_fk = ?", idProducto);
                String sqlTalla = "INSERT INTO producto_tallas (id_producto_fk, talla, stock) VALUES (?, ?, ?)";
                for (Map.Entry<String, Integer> entry : mapTallas.entrySet()) {
                    jdbcTemplate.update(sqlTalla, idProducto, entry.getKey(), entry.getValue());
                }
            }

            return "redirect:/admin/dashboard";
        } catch (Exception e) {
            return "redirect:/admin/editar-pijama?id=" + idProducto + "&error=Error+al+actualizar+el+producto+y+sus+tallas";
        }
    }

    @GetMapping("/eliminar-pijama/{id}")
    public String eliminarPijama(@PathVariable("id") Integer idProducto, HttpSession session) {

        @SuppressWarnings("unchecked")
        Map<String, Object> usuario = (Map<String, Object>) session.getAttribute("usuario");
        if (usuario == null || usuario.get("id_rol_fk") == null || Integer.parseInt(usuario.get("id_rol_fk").toString()) != 1) {
            return "redirect:/";
        }

        try {
            String sql = "DELETE FROM productos WHERE id_producto = ?";
            jdbcTemplate.update(sql, idProducto);
        } catch (Exception e) {
            // Manejar si el producto tiene llaves foráneas asociadas
        }

        return "redirect:/admin/dashboard";
    }

    // parte usuarios

    @GetMapping("/editar-usuario")
    public String mostrarEditarUsuario(@RequestParam(value = "documento", required = false, defaultValue = "") String documento,
                                       @RequestParam(value = "error", required = false) String error,
                                       @RequestParam(value = "exito", required = false) String exito,
                                       HttpSession session,
                                       Model model) {

        @SuppressWarnings("unchecked")
        Map<String, Object> usuarioSesion = (Map<String, Object>) session.getAttribute("usuario");
        if (usuarioSesion == null || usuarioSesion.get("id_rol_fk") == null || Integer.parseInt(usuarioSesion.get("id_rol_fk").toString()) != 1) {
            return "redirect:/";
        }

        if (documento.trim().isEmpty()) {
            model.addAttribute("error", "No se indicó el usuario a editar.");
            return "admin/editar_usuario";
        }

        try {
            String sql = "SELECT documento, tipo_documento, nombres, apellidos, correo, telefono, direccion, id_rol_fk " +
                    "FROM usuarios WHERE documento = ?";
            List<Map<String, Object>> usuarios = jdbcTemplate.queryForList(sql, documento);

            if (usuarios.isEmpty()) {
                model.addAttribute("error", "No se encontró el usuario solicitado.");
            } else {
                model.addAttribute("usuario", usuarios.get(0));
            }
        } catch (Exception e) {
            model.addAttribute("error", "No se pudo cargar el usuario.");
        }

        model.addAttribute("error", error);
        model.addAttribute("exito", exito);

        return "admin/editar_usuario";
    }

    @PostMapping("/editar-usuario")
    public String actualizarUsuario(@RequestParam("documento") String documento,
                                    @RequestParam("nombres") String nombres,
                                    @RequestParam("apellidos") String apellidos,
                                    @RequestParam("correo") String correo,
                                    @RequestParam(value = "telefono", required = false) String telefono,
                                    @RequestParam("direccion") String direccion,
                                    @RequestParam("id_rol_fk") Integer idRolFk,
                                    HttpSession session) {

        @SuppressWarnings("unchecked")
        Map<String, Object> usuarioSesion = (Map<String, Object>) session.getAttribute("usuario");
        if (usuarioSesion == null || usuarioSesion.get("id_rol_fk") == null || Integer.parseInt(usuarioSesion.get("id_rol_fk").toString()) != 1) {
            return "redirect:/";
        }

        String correoLimpio = correo.trim().toLowerCase();
        String telefonoLimpio = telefono != null ? telefono.trim() : "";

        // validar correo en .com
        if (!correoLimpio.endsWith(".com")) {
            return "redirect:/admin/editar-usuario?documento=" + documento + "&error=El+correo+debe+terminar+obligatoriamente+en+.com";
        }

        // validar el formato del telefono
        if (!telefonoLimpio.isEmpty() && !phonePattern.matcher(telefonoLimpio).matches()) {
            return "redirect:/admin/editar-usuario?documento=" + documento + "&error=El+telefono+debe+tener+exactamente+10+digitos+y+comenzar+por+3";
        }

        try {
            String sql = "UPDATE usuarios SET nombres = ?, apellidos = ?, correo = ?, telefono = ?, direccion = ?, id_rol_fk = ? WHERE documento = ?";
            jdbcTemplate.update(sql, nombres, apellidos, correoLimpio, telefonoLimpio, direccion, idRolFk, documento);

            return "redirect:/admin/usuarios?success=Usuario+actualizado+correctamente";
        } catch (Exception e) {
            return "redirect:/admin/editar-usuario?documento=" + documento + "&error=Error+al+actualizar+el+usuario";
        }
    }

    @GetMapping("/usuarios")
    public String listarUsuarios(@RequestParam(value = "success", required = false) String success,
                                 @RequestParam(value = "error", required = false) String error,
                                 HttpSession session,
                                 Model model) {

        @SuppressWarnings("unchecked")
        Map<String, Object> usuarioSesion = (Map<String, Object>) session.getAttribute("usuario");
        if (usuarioSesion == null || usuarioSesion.get("id_rol_fk") == null || Integer.parseInt(usuarioSesion.get("id_rol_fk").toString()) != 1) {
            return "redirect:/";
        }

        List<Map<String, Object>> usuarios;
        List<Map<String, Object>> roles;

        try {
            String sqlUsuarios = "SELECT u.documento, u.tipo_documento, u.nombres, u.apellidos, u.correo, u.telefono, u.direccion, u.id_rol_fk, r.nombre_rol " +
                    "FROM usuarios u " +
                    "LEFT JOIN roles r ON r.id_rol = u.id_rol_fk " +
                    "ORDER BY u.nombres ASC";
            usuarios = jdbcTemplate.queryForList(sqlUsuarios);

            String sqlRoles = "SELECT id_rol, nombre_rol FROM roles ORDER BY id_rol ASC";
            roles = jdbcTemplate.queryForList(sqlRoles);
        } catch (Exception e) {
            usuarios = List.of();
            roles = List.of();
        }

        model.addAttribute("usuarios", usuarios);
        model.addAttribute("roles", roles);
        model.addAttribute("success", success);
        model.addAttribute("error", error);

        return "admin/usuarios";
    }

    @PostMapping("/cambiar-rol-usuario")
    public String cambiarRolUsuario(@RequestParam("documento") String documento,
                                    @RequestParam("id_rol_fk") Integer idRolFk,
                                    HttpSession session) {

        @SuppressWarnings("unchecked")
        Map<String, Object> usuarioSesion = (Map<String, Object>) session.getAttribute("usuario");
        if (usuarioSesion == null || usuarioSesion.get("id_rol_fk") == null || Integer.parseInt(usuarioSesion.get("id_rol_fk").toString()) != 1) {
            return "redirect:/";
        }

        try {
            String sql = "UPDATE usuarios SET id_rol_fk = ? WHERE documento = ?";
            jdbcTemplate.update(sql, idRolFk, documento);

            return "redirect:/admin/usuarios?success=Rol+actualizado+correctamente";
        } catch (Exception e) {
            return "redirect:/admin/usuarios?error=Error+al+actualizar+el+rol";
        }
    }

    @GetMapping("/eliminar-usuario/{documento}")
    public String eliminarUsuario(@PathVariable("documento") String documento, HttpSession session) {

        @SuppressWarnings("unchecked")
        Map<String, Object> usuarioSesion = (Map<String, Object>) session.getAttribute("usuario");
        if (usuarioSesion == null || usuarioSesion.get("id_rol_fk") == null || Integer.parseInt(usuarioSesion.get("id_rol_fk").toString()) != 1) {
            return "redirect:/";
        }

        // Evitar que el administrador actual se elimine a sí mismo
        String docActual = usuarioSesion.get("documento") != null ? usuarioSesion.get("documento").toString() : "";
        if (docActual.equals(documento)) {
            return "redirect:/admin/usuarios?error=No+puedes+eliminar+tu+propia+cuenta+de+administrador";
        }

        try {
            String sql = "DELETE FROM usuarios WHERE documento = ?";
            jdbcTemplate.update(sql, documento);
            return "redirect:/admin/usuarios?success=Usuario+eliminado+correctamente";
        } catch (Exception e) {
            return "redirect:/admin/usuarios?error=No+se+pudo+eliminar+el+usuario+debido+a+registros+asociados";
        }
    }

    // =registro de usuarios

    @GetMapping("/registrar-usuario")
    public String mostrarRegistrarUsuario(@RequestParam(value = "error", required = false) String error,
                                          HttpSession session,
                                          Model model) {

        @SuppressWarnings("unchecked")
        Map<String, Object> usuarioSesion = (Map<String, Object>) session.getAttribute("usuario");
        if (usuarioSesion == null || usuarioSesion.get("id_rol_fk") == null || Integer.parseInt(usuarioSesion.get("id_rol_fk").toString()) != 1) {
            return "redirect:/";
        }

        try {
            String sqlRoles = "SELECT id_rol, nombre_rol FROM roles ORDER BY id_rol ASC";
            List<Map<String, Object>> roles = jdbcTemplate.queryForList(sqlRoles);
            model.addAttribute("roles", roles);
        } catch (Exception e) {
            model.addAttribute("roles", List.of());
        }

        model.addAttribute("error", error);
        return "admin/registrar_usuario";
    }



    @PostMapping("/registrar-usuario")
    public String guardarUsuario(@RequestParam("documento") String documento,
                                 @RequestParam("tipo_documento") String tipoDocumento,
                                 @RequestParam("nombres") String nombres,
                                 @RequestParam("apellidos") String apellidos,
                                 @RequestParam("correo") String correo,
                                 @RequestParam("password") String password,
                                 @RequestParam("telefono") String telefono,
                                 @RequestParam("direccion") String direccion,
                                 @RequestParam("id_rol_fk") Integer idRolFk,
                                 HttpSession session) {

        @SuppressWarnings("unchecked")
        Map<String, Object> usuarioSesion = (Map<String, Object>) session.getAttribute("usuario");
        if (usuarioSesion == null || usuarioSesion.get("id_rol_fk") == null || Integer.parseInt(usuarioSesion.get("id_rol_fk").toString()) != 1) {
            return "redirect:/";
        }

        // validar que ningun campo quede en blanco
        if (documento.trim().isEmpty() || tipoDocumento.trim().isEmpty() || nombres.trim().isEmpty() ||
                apellidos.trim().isEmpty() || correo.trim().isEmpty() || password.trim().isEmpty() ||
                telefono.trim().isEmpty() || direccion.trim().isEmpty() || idRolFk == null) {
            return "redirect:/admin/registrar-usuario?error=Todos+los+campos+son+obligatorios";
        }

        String correoLimpio = correo.trim().toLowerCase();
        String telefonoLimpio = telefono.trim();

        try {
            // 1. Validar correo termina en .com
            if (!correoLimpio.endsWith(".com")) {
                return "redirect:/admin/registrar-usuario?error=El+correo+debe+terminar+obligatoriamente+en+.com";
            }

            // 2. Validar teléfono (10 dígitos e inicia en 3)
            if (!phonePattern.matcher(telefonoLimpio).matches()) {
                return "redirect:/admin/registrar-usuario?error=El+telefono+debe+tener+exactamente+10+digitos+y+comenzar+por+3";
            }

            // 3. Validar si la contraseña cumple con las políticas de seguridad
            if (!pattern.matcher(password).matches()) {
                return "redirect:/admin/registrar-usuario?error=La+contrasena+debe+tener+al+menos+8+caracteres,+una+mayuscula,+una+minuscula,+un+numero+y+un+caracter+especial";
            }

            // 4. Verificar existencia previa
            String sqlCheck = "SELECT COUNT(*) FROM usuarios WHERE documento = ? OR correo = ?";
            Integer existe = jdbcTemplate.queryForObject(sqlCheck, Integer.class, documento.trim(), correoLimpio);

            if (existe != null && existe > 0) {
                return "redirect:/admin/registrar-usuario?error=El+documento+o+el+correo+ya+se+encuentran+registrados";
            }

            // 5. Encriptar contraseña con BCrypt
            String passwordEncriptada = passwordEncoder.encode(password);

            String sqlInsert = "INSERT INTO usuarios (documento, tipo_documento, nombres, apellidos, correo, password, telefono, direccion, id_rol_fk) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

            jdbcTemplate.update(sqlInsert, documento.trim(), tipoDocumento.trim(), nombres.trim(), apellidos.trim(), correoLimpio, passwordEncriptada, telefonoLimpio, direccion.trim(), idRolFk);

            return "redirect:/admin/usuarios?success=Usuario+registrado+correctamente";
        } catch (Exception e) {
            return "redirect:/admin/registrar-usuario?error=Error+al+registrar+el+usuario";
        }
    }
}