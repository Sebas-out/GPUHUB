package com.GPUHUB.GPUHUB.controlador.api;

import com.GPUHUB.GPUHUB.dto.ProductoDTO;
import com.GPUHUB.GPUHUB.modelo.Almacen;
import com.GPUHUB.GPUHUB.modelo.Usuario;
import com.GPUHUB.GPUHUB.modelo.Datos;
import com.GPUHUB.GPUHUB.modelo.Producto;
import com.GPUHUB.GPUHUB.servicio.AlmacenServicio;
import com.GPUHUB.GPUHUB.servicio.ProductoServicio;
import com.GPUHUB.GPUHUB.servicio.UsuarioServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "http://localhost:8080", allowCredentials = "true")
@RequestMapping("/api/productos")
public class ProductoApiControlador {

    private final ProductoServicio productoServicio;
    private final AlmacenServicio almacenServicio;
    private final UsuarioServicio usuarioServicio;
    
    @Autowired
    public ProductoApiControlador(ProductoServicio productoServicio, 
                                 AlmacenServicio almacenServicio,
                                 UsuarioServicio usuarioServicio) {
        this.productoServicio = productoServicio;
        this.almacenServicio = almacenServicio;
        this.usuarioServicio = usuarioServicio;
    }

    // Ya no necesitamos constantes para el directorio de carga
    
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerProductoPorId(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Verificar que el usuario esté autenticado
            Usuario usuario = usuarioServicio.buscarPorCorreo(userDetails.getUsername());
            if (usuario == null || usuario.getTienda() == null) {
                response.put("success", false);
                response.put("message", "Usuario no autorizado o no tiene una tienda asignada");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            
            // Obtener el producto
            Producto producto = productoServicio.obtenerProductoPorId(id);
            if (producto == null) {
                response.put("success", false);
                response.put("message", "Producto no encontrado");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            // Verificar que el producto pertenezca a un almacén de la tienda del usuario
            if (producto.getAlmacen() == null || 
                producto.getAlmacen().getTienda() == null || 
                !producto.getAlmacen().getTienda().getIdTienda().equals(usuario.getTienda().getIdTienda())) {
                response.put("success", false);
                response.put("message", "No tiene permiso para acceder a este producto");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            
            // Convertir a DTO para la respuesta
            ProductoDTO productoDTO = convertirADTO(producto);
            return ResponseEntity.ok(productoDTO);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al obtener el producto: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<?> crearProducto(
            @RequestParam(value = "imagen", required = false) MultipartFile imagen,
            @RequestParam("idAlmacen") Long idAlmacen,
            @RequestParam("marcaEnsambladora") String marcaEnsambladora,
            @RequestParam("marcaTarjeta") String marcaTarjeta,
            @RequestParam("modelo") String modelo,
            @RequestParam("numeroSerie") String numeroSerie,
            @RequestParam(value = "descripcion", required = false) String descripcion,
            @RequestParam("estado") String estado,
            @RequestParam("especificaciones") String especificacionesJson,
            @RequestParam("componentes") String componentesJson,
            @RequestParam(value = "idProducto", required = false) Long idProducto,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Verificar que el usuario esté autenticado y tenga una tienda asignada
            Usuario usuario = usuarioServicio.buscarPorCorreo(userDetails.getUsername());
            if (usuario == null || usuario.getTienda() == null) {
                response.put("success", false);
                response.put("message", "Usuario no autorizado o no tiene una tienda asignada");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            
            // Verificar que el almacén pertenezca a la tienda del usuario
            Almacen almacen = almacenServicio.obtenerAlmacenPorId(idAlmacen);
            if (almacen == null || !almacen.getTienda().getIdTienda().equals(usuario.getTienda().getIdTienda())) {
                response.put("success", false);
                response.put("message", "No tiene permiso para agregar productos a este almacén");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            
            // Convertir JSON a listas
            ObjectMapper objectMapper = new ObjectMapper();
            List<Datos> especificaciones = objectMapper.readValue(especificacionesJson, 
                objectMapper.getTypeFactory().constructCollectionType(List.class, Datos.class));
                
            List<Datos> componentes = objectMapper.readValue(componentesJson, 
                objectMapper.getTypeFactory().constructCollectionType(List.class, Datos.class));
            
            // Crear el producto
            Producto producto = new Producto();
            producto.setMarcaEnsambladora(marcaEnsambladora);
            producto.setMarcaTarjeta(marcaTarjeta);
            producto.setModelo(modelo);
            producto.setNumeroSerie(numeroSerie);
            producto.setDescripcion(descripcion);
            producto.setEstado(estado);
            producto.setAlmacen(almacen);
            
            // Si es una edición, establecer el ID del producto
            if (idProducto != null) {
                producto.setId_producto(idProducto);
            }
            
            // Guardar la imagen si se proporcionó
            if (imagen != null && !imagen.isEmpty()) {
                try {
                    // Convertir la imagen a bytes y guardarla directamente en la base de datos
                    byte[] imagenBytes = imagen.getBytes();
                    producto.setImagen(imagenBytes);
                } catch (IOException e) {
                    System.err.println("Error al procesar la imagen: " + e.getMessage());
                    response.put("success", false);
                    response.put("message", "Error al procesar la imagen: " + e.getMessage());
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
                }
            }
            
            // Guardar el producto para obtener su ID
            Producto productoGuardado = productoServicio.guardarProducto(producto);
            
            // Si es una edición, eliminar los datos anteriores
            if (idProducto != null) {
                productoServicio.eliminarDatosPorProductoId(idProducto);
            }
            
            // Procesar las especificaciones (tipo 1)
            if (especificaciones != null) {
                for (Datos especificacion : especificaciones) {
                    if (especificacion.getDescripcion() != null && !especificacion.getDescripcion().trim().isEmpty()) {
                        especificacion.setTipo(1); // Tipo 1 para especificaciones técnicas
                        especificacion.setProducto(productoGuardado);
                        // Guardar cada especificación
                        productoServicio.guardarDato(especificacion);
                    }
                }
            }
            
            // Procesar los componentes compatibles (tipo 0)
            if (componentes != null) {
                for (Datos componente : componentes) {
                    if (componente.getDescripcion() != null && !componente.getDescripcion().trim().isEmpty()) {
                        componente.setTipo(0); // Tipo 0 para componentes compatibles
                        componente.setProducto(productoGuardado);
                        componente.setValor(null); // Asegurar que el valor sea nulo para componentes
                        // Guardar cada componente
                        productoServicio.guardarDato(componente);
                    }
                }
            }
            
            return ResponseEntity.status(HttpStatus.CREATED).body(convertirADTO(productoGuardado));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al crear el producto: " + e.getMessage());
        }
    }
    
    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<?> actualizarProducto(
            @PathVariable Long id,
            @RequestParam(value = "imagen", required = false) MultipartFile imagen,
            @RequestParam("idAlmacen") Long idAlmacen,
            @RequestParam("marcaEnsambladora") String marcaEnsambladora,
            @RequestParam("marcaTarjeta") String marcaTarjeta,
            @RequestParam("modelo") String modelo,
            @RequestParam("numeroSerie") String numeroSerie,
            @RequestParam(value = "descripcion", required = false) String descripcion,
            @RequestParam("estado") String estado,
            @RequestParam(value = "especificaciones", required = false) String especificacionesJson,
            @RequestParam(value = "componentes", required = false) String componentesJson,
            @AuthenticationPrincipal UserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();
        try {
            Usuario usuario = usuarioServicio.buscarPorCorreo(userDetails.getUsername());
            if (usuario == null || usuario.getTienda() == null) {
                response.put("success", false);
                response.put("message", "Usuario no autorizado o no tiene una tienda asignada");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            Producto productoExistente = productoServicio.obtenerProductoPorId(id);
            if (productoExistente == null) {
                response.put("success", false);
                response.put("message", "No se encontró el producto con ID: " + id);
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
            if (!productoExistente.getAlmacen().getTienda().getIdTienda().equals(usuario.getTienda().getIdTienda())) {
                response.put("success", false);
                response.put("message", "No tiene permiso para modificar este producto");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            productoExistente.setMarcaEnsambladora(marcaEnsambladora);
            productoExistente.setMarcaTarjeta(marcaTarjeta);
            productoExistente.setModelo(modelo);
            productoExistente.setNumeroSerie(numeroSerie);
            productoExistente.setDescripcion(descripcion);
            productoExistente.setEstado(estado);
            if (imagen != null && !imagen.isEmpty()) {
                try {
                    byte[] imagenBytes = imagen.getBytes();
                    productoExistente.setImagen(imagenBytes);
                } catch (IOException e) {
                    response.put("success", false);
                    response.put("message", "Error al procesar la imagen: " + e.getMessage());
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
                }
            }
            Almacen almacen = almacenServicio.obtenerAlmacenPorId(idAlmacen);
            if (almacen != null && almacen.getTienda().getIdTienda().equals(usuario.getTienda().getIdTienda())) {
                productoExistente.setAlmacen(almacen);
            }
            List<Datos> especificaciones = null;
            List<Datos> componentes = null;
            ObjectMapper objectMapper = new ObjectMapper();
            if (especificacionesJson != null && !especificacionesJson.isEmpty()) {
                try {
                    Datos[] especificacionesArr = objectMapper.readValue(especificacionesJson, Datos[].class);
                    especificaciones = java.util.Arrays.asList(especificacionesArr);
                } catch (Exception e) {
                    response.put("success", false);
                    response.put("message", "Error al procesar las especificaciones: " + e.getMessage());
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                }
            }
            if (componentesJson != null && !componentesJson.isEmpty()) {
                try {
                    Datos[] componentesArr = objectMapper.readValue(componentesJson, Datos[].class);
                    componentes = java.util.Arrays.asList(componentesArr);
                } catch (Exception e) {
                    response.put("success", false);
                    response.put("message", "Error al procesar los componentes: " + e.getMessage());
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                }
            }
            Producto productoActualizado = productoServicio.actualizarProductoConDatos(id, productoExistente, especificaciones, componentes);
            response.put("success", true);
            response.put("message", "Producto actualizado exitosamente");
            response.put("producto", convertirADTO(productoActualizado));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al actualizar el producto: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarProducto(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Verificar que el usuario esté autenticado y tenga una tienda asignada
            Usuario usuario = usuarioServicio.buscarPorCorreo(userDetails.getUsername());
            if (usuario == null || usuario.getTienda() == null) {
                response.put("success", false);
                response.put("message", "Usuario no autorizado o no tiene una tienda asignada");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            
            // Obtener el producto
            Producto producto = productoServicio.obtenerProductoPorId(id);
            if (producto == null) {
                response.put("success", false);
                response.put("message", "No se encontró el producto con ID: " + id);
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
            
            // Verificar que el producto pertenezca a la tienda del usuario
            if (!producto.getAlmacen().getTienda().getIdTienda().equals(usuario.getTienda().getIdTienda())) {
                response.put("success", false);
                response.put("message", "No tiene permiso para eliminar este producto");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            
            // Eliminar el producto (esto eliminará en cascada los datos asociados)
            productoServicio.eliminarProducto(id);
            
            response.put("success", true);
            response.put("message", "Producto eliminado exitosamente");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al eliminar el producto: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @DeleteMapping("/{id}/imagen")
    public ResponseEntity<?> eliminarImagenProductoEndpoint(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Verificar que el usuario esté autenticado y tenga una tienda asignada
            Usuario usuario = usuarioServicio.buscarPorCorreo(userDetails.getUsername());
            if (usuario == null || usuario.getTienda() == null) {
                response.put("success", false);
                response.put("message", "Usuario no autorizado o no tiene una tienda asignada");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            
            // Obtener el producto
            Producto producto = productoServicio.obtenerProductoPorId(id);
            if (producto == null) {
                response.put("success", false);
                response.put("message", "No se encontró el producto con ID: " + id);
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
            
            // Verificar que el producto pertenezca a la tienda del usuario
            if (!producto.getAlmacen().getTienda().getIdTienda().equals(usuario.getTienda().getIdTienda())) {
                response.put("success", false);
                response.put("message", "No tiene permiso para modificar este producto");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            
            // Eliminar la imagen usando el método específico
            boolean eliminada = eliminarImagenProducto(producto);
            
            if (eliminada) {
                response.put("success", true);
                response.put("message", "Imagen eliminada exitosamente");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "El producto no tiene una imagen asociada");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al eliminar la imagen: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Elimina la imagen del producto
     * @param producto El producto del cual eliminar la imagen
     * @return true si la imagen se eliminó correctamente, false en caso contrario
     */
    private boolean eliminarImagenProducto(Producto producto) {
        try {
            // Verificar si el producto tiene una imagen
            if (producto.getImagen() == null || producto.getImagen().length == 0) {
                return false;
            }
            
            // Eliminar la imagen estableciendo a null
            producto.setImagen(null);
            productoServicio.guardarProducto(producto);
            
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Método auxiliar para convertir un Producto a ProductoDTO
    private ProductoDTO convertirADTO(Producto producto) {
        if (producto == null) {
            return null;
        }
        
        // Obtener las especificaciones (tipo 1)
        List<Datos> especificaciones = producto.getDatos() != null ?
            producto.getDatos().stream()
                .filter(d -> d.getTipo() == 1)
                .collect(Collectors.toList()) :
            new ArrayList<>();
        
        // Obtener los componentes compatibles (tipo 0)
        List<Datos> componentes = producto.getDatos() != null ?
            producto.getDatos().stream()
                .filter(d -> d.getTipo() == 0)
                .collect(Collectors.toList()) :
            new ArrayList<>();
        
        // Convertir la imagen a Base64 si existe
        String imagenBase64 = null;
        if (producto.getImagen() != null && producto.getImagen().length > 0) {
            imagenBase64 = "data:image/jpeg;base64," + 
                         Base64.getEncoder().encodeToString(producto.getImagen());
        }
        
        ProductoDTO dto = new ProductoDTO();
        dto.setId_producto(producto.getId_producto());
        dto.setMarcaEnsambladora(producto.getMarcaEnsambladora());
        dto.setMarcaTarjeta(producto.getMarcaTarjeta());
        dto.setModelo(producto.getModelo());
        dto.setNumeroSerie(producto.getNumeroSerie());
        dto.setDescripcion(producto.getDescripcion());
        dto.setEstado(producto.getEstado());
        dto.setIdAlmacen(producto.getAlmacen() != null ? producto.getAlmacen().getId_almacen() : null);
        dto.setEspecificaciones(especificaciones);
        dto.setComponentes(componentes);
        dto.setImagen(producto.getImagen());
        dto.setImagenBase64(imagenBase64);
        
        return dto;
    }
}
