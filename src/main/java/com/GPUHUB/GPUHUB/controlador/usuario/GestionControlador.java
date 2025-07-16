package com.GPUHUB.GPUHUB.controlador.usuario;

import com.GPUHUB.GPUHUB.modelo.Trabajador;
import com.GPUHUB.GPUHUB.modelo.Usuario;
import com.GPUHUB.GPUHUB.servicio.TrabajadorServicio;
import com.GPUHUB.GPUHUB.servicio.UsuarioServicio;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/gestion")
public class GestionControlador {

    private final UsuarioServicio usuarioServicio;
    private final TrabajadorServicio trabajadorServicio;

    public GestionControlador(UsuarioServicio usuarioServicio, 
                            TrabajadorServicio trabajadorServicio) {
        this.usuarioServicio = usuarioServicio;
        this.trabajadorServicio = trabajadorServicio;
    }

    @GetMapping
    public String mostrarGestion(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Usuario usuario = usuarioServicio.buscarPorCorreo(userDetails.getUsername());
        // Usamos listarActivosPorTienda para mostrar solo los trabajadores con acceso
        List<Trabajador> trabajadores = trabajadorServicio.listarActivosPorTienda(usuario.getTienda());
        
        model.addAttribute("usuario", usuario);
        model.addAttribute("trabajadores", trabajadores);
        return "gestion";
    }
}
