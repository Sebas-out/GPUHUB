package com.GPUHUB.GPUHUB.controlador.trabajador;

import com.GPUHUB.GPUHUB.modelo.Cambios;
import com.GPUHUB.GPUHUB.modelo.Tienda;
import com.GPUHUB.GPUHUB.modelo.Trabajador;
import com.GPUHUB.GPUHUB.servicio.CambiosServicio;
import com.GPUHUB.GPUHUB.servicio.TrabajadorServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/trabajador/panel")
public class PanelTrabajadorControlador {

    private final TrabajadorServicio trabajadorServicio;
    private final CambiosServicio cambiosServicio;

    @Autowired
    public PanelTrabajadorControlador(TrabajadorServicio trabajadorServicio, CambiosServicio cambiosServicio) {
        this.trabajadorServicio = trabajadorServicio;
        this.cambiosServicio = cambiosServicio;
    }

    @GetMapping
    public String mostrarPanelTrabajador(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        // El username es el DNI del trabajador (según la implementación de TrabajadorUserDetailsService)
        String dni = userDetails.getUsername();
        
        // Buscar el trabajador por DNI
        Optional<Trabajador> trabajadorOpt = trabajadorServicio.buscarPorDni(dni);
        
        if (trabajadorOpt.isPresent()) {
            Trabajador trabajador = trabajadorOpt.get();
            model.addAttribute("trabajador", trabajador);
            model.addAttribute("username", trabajador.getNombres() + " " + trabajador.getApellidos());
        } else {
            // Si no se encuentra por DNI, intentar por correo (por si acaso)
            trabajadorOpt = trabajadorServicio.buscarPorCorreo(dni);
            if (trabajadorOpt.isPresent()) {
                Trabajador trabajador = trabajadorOpt.get();
                model.addAttribute("trabajador", trabajador);
                model.addAttribute("username", trabajador.getNombres() + " " + trabajador.getApellidos());
            } else {
                // Si no se encuentra el trabajador, redirigir a la página de error
                return "redirect:/error?mensaje=Trabajador no encontrado";
            }
        }
        
        return "trabajador/panel-trabajador";
    }

    @GetMapping("/inventario")
    public String mostrarInventarioTrabajador(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        // El username es el DNI del trabajador
        String dni = userDetails.getUsername();
        
        // Buscar el trabajador por DNI
        Optional<Trabajador> trabajadorOpt = trabajadorServicio.buscarPorDni(dni);
        
        if (trabajadorOpt.isPresent()) {
            Trabajador trabajador = trabajadorOpt.get();
            model.addAttribute("trabajador", trabajador);
            model.addAttribute("username", trabajador.getNombres() + " " + trabajador.getApellidos());
        } else {
            // Si no se encuentra por DNI, intentar por correo
            trabajadorOpt = trabajadorServicio.buscarPorCorreo(dni);
            if (trabajadorOpt.isPresent()) {
                Trabajador trabajador = trabajadorOpt.get();
                model.addAttribute("trabajador", trabajador);
                model.addAttribute("username", trabajador.getNombres() + " " + trabajador.getApellidos());
            } else {
                // Si no se encuentra el trabajador, redirigir a la página de error
                return "redirect:/error?mensaje=Trabajador no encontrado";
            }
        }
        
        return "trabajador/inventario-trabajador";
    }

    @GetMapping("/cambios-trabajador")
    public String mostrarCambiosTrabajador(@AuthenticationPrincipal UserDetails userDetails,
                                           @RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "10") int size,
                                           Model model) {
        String username = userDetails.getUsername();
        Optional<Trabajador> trabajadorOpt = trabajadorServicio.buscarPorDni(username);
        if (!trabajadorOpt.isPresent()) {
            trabajadorOpt = trabajadorServicio.buscarPorCorreo(username);
        }
        if (!trabajadorOpt.isPresent()) {
            return "redirect:/error?mensaje=Trabajador no encontrado";
        }
        Trabajador trabajador = trabajadorOpt.get();
        Tienda tienda = trabajador.getTienda();
        if (tienda == null) {
            return "redirect:/error?mensaje=Trabajador sin tienda asignada";
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by("fechaCambio").descending());
        Page<Cambios> cambiosPage = cambiosServicio.obtenerCambiosPorTienda(tienda, pageable);
        long totalCambios = cambiosServicio.contarCambiosPorTienda(tienda);
        List<Cambios> cambiosCreacion = cambiosServicio.obtenerCambiosPorTiendaYTipo(tienda, "CREO");
        List<Cambios> cambiosEdicion = cambiosServicio.obtenerCambiosPorTiendaYTipo(tienda, "EDITO");
        List<Cambios> cambiosEliminacion = cambiosServicio.obtenerCambiosPorTiendaYTipo(tienda, "ELIMINO");
        model.addAttribute("cambios", cambiosPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", cambiosPage.getTotalPages());
        model.addAttribute("totalItems", cambiosPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("totalCambios", totalCambios);
        model.addAttribute("totalCreaciones", cambiosCreacion.size());
        model.addAttribute("totalEdiciones", cambiosEdicion.size());
        model.addAttribute("totalEliminaciones", cambiosEliminacion.size());
        model.addAttribute("esTrabajador", true);
        model.addAttribute("nombreUsuario", trabajador.getNombres() + " " + trabajador.getApellidos());
        model.addAttribute("trabajador", trabajador);
        model.addAttribute("username", trabajador.getNombres() + " " + trabajador.getApellidos());
        return "trabajador/cambios-trabajador";
    }
}
