package com.GPUHUB.GPUHUB.controlador.trabajador;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/trabajador")
public class LoginTrabajadorControlador {

    @GetMapping("/login")
    public String mostrarLoginTrabajador(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            @RequestParam(value = "accesoExpirado", required = false) String accesoExpirado,
            @RequestParam(value = "accesoInactivo", required = false) String accesoInactivo,
            Model model) {
        
        if (error != null) {
            model.addAttribute("error", "DNI o contraseña incorrectos");
        }
        
        if (accesoExpirado != null) {
            model.addAttribute("error", "Su acceso ha expirado. Por favor, contacte al administrador.");
        }
        
        if (accesoInactivo != null) {
            model.addAttribute("error", "Su cuenta no tiene acceso activo. Por favor, contacte al administrador.");
        }
        
        if (logout != null) {
            model.addAttribute("message", "Has cerrado sesión correctamente");
        }
        
        return "trabajador/login-trabajador";
    }
    
    @PostMapping("/login")
    public String procesarLoginTrabajador() {
        // La autenticación real la maneja Spring Security
        return "redirect:/trabajador/panel";
    }
    
    @GetMapping("/logout")
    public String logoutTrabajador(HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        return "redirect:/trabajador/login?logout";
    }
}
