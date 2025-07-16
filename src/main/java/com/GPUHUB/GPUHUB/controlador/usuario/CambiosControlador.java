package com.GPUHUB.GPUHUB.controlador.usuario;

import com.GPUHUB.GPUHUB.modelo.Cambios;
import com.GPUHUB.GPUHUB.modelo.Tienda;
import com.GPUHUB.GPUHUB.modelo.Trabajador;
import com.GPUHUB.GPUHUB.modelo.Usuario;
import com.GPUHUB.GPUHUB.servicio.CambiosServicio;
import com.GPUHUB.GPUHUB.servicio.TrabajadorServicio;
import com.GPUHUB.GPUHUB.servicio.UsuarioServicio;
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
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
public class CambiosControlador {

    @Autowired
    private CambiosServicio cambiosServicio;
    
    @Autowired
    private UsuarioServicio usuarioServicio;
    
    @Autowired
    private TrabajadorServicio trabajadorServicio;

    @GetMapping("/cambios")
    public String mostrarCambios(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        
        try {
            System.out.println("Iniciando carga de cambios para usuario: " + userDetails.getUsername());
            
            // Determinar si es usuario o trabajador
            Usuario usuario = usuarioServicio.buscarPorCorreo(userDetails.getUsername());
            Trabajador trabajador = null;
            Tienda tienda = null;
            
            if (usuario != null) {
                System.out.println("Usuario encontrado: " + usuario.getNombres());
                tienda = usuario.getTienda();
            } else {
                System.out.println("Buscando como trabajador...");
                // Intentar buscar como trabajador
                trabajador = trabajadorServicio.buscarPorDni(userDetails.getUsername()).orElse(null);
                if (trabajador == null) {
                    trabajador = trabajadorServicio.buscarPorCorreo(userDetails.getUsername()).orElse(null);
                }
                if (trabajador != null) {
                    System.out.println("Trabajador encontrado: " + trabajador.getNombres());
                    tienda = trabajador.getTienda();
                }
            }
            
            if (tienda == null) {
                System.out.println("No se encontró tienda para el usuario");
                return "redirect:/login?error=no_tienda";
            }
            
            System.out.println("Tienda encontrada: " + tienda.getNombreTienda());
            
            // Configurar paginación
            Pageable pageable = PageRequest.of(page, size, Sort.by("fechaCambio").descending());
            
            // Obtener cambios paginados
            Page<Cambios> cambiosPage = cambiosServicio.obtenerCambiosPorTienda(tienda, pageable);
            System.out.println("Cambios obtenidos: " + cambiosPage.getTotalElements());
            
            // Obtener estadísticas
            long totalCambios = cambiosServicio.contarCambiosPorTienda(tienda);
            List<Cambios> cambiosCreacion = cambiosServicio.obtenerCambiosPorTiendaYTipo(tienda, "CREO");
            List<Cambios> cambiosEdicion = cambiosServicio.obtenerCambiosPorTiendaYTipo(tienda, "EDITO");
            List<Cambios> cambiosEliminacion = cambiosServicio.obtenerCambiosPorTiendaYTipo(tienda, "ELIMINO");
            
            // Agregar datos al modelo
            model.addAttribute("cambios", cambiosPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", cambiosPage.getTotalPages());
            model.addAttribute("totalItems", cambiosPage.getTotalElements());
            model.addAttribute("pageSize", size);
            
            // Estadísticas
            model.addAttribute("totalCambios", totalCambios);
            model.addAttribute("totalCreaciones", cambiosCreacion.size());
            model.addAttribute("totalEdiciones", cambiosEdicion.size());
            model.addAttribute("totalEliminaciones", cambiosEliminacion.size());
            
            // Información del usuario/trabajador
            model.addAttribute("esTrabajador", trabajador != null);
            if (trabajador != null) {
                model.addAttribute("nombreUsuario", trabajador.getNombres() + " " + trabajador.getApellidos());
                model.addAttribute("trabajador", trabajador);
                model.addAttribute("username", trabajador.getNombres() + " " + trabajador.getApellidos());
            } else {
                model.addAttribute("nombreUsuario", usuario.getNombres() + " " + usuario.getApellidos());
                model.addAttribute("usuario", usuario);
            }
            
            System.out.println("Página de cambios cargada exitosamente");
            return "cambios";
            
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error en CambiosControlador: " + e.getMessage());
            model.addAttribute("error", "Error al cargar los cambios: " + e.getMessage());
            model.addAttribute("cambios", new ArrayList<>());
            model.addAttribute("currentPage", 0);
            model.addAttribute("totalPages", 0);
            model.addAttribute("totalItems", 0);
            model.addAttribute("pageSize", 10);
            model.addAttribute("totalCambios", 0);
            model.addAttribute("totalCreaciones", 0);
            model.addAttribute("totalEdiciones", 0);
            model.addAttribute("totalEliminaciones", 0);
            model.addAttribute("esTrabajador", false);
            return "cambios";
        }
    }
    
    @GetMapping("/cambios/test")
    public String testCambios(Model model) {
        try {
            System.out.println("Probando endpoint de cambios...");
            model.addAttribute("cambios", new ArrayList<>());
            model.addAttribute("currentPage", 0);
            model.addAttribute("totalPages", 0);
            model.addAttribute("totalItems", 0);
            model.addAttribute("pageSize", 10);
            model.addAttribute("totalCambios", 0);
            model.addAttribute("totalCreaciones", 0);
            model.addAttribute("totalEdiciones", 0);
            model.addAttribute("totalEliminaciones", 0);
            model.addAttribute("esTrabajador", false);
            model.addAttribute("usuario", null);
            System.out.println("Endpoint de prueba cargado exitosamente");
            return "cambios";
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error en CambiosControlador: " + e.getMessage());
            model.addAttribute("error", "Error al cargar los cambios: " + e.getMessage());
            model.addAttribute("cambios", new ArrayList<>());
            model.addAttribute("currentPage", 0);
            model.addAttribute("totalPages", 0);
            model.addAttribute("totalItems", 0);
            model.addAttribute("pageSize", 10);
            model.addAttribute("totalCambios", 0);
            model.addAttribute("totalCreaciones", 0);
            model.addAttribute("totalEdiciones", 0);
            model.addAttribute("totalEliminaciones", 0);
            model.addAttribute("esTrabajador", false);
            return "cambios";
        }
    }
} 