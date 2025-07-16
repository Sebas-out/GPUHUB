package com.GPUHUB.GPUHUB.controlador.usuario;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;

@Controller
public class TestControlador {

    @GetMapping("/test-cambios")
    public String testCambios(Model model) {
        System.out.println("Probando endpoint de cambios...");
        
        try {
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
            model.addAttribute("error", null);
            
            System.out.println("Endpoint de prueba cargado exitosamente");
            return "cambios-simple";
            
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error en TestControlador: " + e.getMessage());
            model.addAttribute("error", "Error al cargar los cambios: " + e.getMessage());
            return "cambios-simple";
        }
    }
} 