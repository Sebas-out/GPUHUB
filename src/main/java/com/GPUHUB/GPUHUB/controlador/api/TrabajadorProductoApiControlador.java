package com.GPUHUB.GPUHUB.controlador.api;

import com.GPUHUB.GPUHUB.dto.ProductoDTO;
import com.GPUHUB.GPUHUB.modelo.Almacen;
import com.GPUHUB.GPUHUB.modelo.Datos;
import com.GPUHUB.GPUHUB.modelo.Producto;
import com.GPUHUB.GPUHUB.modelo.Trabajador;
import com.GPUHUB.GPUHUB.servicio.AlmacenServicio;
import com.GPUHUB.GPUHUB.servicio.CambiosServicio;
import com.GPUHUB.GPUHUB.servicio.ProductoServicio;
import com.GPUHUB.GPUHUB.servicio.TrabajadorServicio;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/trabajador/productos")
public class TrabajadorProductoApiControlador {

    @Autowired
    private ProductoServicio productoServicio;
    
    @Autowired
    private AlmacenServicio almacenServicio;
    
    @Autowired
    private TrabajadorServicio trabajadorServicio;
    
    @Autowired
    private CambiosServicio cambiosServicio;
    
    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerProductoPorId(
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
            
            Producto producto = productoServicio.obtenerProductoPorId(id);
            if (producto == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Verificar que el producto pertenezca a un almacén de la tienda del trabajador
            if (producto.getAlmacen() == null || 
                !producto.getAlmacen().getTienda().getIdTienda().equals(trabajador.getTienda().getIdTienda())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("No tiene permiso para ver este producto");
            }
            
            // Convertir a DTO
            ProductoDTO dto = convertirProductoADTO(producto);
            return ResponseEntity.ok(dto);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al obtener el producto: " + e.getMessage());
        }
    }
    
    @PostMapping
    public ResponseEntity<?> crearProducto(
            @RequestParam("idAlmacen") Long idAlmacen,
            @RequestParam("marcaEnsambladora") String marcaEnsambladora,
            @RequestParam("marcaTarjeta") String marcaTarjeta,
            @RequestParam("modelo") String modelo,
            @RequestParam("numeroSerie") String numeroSerie,
            @RequestParam("descripcion") String descripcion,
            @RequestParam("estado") String estado,
            @RequestParam(value = "imagen", required = false) MultipartFile imagen,
            @RequestParam(value = "especificaciones", required = false) String especificacionesJson,
            @RequestParam(value = "componentes", required = false) String componentesJson,
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
            
            // Verificar que el almacén pertenezca a la tienda del trabajador
            Almacen almacen = almacenServicio.obtenerAlmacenPorId(idAlmacen);
            if (almacen == null || !almacen.getTienda().getIdTienda().equals(trabajador.getTienda().getIdTienda())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("No tiene permiso para agregar productos a este almacén");
            }
            
            // Crear el producto
            Producto producto = new Producto();
            producto.setMarcaEnsambladora(marcaEnsambladora);
            producto.setMarcaTarjeta(marcaTarjeta);
            producto.setModelo(modelo);
            producto.setNumeroSerie(numeroSerie);
            producto.setDescripcion(descripcion);
            producto.setEstado(estado);
            producto.setAlmacen(almacen);
            
            // Procesar imagen
            if (imagen != null && !imagen.isEmpty()) {
                producto.setImagen(imagen.getBytes());
            }
            
            // Procesar especificaciones y componentes si existen
            List<Datos> especificaciones = null;
            List<Datos> componentes = null;
            
            if (especificacionesJson != null && !especificacionesJson.isEmpty()) {
                especificaciones = objectMapper.readValue(especificacionesJson, 
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Datos.class));
            }
            
            if (componentesJson != null && !componentesJson.isEmpty()) {
                componentes = objectMapper.readValue(componentesJson, 
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Datos.class));
            }
            
            // Guardar el producto con todos sus datos en una sola transacción
            Producto productoGuardado = productoServicio.guardarProductoConDatos(producto, especificaciones, componentes);
            
            // Registrar el cambio
            cambiosServicio.registrarCreacionProducto(productoGuardado, trabajador);
            
            // Convertir a DTO para la respuesta
            ProductoDTO dto = convertirProductoADTO(productoGuardado);
            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al crear el producto: " + e.getMessage());
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarProducto(
            @PathVariable Long id,
            @RequestParam("idAlmacen") Long idAlmacen,
            @RequestParam("marcaEnsambladora") String marcaEnsambladora,
            @RequestParam("marcaTarjeta") String marcaTarjeta,
            @RequestParam("modelo") String modelo,
            @RequestParam("numeroSerie") String numeroSerie,
            @RequestParam("descripcion") String descripcion,
            @RequestParam("estado") String estado,
            @RequestParam(value = "imagen", required = false) MultipartFile imagen,
            @RequestParam(value = "especificaciones", required = false) String especificacionesJson,
            @RequestParam(value = "componentes", required = false) String componentesJson,
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
            
            // Verificar que el producto exista y pertenezca a la tienda del trabajador
            Producto productoExistente = productoServicio.obtenerProductoPorId(id);
            if (productoExistente == null || 
                productoExistente.getAlmacen() == null ||
                !productoExistente.getAlmacen().getTienda().getIdTienda().equals(trabajador.getTienda().getIdTienda())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("No tiene permiso para modificar este producto");
            }
            
            // Crear una copia del producto anterior para el registro de cambios
            Producto productoAnterior = new Producto();
            productoAnterior.setMarcaEnsambladora(productoExistente.getMarcaEnsambladora());
            productoAnterior.setMarcaTarjeta(productoExistente.getMarcaTarjeta());
            productoAnterior.setModelo(productoExistente.getModelo());
            productoAnterior.setNumeroSerie(productoExistente.getNumeroSerie());
            productoAnterior.setDescripcion(productoExistente.getDescripcion());
            productoAnterior.setEstado(productoExistente.getEstado());
            productoAnterior.setAlmacen(productoExistente.getAlmacen());
            
            // Verificar que el nuevo almacén pertenezca a la tienda del trabajador
            Almacen almacen = almacenServicio.obtenerAlmacenPorId(idAlmacen);
            if (almacen == null || !almacen.getTienda().getIdTienda().equals(trabajador.getTienda().getIdTienda())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("No tiene permiso para usar este almacén");
            }
            
            // Actualizar el producto
            productoExistente.setMarcaEnsambladora(marcaEnsambladora);
            productoExistente.setMarcaTarjeta(marcaTarjeta);
            productoExistente.setModelo(modelo);
            productoExistente.setNumeroSerie(numeroSerie);
            productoExistente.setDescripcion(descripcion);
            productoExistente.setEstado(estado);
            productoExistente.setAlmacen(almacen);
            
            // Procesar imagen si se proporciona
            if (imagen != null && !imagen.isEmpty()) {
                productoExistente.setImagen(imagen.getBytes());
            }
            
            // Procesar especificaciones y componentes si existen
            List<Datos> especificaciones = null;
            List<Datos> componentes = null;
            
            if (especificacionesJson != null && !especificacionesJson.isEmpty()) {
                especificaciones = objectMapper.readValue(especificacionesJson, 
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Datos.class));
            }
            
            if (componentesJson != null && !componentesJson.isEmpty()) {
                componentes = objectMapper.readValue(componentesJson, 
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Datos.class));
            }
            
            // Actualizar el producto con todos sus datos en una sola transacción
            Producto productoGuardado = productoServicio.actualizarProductoConDatos(id, productoExistente, especificaciones, componentes);
            
            // Registrar el cambio
            cambiosServicio.registrarEdicionProducto(productoAnterior, productoGuardado, trabajador);
            
            // Convertir a DTO para la respuesta
            ProductoDTO dto = convertirProductoADTO(productoGuardado);
            return ResponseEntity.ok(dto);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al actualizar el producto: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarProducto(
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
            
            // Verificar que el producto exista y pertenezca a la tienda del trabajador
            Producto producto = productoServicio.obtenerProductoPorId(id);
            if (producto == null || 
                producto.getAlmacen() == null ||
                !producto.getAlmacen().getTienda().getIdTienda().equals(trabajador.getTienda().getIdTienda())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("No tiene permiso para eliminar este producto");
            }
            
            // Registrar el cambio antes de eliminar
            cambiosServicio.registrarEliminacionProducto(producto, trabajador);
            
            productoServicio.eliminarProducto(id);
            return ResponseEntity.ok().body("Producto eliminado correctamente");
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al eliminar el producto: " + e.getMessage());
        }
    }
    
    private ProductoDTO convertirProductoADTO(Producto producto) {
        ProductoDTO dto = new ProductoDTO();
        dto.setId_producto(producto.getId_producto());
        dto.setMarcaEnsambladora(producto.getMarcaEnsambladora());
        dto.setMarcaTarjeta(producto.getMarcaTarjeta());
        dto.setModelo(producto.getModelo());
        dto.setNumeroSerie(producto.getNumeroSerie());
        dto.setDescripcion(producto.getDescripcion());
        dto.setEstado(producto.getEstado());
        dto.setIdAlmacen(producto.getAlmacen() != null ? producto.getAlmacen().getId_almacen() : null);
        
        // Convertir imagen a Base64
        if (producto.getImagen() != null && producto.getImagen().length > 0) {
            String imgBase64 = "data:image/jpeg;base64," + 
                              Base64.getEncoder().encodeToString(producto.getImagen());
            dto.setImagenBase64(imgBase64);
        }
        
        // Agregar especificaciones y componentes
        if (producto.getDatos() != null) {
            List<Datos> especificaciones = producto.getDatos().stream()
                .filter(d -> d.getTipo() == 1)
                .collect(Collectors.toList());
            List<Datos> componentes = producto.getDatos().stream()
                .filter(d -> d.getTipo() == 0)
                .collect(Collectors.toList());
            
            dto.setEspecificaciones(especificaciones);
            dto.setComponentes(componentes);
        }
        
        return dto;
    }
} 