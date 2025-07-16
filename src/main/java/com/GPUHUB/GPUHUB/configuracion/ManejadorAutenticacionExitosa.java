package com.GPUHUB.GPUHUB.configuracion;

import com.GPUHUB.GPUHUB.servicio.UsuarioServicio;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ManejadorAutenticacionExitosa implements AuthenticationSuccessHandler {

    private final UsuarioServicio usuarioServicio;

    @Autowired
    public ManejadorAutenticacionExitosa(UsuarioServicio usuarioServicio) {
        this.usuarioServicio = usuarioServicio;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, 
                                      Authentication authentication) throws IOException, ServletException {
        String username = authentication.getName();
        
        // Actualizar último acceso del usuario
        usuarioServicio.actualizarUltimoAcceso(username);
        
        // Redirigir al panel de usuario normal
        response.sendRedirect("/panel");
    }
}
