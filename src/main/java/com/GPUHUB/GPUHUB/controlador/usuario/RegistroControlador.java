package com.GPUHUB.GPUHUB.controlador.usuario;

import com.GPUHUB.GPUHUB.dto.RegistroUsuarioDTO;
import com.GPUHUB.GPUHUB.servicio.UsuarioServicio;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/registro")
public class RegistroControlador {

    private final UsuarioServicio usuarioServicio;

    @Autowired
    public RegistroControlador(UsuarioServicio usuarioServicio) {
        this.usuarioServicio = usuarioServicio;
    }

    @GetMapping
    public String mostrarFormularioRegistro(Model model) {
        model.addAttribute("usuario", new RegistroUsuarioDTO());
        return "registro";
    }

    @PostMapping
    public String registrarUsuario(@Valid @ModelAttribute("usuario") RegistroUsuarioDTO registroDTO,
                                  BindingResult result,
                                  RedirectAttributes redirectAttributes,
                                  Model model) {
        if (result.hasErrors()) {
            model.addAttribute("org.springframework.validation.BindingResult.usuario", result);
            model.addAttribute("usuario", registroDTO);
            
            result.getFieldErrors().forEach(error -> 
                model.addAttribute(error.getField() + "Error", error.getDefaultMessage())
            );
            
            return "registro";
        }
        
        try {
            usuarioServicio.guardar(registroDTO);
            redirectAttributes.addFlashAttribute("mensajeExito", "¡Registro exitoso! Por favor inicia sesión.");
            return "redirect:/login?registroExitoso";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("usuario", registroDTO);
            return "registro";
        }
    }
}
