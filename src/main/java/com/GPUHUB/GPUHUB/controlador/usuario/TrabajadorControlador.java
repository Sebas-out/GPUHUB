package com.GPUHUB.GPUHUB.controlador.usuario;

import com.GPUHUB.GPUHUB.modelo.Trabajador;
import com.GPUHUB.GPUHUB.modelo.Usuario;
import com.GPUHUB.GPUHUB.servicio.TrabajadorServicio;
import com.GPUHUB.GPUHUB.servicio.UsuarioServicio;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/trabajadores")
public class TrabajadorControlador {

    private final UsuarioServicio usuarioServicio;
    private final TrabajadorServicio trabajadorServicio;
    private final PasswordEncoder passwordEncoder;

    public TrabajadorControlador(UsuarioServicio usuarioServicio, 
                                TrabajadorServicio trabajadorServicio,
                                PasswordEncoder passwordEncoder) {
        this.usuarioServicio = usuarioServicio;
        this.trabajadorServicio = trabajadorServicio;
        this.passwordEncoder = passwordEncoder;
    }


    @PostMapping("/guardar")
    @Transactional
    public String guardarTrabajador(
            @ModelAttribute("trabajador") Trabajador trabajador,
            @RequestParam("fechaAcceso") String fechaAccesoStr,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        System.out.println("Iniciando guardado de trabajador...");
        
        try {
            // 1. Obtener el usuario actual y configurar datos básicos
            Usuario usuario = usuarioServicio.buscarPorCorreo(userDetails.getUsername());
            model.addAttribute("usuario", usuario);
            model.addAttribute("fechaActual", LocalDate.now().toString());
            
            // 2. Establecer la tienda del trabajador
            trabajador.setTienda(usuario.getTienda());
            trabajador.setRol("TRABAJADOR");
            
            // 3. Establecer la fecha de acceso
            trabajador.setFechaAcceso(LocalDate.parse(fechaAccesoStr));
            
            // 4. Verificar si es un nuevo trabajador o una actualización
            boolean esNuevo = (trabajador.getIdTrabajador() == null);
            System.out.println("Es nuevo trabajador: " + esNuevo);
            
            // 5. Validar DNI
            // Normalizar el DNI (eliminar espacios y convertir a mayúsculas)
            String dni = trabajador.getDni() != null ? trabajador.getDni().trim().toUpperCase() : "";
            trabajador.setDni(dni);
            
            System.out.println("=== Validando DNI: " + dni + " (Nuevo: " + esNuevo + ") ===");
            System.out.println("ID del trabajador actual: " + trabajador.getIdTrabajador());
            
            // Validar formato de DNI (8 dígitos)
            if (dni.length() != 8 || !dni.matches("\\d{8}")) {
                String errorMsg = "El DNI debe tener exactamente 8 dígitos numéricos";
                System.out.println(errorMsg);
                model.addAttribute("error", errorMsg);
                model.addAttribute("trabajador", trabajador);
                return esNuevo ? "nuevo-trabajador" : "editar-trabajador";
            }
            
            // Verificar si el DNI ya existe
            Optional<Trabajador> trabajadorConMismoDni = trabajadorServicio.buscarPorDni(dni);
            
            if (esNuevo) {
                // Para nuevo trabajador, no debe existir ningún otro con el mismo DNI
                if (trabajadorConMismoDni.isPresent()) {
                    String errorMsg = "Ya existe un trabajador con el DNI: " + dni;
                    System.out.println(errorMsg);
                    model.addAttribute("error", errorMsg);
                    model.addAttribute("trabajador", trabajador);
                    return "nuevo-trabajador";
                }
            } else {
                // Para actualización, permitir mantener el mismo DNI pero no permitir duplicados con otros trabajadores
                if (trabajadorConMismoDni.isPresent() && 
                    !trabajadorConMismoDni.get().getIdTrabajador().equals(trabajador.getIdTrabajador())) {
                    String errorMsg = "El DNI " + dni + " ya está en uso por otro trabajador";
                    System.out.println(errorMsg);
                    model.addAttribute("error", errorMsg);
                    model.addAttribute("trabajador", trabajador);
                    return "editar-trabajador";
                }
            }
            
            // 6. Validar teléfono
            String telefono = trabajador.getTelefono() != null ? trabajador.getTelefono().trim() : "";
            trabajador.setTelefono(telefono);
            
            if (!telefono.isEmpty()) {
                // Verificar si el teléfono ya existe para otro trabajador
                Optional<Trabajador> trabajadorConMismoTelefono = trabajadorServicio.buscarPorTelefono(telefono);
                
                if (trabajadorConMismoTelefono.isPresent()) {
                    // Si es un nuevo trabajador o es una actualización con un teléfono diferente
                    if (esNuevo || !trabajadorConMismoTelefono.get().getIdTrabajador().equals(trabajador.getIdTrabajador())) {
                        String errorMsg = "El teléfono " + telefono + " ya está registrado para otro trabajador";
                        System.out.println(errorMsg);
                        model.addAttribute("error", errorMsg);
                        model.addAttribute("trabajador", trabajador);
                        return esNuevo ? "nuevo-trabajador" : "editar-trabajador";
                    }
                }
            }
            
            // 7. Hashear la contraseña antes de guardar
            if (trabajador.getPassword() != null && !trabajador.getPassword().isEmpty()) {
                String passwordHasheada = passwordEncoder.encode(trabajador.getPassword());
                trabajador.setPassword(passwordHasheada);
            }
            
            // 8. Guardar el trabajador
            trabajador.setEstado(1); // 1 = activo, 0 = inactivo
            Trabajador trabajadorGuardado = trabajadorServicio.guardar(trabajador);
            System.out.println("Trabajador guardado con ID: " + trabajadorGuardado.getIdTrabajador());
            
            // 8. Mostrar mensaje de éxito y redirigir
            String mensajeExito = esNuevo ? "Trabajador registrado exitosamente" : "Trabajador actualizado exitosamente";
            redirectAttributes.addFlashAttribute("mensajeExito", mensajeExito);
            
            // Redirigir al formulario en blanco después de guardar exitosamente
            return "redirect:/trabajadores/nuevo";
            
        } catch (Exception e) {
            String errorMsg = "Error al procesar la solicitud: " + e.getMessage();
            System.err.println(errorMsg);
            e.printStackTrace();
            model.addAttribute("error", errorMsg);
            model.addAttribute("trabajador", trabajador);
            return "nuevo-trabajador";
        }
    }
    
    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model,
            HttpServletRequest request) {
        
        // Limpiar el modelo si es una nueva carga y no un reenvío por error
        if (!isErrorPage(request)) {
            model.asMap().remove("error");
            model.asMap().remove("trabajador");
        }
        
        // Obtener el usuario actual
        Usuario usuario = usuarioServicio.buscarPorCorreo(userDetails.getUsername());
        
        // Solo crear un nuevo trabajador si no hay uno en el modelo
        if (!model.containsAttribute("trabajador")) {
            Trabajador trabajador = new Trabajador();
            trabajador.setTienda(usuario.getTienda());
            trabajador.setRol("TRABAJADOR");
            trabajador.setEstado(1); // 1 = Tiene acceso
            model.addAttribute("trabajador", trabajador);
        }
        
        model.addAttribute("usuario", usuario);
        model.addAttribute("fechaActual", LocalDate.now().toString());
        
        return "nuevo-trabajador";
    }
    
    // Método auxiliar para verificar si es una página de error
    private boolean isErrorPage(HttpServletRequest request) {
        return request.getAttribute("javax.servlet.error.status_code") != null;
    }
    

    
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEdicion(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {
        
        Usuario usuario = usuarioServicio.buscarPorCorreo(userDetails.getUsername());
        Trabajador trabajador = trabajadorServicio.buscarPorId(id);
        
        // Verificar que el trabajador pertenezca a la tienda del usuario
        if (!trabajador.getTienda().getIdTienda().equals(usuario.getTienda().getIdTienda())) {
            throw new RuntimeException("No tiene permisos para editar este trabajador");
        }
        
        model.addAttribute("usuario", usuario);
        model.addAttribute("trabajador", trabajador);
        model.addAttribute("fechaActual", LocalDate.now().toString());
        
        return "editar-trabajador";
    }
    
    @GetMapping("/obtener/{id}")
    @ResponseBody
    public ResponseEntity<?> obtenerTrabajador(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        try {
            // Obtener el trabajador existente
            Trabajador trabajador = trabajadorServicio.buscarPorId(id);
            
            // Verificar que el trabajador pertenezca a la tienda del usuario
            Usuario usuario = usuarioServicio.buscarPorCorreo(userDetails.getUsername());
            if (!trabajador.getTienda().getIdTienda().equals(usuario.getTienda().getIdTienda())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "No tiene permisos para ver este trabajador"));
            }
            
            // Crear un mapa con los datos del trabajador
            Map<String, Object> trabajadorData = new HashMap<>();
            trabajadorData.put("id", trabajador.getIdTrabajador());
            trabajadorData.put("dni", trabajador.getDni());
            trabajadorData.put("nombres", trabajador.getNombres());
            trabajadorData.put("apellidos", trabajador.getApellidos());
            trabajadorData.put("telefono", trabajador.getTelefono());
            trabajadorData.put("fechaAcceso", trabajador.getFechaAcceso());
            trabajadorData.put("rol", trabajador.getRol());

            
            return ResponseEntity.ok(trabajadorData);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Trabajador no encontrado: " + e.getMessage()));
        }
    }
    
    @PostMapping("/actualizar/{id}")
    @ResponseBody
    public ResponseEntity<?> actualizarTrabajador(
            @PathVariable Long id,
            @RequestParam Map<String, String> params,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        try {
            // Obtener el trabajador existente
            Trabajador trabajadorExistente = trabajadorServicio.buscarPorId(id);
            
            // Verificar que el trabajador pertenezca a la tienda del usuario
            Usuario usuario = usuarioServicio.buscarPorCorreo(userDetails.getUsername());
            if (!trabajadorExistente.getTienda().getIdTienda().equals(usuario.getTienda().getIdTienda())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "No tiene permisos para editar este trabajador"));
            }
            
            // Actualizar solo los campos que se pueden modificar
            if (params.containsKey("telefono")) {
                trabajadorExistente.setTelefono(params.get("telefono"));
            }
            if (params.containsKey("rol")) {
                trabajadorExistente.setRol(params.get("rol"));
            }
            
            // Actualizar la contraseña si se proporciona una nueva
            if (params.containsKey("password") && !params.get("password").isEmpty()) {
                String nuevaPassword = params.get("password");
                String passwordHasheada = passwordEncoder.encode(nuevaPassword);
                trabajadorExistente.setPassword(passwordHasheada);
            }
            
            // Guardar los cambios
            trabajadorServicio.actualizar(trabajadorExistente);
            
            // Devolver una respuesta exitosa
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Trabajador actualizado exitosamente"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al actualizar el trabajador: " + e.getMessage()));
        }
    }
    
    @DeleteMapping("/eliminar/{id}")
    @ResponseBody
    public ResponseEntity<?> eliminarTrabajador(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        try {
            // Verificar que el trabajador pertenezca a la tienda del usuario
            Usuario usuario = usuarioServicio.buscarPorCorreo(userDetails.getUsername());
            Trabajador trabajador = trabajadorServicio.buscarPorId(id);
            
            if (!trabajador.getTienda().getIdTienda().equals(usuario.getTienda().getIdTienda())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "No tiene permisos para eliminar este trabajador"));
            }
            
            // Eliminar el trabajador (borrado lógico)
            trabajadorServicio.eliminar(id);
            
            return ResponseEntity.ok()
                .body(Map.of("mensaje", "Trabajador eliminado exitosamente"));
                
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error al eliminar el trabajador: " + e.getMessage()));
        }
    }
    
    @PostMapping("/actualizar-fecha/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, String>> actualizarFechaAcceso(
            @PathVariable Long id,
            @RequestParam("fechaAcceso") String fechaAccesoStr,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Map<String, String> response = new HashMap<>();
        System.out.println("Iniciando actualización de fecha para trabajador ID: " + id);
        System.out.println("Nueva fecha recibida: " + fechaAccesoStr);
        
        try {
            // Validaciones básicas
            if (id == null || fechaAccesoStr == null || fechaAccesoStr.trim().isEmpty()) {
                System.out.println("Error: Datos de entrada inválidos");
                response.put("error", "Datos de entrada inválidos");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Obtener usuario
            String username = userDetails != null ? userDetails.getUsername() : "null";
            System.out.println("Usuario autenticado: " + username);
            
            Usuario usuario = usuarioServicio.buscarPorCorreo(username);
            if (usuario == null) {
                System.out.println("Error: Usuario no encontrado");
                response.put("error", "Usuario no encontrado");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            if (usuario.getTienda() == null) {
                System.out.println("Error: Usuario no tiene tienda asignada");
                response.put("error", "Usuario no tiene tienda asignada");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            
            // Obtener trabajador
            Trabajador trabajador = trabajadorServicio.buscarPorId(id);
            if (trabajador == null) {
                System.out.println("Error: Trabajador no encontrado con ID: " + id);
                response.put("error", "Trabajador no encontrado");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            if (!trabajador.getTienda().getIdTienda().equals(usuario.getTienda().getIdTienda())) {
                System.out.println("Error: El trabajador no pertenece a la tienda del usuario");
                response.put("error", "No tiene permisos para modificar este trabajador");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            
            try {
                // Actualizar fecha
                LocalDate fechaAcceso = LocalDate.parse(fechaAccesoStr);
                System.out.println("Fecha parseada correctamente: " + fechaAcceso);
                
                trabajador.setFechaAcceso(fechaAcceso);
                trabajadorServicio.actualizar(trabajador);
                
                System.out.println("Fecha actualizada exitosamente");
                response.put("mensaje", "Fecha actualizada correctamente");
                return ResponseEntity.ok(response);
                
            } catch (Exception e) {
                System.out.println("Error al actualizar la fecha: " + e.getMessage());
                e.printStackTrace();
                response.put("error", "Error al actualizar la fecha: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
            
        } catch (Exception e) {
            System.out.println("Error inesperado: " + e.getMessage());
            response.put("error", "Error inesperado: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
