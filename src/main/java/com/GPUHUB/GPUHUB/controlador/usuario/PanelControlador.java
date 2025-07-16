package com.GPUHUB.GPUHUB.controlador.usuario;

import com.GPUHUB.GPUHUB.modelo.Usuario;
import com.GPUHUB.GPUHUB.servicio.UsuarioServicio;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/panel")
public class PanelControlador {

    private final UsuarioServicio usuarioServicio;

    public PanelControlador(UsuarioServicio usuarioServicio) {
        this.usuarioServicio = usuarioServicio;
    }

    @GetMapping
    public String mostrarPanel(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Usuario usuario = usuarioServicio.buscarPorCorreo(userDetails.getUsername());
        model.addAttribute("usuario", usuario);
        return "panel";
    }
}
