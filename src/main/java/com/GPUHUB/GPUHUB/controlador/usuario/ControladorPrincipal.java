package com.GPUHUB.GPUHUB.controlador.usuario;

import com.GPUHUB.GPUHUB.modelo.Usuario;
import com.GPUHUB.GPUHUB.servicio.UsuarioServicio;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ControladorPrincipal {

    private final UsuarioServicio usuarioServicio;

    public ControladorPrincipal(UsuarioServicio usuarioServicio) {
        this.usuarioServicio = usuarioServicio;
    }

    @GetMapping("/")
    public String mostrarPaginaInicio() {
        return "index"; 
    }
    
    @GetMapping("/buscar")
    public String mostrarPaginaBuscar() {
        return "buscar";
    }
    

    
    @GetMapping("/error")
    public String manejarError() {
        return "error";
    }
    

    @GetMapping("/inventario")
    public String mostrarInventario(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails != null) {
            Usuario usuario = usuarioServicio.buscarPorCorreo(userDetails.getUsername());
            model.addAttribute("usuario", usuario);
        }
        return "inventario";
    }

    @GetMapping("/servicios")
    public String mostrarServicios() {
        return "servicios";
    }

    @GetMapping("/nosotros")
    public String mostrarNosotros() {
        return "nosotros";
    }

    @GetMapping("/condiciones")
    public String mostrarCondiciones() {
        return "condiciones";
    }

    @GetMapping("/politica")
    public String mostrarPolitica() {
        return "politica";
    }

    @GetMapping("/cookies")
    public String mostrarCookies() {
        return "cookies";
    }
}
