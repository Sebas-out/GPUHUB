package com.GPUHUB.GPUHUB.controlador.usuario;

import com.GPUHUB.GPUHUB.modelo.Usuario;
import com.GPUHUB.GPUHUB.servicio.UsuarioServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RecuperarControlador {

    @Autowired
    private UsuarioServicio usuarioServicio;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/recuperar")
    public String mostrarFormularioRecuperar() {
        return "recuperar";
    }

    @PostMapping("/recuperar")
    public String procesarRecuperacion(
            @RequestParam("correo") String correo,
            @RequestParam("primoFavorito") String primoFavorito,
            @RequestParam("animalFavorito") String animalFavorito,
            @RequestParam("lugarFavorito") String lugarFavorito,
            @RequestParam("nuevaContrasena") String nuevaContrasena,
            @RequestParam("confirmarContrasena") String confirmarContrasena,
            Model model
    ) {
        try {
            Usuario usuario = usuarioServicio.buscarPorCorreo(correo);
            if (usuario == null) {
                model.addAttribute("error", "No existe una cuenta con ese correo.");
                return "recuperar";
            }
            if (!usuario.getPrimoFavorito().equalsIgnoreCase(primoFavorito.trim()) ||
                !usuario.getAnimalFavorito().equalsIgnoreCase(animalFavorito.trim()) ||
                !usuario.getLugarFavorito().equalsIgnoreCase(lugarFavorito.trim())) {
                model.addAttribute("error", "Las respuestas de seguridad no coinciden.");
                return "recuperar";
            }
            if (!nuevaContrasena.equals(confirmarContrasena)) {
                model.addAttribute("error", "Las contraseñas no coinciden.");
                return "recuperar";
            }
            if (nuevaContrasena.length() < 8) {
                model.addAttribute("error", "La nueva contraseña debe tener al menos 8 caracteres.");
                return "recuperar";
            }
            usuario.setPassword(passwordEncoder.encode(nuevaContrasena));
            usuarioServicio.actualizarUsuario(usuario);
            model.addAttribute("mensajeExito", "¡Contraseña restablecida correctamente! Ahora puedes iniciar sesión.");
            return "login";
        } catch (Exception e) {
            model.addAttribute("error", "Ocurrió un error: " + e.getMessage());
            return "recuperar";
        }
    }
} 