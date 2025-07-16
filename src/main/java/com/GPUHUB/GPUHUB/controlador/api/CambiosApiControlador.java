package com.GPUHUB.GPUHUB.controlador.api;

import com.GPUHUB.GPUHUB.modelo.Cambios;
import com.GPUHUB.GPUHUB.modelo.Tienda;
import com.GPUHUB.GPUHUB.modelo.Trabajador;
import com.GPUHUB.GPUHUB.modelo.Usuario;
import com.GPUHUB.GPUHUB.servicio.CambiosServicio;
import com.GPUHUB.GPUHUB.servicio.TrabajadorServicio;
import com.GPUHUB.GPUHUB.servicio.UsuarioServicio;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/cambios")
public class CambiosApiControlador {

    @Autowired
    private CambiosServicio cambiosServicio;
    
    @Autowired
    private UsuarioServicio usuarioServicio;
    
    @Autowired
    private TrabajadorServicio trabajadorServicio;
    
    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping("/{id}/detalles")
    public ResponseEntity<?> obtenerDetallesCambio(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        try {
            Usuario usuario = null;
            Trabajador trabajador = null;
            Tienda tienda = null;
            try {
                usuario = usuarioServicio.buscarPorCorreo(userDetails.getUsername());
                if (usuario != null) {
                    tienda = usuario.getTienda();
                }
            } catch (Exception ex) {
                // No es usuario, intentar como trabajador
                trabajador = trabajadorServicio.buscarPorDni(userDetails.getUsername()).orElse(null);
                if (trabajador == null) {
                    trabajador = trabajadorServicio.buscarPorCorreo(userDetails.getUsername()).orElse(null);
                }
                if (trabajador != null) {
                    tienda = trabajador.getTienda();
                }
            }
            
            if (tienda == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Usuario no autorizado o no tiene una tienda asignada");
            }
            
            // Obtener el cambio
            Cambios cambio = cambiosServicio.obtenerCambioPorId(id);
            if (cambio == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Verificar que el cambio pertenezca a la tienda del usuario
            // Validar existencia de producto y almacén antes de acceder
            if (cambio.getProducto() == null || cambio.getProducto().getAlmacen() == null || cambio.getProducto().getAlmacen().getTienda() == null || !cambio.getProducto().getAlmacen().getTienda().getIdTienda().equals(tienda.getIdTienda())) {
                // Si no existe producto o almacén, permitir ver el cambio pero mostrar mensajes alternativos
                //return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tiene permiso para ver este cambio");
            }
            
            // Preparar la respuesta
            Map<String, Object> response = new HashMap<>();
            
            // Información del almacén
            if (cambio.getProducto() != null && cambio.getProducto().getAlmacen() != null) {
                response.put("infoAlmacen", cambio.getInfoAlmacen());
            } else {
                response.put("infoAlmacen", "Almacén no disponible");
            }
            
            // Información del trabajador
            response.put("trabajador", cambio.getNombreTrabajador());
            response.put("dniTrabajador", cambio.getDniTrabajador());
            
            // Información del producto
            if (cambio.getProducto() != null) {
                response.put("producto", cambio.getProducto().getMarcaEnsambladora() + " " + cambio.getProducto().getModelo());
                response.put("numeroSerie", cambio.getProducto().getNumeroSerie());
            } else {
                response.put("producto", "Producto eliminado");
                response.put("numeroSerie", "-");
            }
            
            // Tipo de cambio
            response.put("tipoCambio", cambio.getTipoCambio());
            response.put("descripcionCambio", cambio.getDescripcionCambio());
            response.put("fechaCambio", cambio.getFechaCambio().toString());
            
            // Datos anteriores y nuevos
            try {
                if (cambio.getDatosAnteriores() != null && !cambio.getDatosAnteriores().isEmpty()) {
                    JsonNode datosAnteriores = objectMapper.readTree(cambio.getDatosAnteriores());
                    response.put("datosAnteriores", datosAnteriores);
                } else {
                    response.put("datosAnteriores", null);
                }
                
                if (cambio.getDatosNuevos() != null && !cambio.getDatosNuevos().isEmpty()) {
                    JsonNode datosNuevos = objectMapper.readTree(cambio.getDatosNuevos());
                    response.put("datosNuevos", datosNuevos);
                } else {
                    response.put("datosNuevos", null);
                }
            } catch (JsonProcessingException e) {
                response.put("datosAnteriores", null);
                response.put("datosNuevos", null);
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al obtener detalles del cambio: " + e.getMessage());
        }
    }
} 