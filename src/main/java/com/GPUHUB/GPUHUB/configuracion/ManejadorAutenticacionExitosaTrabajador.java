package com.GPUHUB.GPUHUB.configuracion;

import com.GPUHUB.GPUHUB.modelo.Trabajador;
import com.GPUHUB.GPUHUB.servicio.TrabajadorServicio;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
public class ManejadorAutenticacionExitosaTrabajador implements AuthenticationSuccessHandler {

    private final TrabajadorServicio trabajadorServicio;

    @Autowired
    public ManejadorAutenticacionExitosaTrabajador(TrabajadorServicio trabajadorServicio) {
        this.trabajadorServicio = trabajadorServicio;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, 
                                      Authentication authentication) throws IOException, ServletException {
        String dni = authentication.getName();
        
        // Buscar si existe un trabajador con este DNI
        Optional<Trabajador> trabajadorOpt = trabajadorServicio.buscarPorDni(dni);
        if (trabajadorOpt.isPresent()) {
            Trabajador trabajador = trabajadorOpt.get();
            
            // Verificar si el trabajador tiene acceso
            if (trabajador.getEstado() == null || trabajador.getEstado() != 1) {
                // Cerrar la sesión actual
                new SecurityContextLogoutHandler().logout(request, response, authentication);
                
                // Redirigir a la página de login de trabajadores con mensaje de error
                response.sendRedirect("/trabajador/login?error=accesoInactivo");
                return;
            }
            
            // Verificar si la fecha de acceso es válida
            if (trabajador.getFechaAcceso() == null || trabajador.getFechaAcceso().isBefore(java.time.LocalDate.now())) {
                // Cerrar la sesión actual
                new SecurityContextLogoutHandler().logout(request, response, authentication);
                
                // Redirigir a la página de login de trabajadores con mensaje de error
                response.sendRedirect("/trabajador/login?error=accesoExpirado");
                return;
            }
            
            // Redirigir al panel de trabajador
            response.sendRedirect("/trabajador/panel");
        } else {
            // Si por alguna razón no se encuentra el trabajador, redirigir al login de trabajadores
            new SecurityContextLogoutHandler().logout(request, response, authentication);
            response.sendRedirect("/trabajador/login?error=credencialesInvalidas");
        }
    }
}
