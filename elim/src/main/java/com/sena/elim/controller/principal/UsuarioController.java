package com.sena.elim.controller.principal;

import com.sena.elim.model.Usuario;
import com.sena.elim.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

@Controller
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Rreglas de la contraseña segura
    private static final String PASSWORD_PATTERN = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&.#_-])[A-Za-z\\d@$!%*?&.#_-]{8,}$";
    private static final Pattern pattern = Pattern.compile(PASSWORD_PATTERN);

    @GetMapping("/registro")
    public String mostrarRegistro(Authentication authentication,
                                  HttpSession session,
                                  @RequestParam(value = "error", required = false) String error,
                                  @RequestParam(value = "exito", required = false) String exito,
                                  Model model) {

        boolean haySesion = (authentication != null && authentication.isAuthenticated() && !authentication.getPrincipal().equals("anonymousUser"))
                || session.getAttribute("usuario") != null;

        if (haySesion) {
            return "redirect:/";
        }

        model.addAttribute("error", error);
        model.addAttribute("exito", exito);

        return "principal/registro";
    }

    @PostMapping("/registrar")
    public String registrarUsuario(@RequestParam("tipo_documento") String tipoDocumento,
                                   @RequestParam("documento") String documento,
                                   @RequestParam("nombres") String nombres,
                                   @RequestParam("apellidos") String apellidos,
                                   @RequestParam("correo") String correo,
                                   @RequestParam("telefono") String telefono,
                                   @RequestParam("direccion") String direccion,
                                   @RequestParam("password") String password,
                                   @RequestParam("confirm_password") String confirmPassword) {

        String docLimpio = documento.trim();
        String correoLimpio = correo.trim().toLowerCase();

        // 1.validar el correo con .com
        if (!correoLimpio.endsWith(".com")) {
            return "redirect:/registro?error=" + encodeParam("El correo electrónico debe terminar obligatoriamente en '.com'.");
        }

        // 2.coincidencias de contraseñas
        if (!password.equals(confirmPassword)) {
            return "redirect:/registro?error=" + encodeParam("Las contraseñas no coinciden.");
        }

        // 3.validar password swgura
        if (!pattern.matcher(password).matches()) {
            return "redirect:/registro?error=" + encodeParam("La contraseña debe tener al menos 8 caracteres, incluir una mayúscula, una minúscula, un número y un carácter especial (@$!%*?&.#_-).");
        }

        // 4.validar si el usuario no existe
        if (usuarioRepository.existsByDocumentoOrCorreo(docLimpio, correoLimpio)) {
            return "redirect:/registro?error=" + encodeParam("El documento o correo ya se encuentran registrados.");
        }

        try {
            Usuario nuevoUsuario = new Usuario();
            nuevoUsuario.setDocumento(docLimpio);
            nuevoUsuario.setTipoDocumento(tipoDocumento);
            nuevoUsuario.setNombres(nombres.trim());
            nuevoUsuario.setApellidos(apellidos.trim());
            nuevoUsuario.setCorreo(correoLimpio);
            nuevoUsuario.setTelefono(telefono.trim());
            nuevoUsuario.setDireccion(direccion.trim());

            // cifrado de la passwordd
            nuevoUsuario.setPassword(passwordEncoder.encode(password));
            nuevoUsuario.setIdRolFk(2); // Rol Cliente

            usuarioRepository.save(nuevoUsuario);

            return "redirect:/login?exito=" + encodeParam("Usuario registrado correctamente. ¡Ya puedes iniciar sesión!");

        } catch (Exception e) {
            return "redirect:/registro?error=" + encodeParam("Error interno al registrar el usuario.");
        }
    }

    @GetMapping("/login")
    public String mostrarLogin(Authentication authentication,
                               HttpSession session,
                               @RequestParam(value = "error", required = false) String error,
                               @RequestParam(value = "exito", required = false) String exito,
                               Model model) {

        boolean haySesion = (authentication != null && authentication.isAuthenticated() && !authentication.getPrincipal().equals("anonymousUser"))
                || session.getAttribute("usuario") != null;

        if (haySesion) {
            return "redirect:/";
        }

        model.addAttribute("error", error);
        model.addAttribute("exito", exito);

        return "principal/login";
    }

    @PostMapping("/login")
    public String procesarLogin(@RequestParam(value = "correo_login", defaultValue = "") String correo,
                                @RequestParam(value = "clave_login", defaultValue = "") String clave,
                                HttpSession session) {

        String correoLimpio = correo.trim().toLowerCase();
        String claveLimpia = clave.trim();

        if (correoLimpio.isEmpty() || claveLimpia.isEmpty()) {
            return "redirect:/login?error=" + encodeParam("Por favor completa todos los campos");
        }

        // validar el dom .com en login
        if (!correoLimpio.endsWith(".com")) {
            return "redirect:/login?error=" + encodeParam("El correo electrónico ingresado debe terminar en '.com'.");
        }

        Optional<Usuario> usuarioOpt = usuarioRepository.findByCorreo(correoLimpio);

        // 1.validar que el correo exista en la BD
        if (usuarioOpt.isEmpty()) {
            return "redirect:/login?error=" + encodeParam("El correo ingresado no se encuentra registrado en el sistema.");
        }

        Usuario usuario = usuarioOpt.get();

        // 2. Validar contraseña (soporta BCrypt y texto plano en migración)
        boolean esClaveValida = claveLimpia.equals(usuario.getPassword()) ||
                passwordEncoder.matches(claveLimpia, usuario.getPassword());

        if (!esClaveValida) {
            return "redirect:/login?error=" + encodeParam("La contraseña ingresada es incorrecta.");
        }

        // 3.creacion e inicio de sesion en HTTP
        Map<String, Object> datosSesion = new HashMap<>();
        datosSesion.put("documento", usuario.getDocumento());
        datosSesion.put("correo", usuario.getCorreo());
        datosSesion.put("nombres", usuario.getNombres());
        datosSesion.put("id_rol_fk", usuario.getIdRolFk());

        session.setAttribute("usuario", datosSesion);

        if (Integer.valueOf(1).equals(usuario.getIdRolFk())) {
            return "redirect:/admin/dashboard";
        } else {
            return "redirect:/";
        }
    }

    @GetMapping("/logout")
    public String cerrarSesion(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    private String encodeParam(String valor) {
        return URLEncoder.encode(valor, StandardCharsets.UTF_8);
    }
}