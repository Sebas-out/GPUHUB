package com.GPUHUB.GPUHUB.controlador.api;

import com.GPUHUB.GPUHUB.dto.ProductoDTO;
import com.GPUHUB.GPUHUB.modelo.Almacen;
import com.GPUHUB.GPUHUB.modelo.Datos;
import com.GPUHUB.GPUHUB.modelo.Producto;
import com.GPUHUB.GPUHUB.modelo.Trabajador;
import com.GPUHUB.GPUHUB.servicio.AlmacenServicio;
import com.GPUHUB.GPUHUB.servicio.ProductoServicio;
import com.GPUHUB.GPUHUB.servicio.TrabajadorServicio;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trabajador")
public class TrabajadorAlmacenApiControlador {

    @Autowired
    private AlmacenServicio almacenServicio;
    
    @Autowired
    private TrabajadorServicio trabajadorServicio;
    
    @Autowired
    private ProductoServicio productoServicio;

    @GetMapping("/almacenes")
    public ResponseEntity<List<Almacen>> obtenerTodosLosAlmacenes(@AuthenticationPrincipal UserDetails userDetails) {
        Trabajador trabajador = trabajadorServicio.buscarPorDni(userDetails.getUsername()).orElse(null);
        if (trabajador == null) {
            // Intentar buscar por correo si no se encuentra por DNI
            trabajador = trabajadorServicio.buscarPorCorreo(userDetails.getUsername()).orElse(null);
        }
        
        if (trabajador == null || trabajador.getTienda() == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        List<Almacen> almacenes = almacenServicio.obtenerAlmacenesPorTienda(trabajador.getTienda().getIdTienda());
        return ResponseEntity.ok(almacenes);
    }
    
    @PostMapping("/almacenes")
    public ResponseEntity<?> crearAlmacen(
            @RequestBody Almacen almacen,
            @AuthenticationPrincipal UserDetails userDetails) {
                
        try {
            Trabajador trabajador = trabajadorServicio.buscarPorDni(userDetails.getUsername()).orElse(null);
            if (trabajador == null) {
                trabajador = trabajadorServicio.buscarPorCorreo(userDetails.getUsername()).orElse(null);
            }
            
            if (trabajador == null || trabajador.getTienda() == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Trabajador no autorizado o no tiene una tienda asignada");
            }
            
            almacen.setTienda(trabajador.getTienda());
            Almacen nuevoAlmacen = almacenServicio.guardarAlmacen(almacen);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(new AlmacenResponse(nuevoAlmacen));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al crear el almacén: " + e.getMessage());
        }
    }
    
    // Clase DTO para la respuesta
    @lombok.Data
    @lombok.AllArgsConstructor
    private static class AlmacenResponse {
        private Long id_almacen;
        private String nombre;
        private String direccion;
        
        public AlmacenResponse(Almacen almacen) {
            this.id_almacen = almacen.getId_almacen();
            this.nombre = almacen.getNombre();
            this.direccion = almacen.getDireccion();
        }
    }
    
    @PutMapping("/almacenes/{id}")
    public ResponseEntity<?> actualizarAlmacen(
            @PathVariable Long id,
            @RequestBody Almacen almacenActualizado,
            @AuthenticationPrincipal UserDetails userDetails) {
                
        try {
            Trabajador trabajador = trabajadorServicio.buscarPorDni(userDetails.getUsername()).orElse(null);
            if (trabajador == null) {
                trabajador = trabajadorServicio.buscarPorCorreo(userDetails.getUsername()).orElse(null);
            }
            
            if (trabajador == null || trabajador.getTienda() == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Trabajador no autorizado o no tiene una tienda asignada");
            }
            
            Almacen almacenExistente = almacenServicio.obtenerAlmacenPorId(id);
            if (almacenExistente == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Verificar que el almacén pertenezca a la tienda del trabajador
            if (!almacenExistente.getTienda().getIdTienda().equals(trabajador.getTienda().getIdTienda())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("No tiene permiso para modificar este almacén");
            }
            
            // Actualizar solo los campos que se pueden modificar, manteniendo los productos existentes
            almacenExistente.setNombre(almacenActualizado.getNombre());
            almacenExistente.setDireccion(almacenActualizado.getDireccion());
            
            // Guardar el almacén actualizado
            Almacen almacenActualizadoObj = almacenServicio.guardarAlmacen(almacenExistente);
            
            return ResponseEntity.ok(new AlmacenResponse(almacenActualizadoObj));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al actualizar el almacén: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/almacenes/{id}")
    public ResponseEntity<Void> eliminarAlmacen(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
                
        Trabajador trabajador = trabajadorServicio.buscarPorDni(userDetails.getUsername()).orElse(null);
        if (trabajador == null) {
            trabajador = trabajadorServicio.buscarPorCorreo(userDetails.getUsername()).orElse(null);
        }
        
        if (trabajador == null || trabajador.getTienda() == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        // Verificar que el almacén pertenezca a la tienda del trabajador
        Almacen almacen = almacenServicio.obtenerAlmacenPorId(id);
        if (almacen == null || !almacen.getTienda().getIdTienda().equals(trabajador.getTienda().getIdTienda())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        
        almacenServicio.eliminarAlmacen(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/almacenes/{id}")
    public ResponseEntity<Almacen> obtenerAlmacenPorId(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
                
        Trabajador trabajador = trabajadorServicio.buscarPorDni(userDetails.getUsername()).orElse(null);
        if (trabajador == null) {
            trabajador = trabajadorServicio.buscarPorCorreo(userDetails.getUsername()).orElse(null);
        }
        
        if (trabajador == null || trabajador.getTienda() == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        Almacen almacen = almacenServicio.obtenerAlmacenPorId(id);
        if (almacen == null || !almacen.getTienda().getIdTienda().equals(trabajador.getTienda().getIdTienda())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        
        return ResponseEntity.ok(almacen);
    }
    
    @GetMapping("/almacenes/{id}/productos/count")
    public ResponseEntity<Map<String, Integer>> contarProductosEnAlmacen(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
                
        Trabajador trabajador = trabajadorServicio.buscarPorDni(userDetails.getUsername()).orElse(null);
        if (trabajador == null) {
            trabajador = trabajadorServicio.buscarPorCorreo(userDetails.getUsername()).orElse(null);
        }
        
        if (trabajador == null || trabajador.getTienda() == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        // Verificar que el almacén pertenezca a la tienda del trabajador
        Almacen almacen = almacenServicio.obtenerAlmacenPorId(id);
        if (almacen == null || !almacen.getTienda().getIdTienda().equals(trabajador.getTienda().getIdTienda())) {
            return ResponseEntity.notFound().build();
        }
        
        int count = productoServicio.contarProductosPorAlmacen(id);
        Map<String, Integer> response = new HashMap<>();
        response.put("count", count);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/almacenes/{id}/productos")
    public ResponseEntity<?> obtenerProductosDeAlmacen(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
                
        try {
            Trabajador trabajador = trabajadorServicio.buscarPorDni(userDetails.getUsername()).orElse(null);
            if (trabajador == null) {
                trabajador = trabajadorServicio.buscarPorCorreo(userDetails.getUsername()).orElse(null);
            }
            
            if (trabajador == null || trabajador.getTienda() == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Trabajador no autorizado o no tiene una tienda asignada");
            }
            
            Almacen almacen = almacenServicio.obtenerAlmacenPorId(id);
            if (almacen == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Verificar que el almacén pertenezca a la tienda del trabajador
            if (!almacen.getTienda().getIdTienda().equals(trabajador.getTienda().getIdTienda())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("No tiene permiso para ver los productos de este almacén");
            }
            
            List<Producto> productos = productoServicio.obtenerProductosPorAlmacen(id);
            
            // Convertir a DTOs
            List<ProductoDTO> productosDTO = new ArrayList<>();
            for (Producto producto : productos) {
                // Convertir la imagen a Base64 para el frontend
                String imagenBase64 = null;
                if (producto.getImagen() != null) {
                    imagenBase64 = "data:image/jpeg;base64," + 
                        java.util.Base64.getEncoder().encodeToString(producto.getImagen());
                }
                
                // Obtener especificaciones y componentes
                List<Datos> especificaciones = producto.getDatos() != null ?
                    producto.getDatos().stream()
                        .filter(d -> d.getTipo() == 1)
                        .collect(Collectors.toList()) :
                    new ArrayList<>();
                    
                List<Datos> componentes = producto.getDatos() != null ?
                    producto.getDatos().stream()
                        .filter(d -> d.getTipo() == 0)
                        .collect(Collectors.toList()) :
                    new ArrayList<>();
                
                // Convertir la imagen a Base64 si existe
                String imgBase64 = null;
                if (producto.getImagen() != null && producto.getImagen().length > 0) {
                    imgBase64 = "data:image/jpeg;base64," + 
                              Base64.getEncoder().encodeToString(producto.getImagen());
                }
                
                // Crear DTO
                ProductoDTO dto = new ProductoDTO();
                dto.setId_producto(producto.getId_producto());
                dto.setMarcaEnsambladora(producto.getMarcaEnsambladora());
                dto.setMarcaTarjeta(producto.getMarcaTarjeta());
                dto.setModelo(producto.getModelo());
                dto.setNumeroSerie(producto.getNumeroSerie());
                dto.setDescripcion(producto.getDescripcion());
                dto.setEstado(producto.getEstado());
                dto.setIdAlmacen(producto.getAlmacen() != null ? producto.getAlmacen().getId_almacen() : null);
                dto.setImagenBase64(imgBase64);
                
                // Agregar especificaciones y componentes al DTO
                dto.setEspecificaciones(especificaciones);
                dto.setComponentes(componentes);
                
                productosDTO.add(dto);
            }
            
            return ResponseEntity.ok(productosDTO);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al obtener productos: " + e.getMessage());
        }
    }
} 