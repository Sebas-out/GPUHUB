package com.GPUHUB.GPUHUB.servicio;

import com.GPUHUB.GPUHUB.modelo.Datos;
import com.GPUHUB.GPUHUB.modelo.Producto;

import java.util.List;

public interface ProductoServicio {
    List<Producto> obtenerProductosPorAlmacen(Long idAlmacen);
    
    Producto obtenerProductoPorId(Long id);
    
    Producto guardarProducto(Producto producto);
    
    void eliminarProducto(Long id);
    
    Datos guardarDato(Datos dato);
    
    void eliminarDatosPorProductoId(Long productoId);
    
    List<Producto> obtenerTodosLosProductos();
    
    List<Producto> obtenerProductosPorTienda(Long idTienda);
    
    int contarProductosPorAlmacen(Long idAlmacen);
    
    void eliminarProductosPorAlmacenId(Long idAlmacen);
    
    // Nuevo método para guardar producto con datos en una sola transacción
    Producto guardarProductoConDatos(Producto producto, List<Datos> especificaciones, List<Datos> componentes);
    
    // Nuevo método para actualizar producto con datos en una sola transacción
    Producto actualizarProductoConDatos(Long idProducto, Producto producto, List<Datos> especificaciones, List<Datos> componentes);
}
