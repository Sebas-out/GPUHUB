package com.GPUHUB.GPUHUB.servicio;

import com.GPUHUB.GPUHUB.modelo.Cambios;
import com.GPUHUB.GPUHUB.modelo.Producto;
import com.GPUHUB.GPUHUB.modelo.Trabajador;
import com.GPUHUB.GPUHUB.modelo.Tienda;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CambiosServicio {
    
    Cambios guardarCambio(Cambios cambio);
    
    List<Cambios> obtenerCambiosPorTienda(Tienda tienda);
    
    Page<Cambios> obtenerCambiosPorTienda(Tienda tienda, Pageable pageable);
    
    List<Cambios> obtenerCambiosPorTrabajador(Trabajador trabajador);
    
    List<Cambios> obtenerCambiosPorProducto(Long idProducto);
    
    List<Cambios> obtenerCambiosPorTiendaYTipo(Tienda tienda, String tipoCambio);
    
    long contarCambiosPorTienda(Tienda tienda);
    
    // Métodos para registrar cambios automáticamente
    void registrarCreacionProducto(Producto producto, Trabajador trabajador);
    
    void registrarEdicionProducto(Producto productoAnterior, Producto productoNuevo, Trabajador trabajador);
    
    void registrarEliminacionProducto(Producto producto, Trabajador trabajador);
    
    // Método para obtener un cambio por ID
    Cambios obtenerCambioPorId(Long id);
} 