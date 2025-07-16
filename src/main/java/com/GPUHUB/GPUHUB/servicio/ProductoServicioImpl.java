package com.GPUHUB.GPUHUB.servicio;

import com.GPUHUB.GPUHUB.modelo.Almacen;
import com.GPUHUB.GPUHUB.modelo.Datos;
import com.GPUHUB.GPUHUB.modelo.Producto;
import com.GPUHUB.GPUHUB.repositorio.AlmacenRepositorio;
import com.GPUHUB.GPUHUB.repositorio.CambiosRepositorio;
import com.GPUHUB.GPUHUB.repositorio.DatosRepositorio;
import com.GPUHUB.GPUHUB.repositorio.ProductoRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProductoServicioImpl implements ProductoServicio {

    @Autowired
    private ProductoRepositorio productoRepositorio;
    
    @Autowired
    private DatosRepositorio datosRepositorio;
    
    @Autowired
    private AlmacenRepositorio almacenRepositorio;
    
    @Autowired
    private CambiosRepositorio cambiosRepositorio;

    @Override
    @Transactional(readOnly = true)
    public List<Producto> obtenerProductosPorAlmacen(Long idAlmacen) {
        return productoRepositorio.findByAlmacenId(idAlmacen);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Producto obtenerProductoPorId(Long id) {
        Optional<Producto> producto = productoRepositorio.findById(id);
        return producto.orElse(null);
    }
    
    @Override
    @Transactional
    public Producto guardarProducto(Producto producto) {
        return productoRepositorio.save(producto);
    }
    
    @Override
    @Transactional
    public void eliminarProducto(Long id) {
        // Poner en null la referencia a producto en los cambios antes de eliminar
        cambiosRepositorio.setProductoNullByProductoId(id);
        productoRepositorio.deleteById(id);
    }
    
    @Override
    @Transactional
    public Datos guardarDato(Datos dato) {
        return datosRepositorio.save(dato);
    }
    
    @Override
    @Transactional
    public void eliminarDatosPorProductoId(Long productoId) {
        datosRepositorio.deleteByProductoId(productoId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Producto> obtenerTodosLosProductos() {
        return productoRepositorio.findAll();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Producto> obtenerProductosPorTienda(Long idTienda) {
        return productoRepositorio.findByAlmacenTiendaIdTienda(idTienda);
    }
    
    @Override
    @Transactional(readOnly = true)
    public int contarProductosPorAlmacen(Long idAlmacen) {
        return productoRepositorio.countByAlmacenId(idAlmacen);
    }
    
    @Override
    @Transactional
    public void eliminarProductosPorAlmacenId(Long idAlmacen) {
        // Obtenemos el almacén con sus productos
        Almacen almacen = almacenRepositorio.findById(idAlmacen).orElse(null);
        if (almacen == null) {
            return; // No hay nada que eliminar
        }
        
        // Creamos una copia de la lista para evitar ConcurrentModificationException
        List<Producto> productosAEliminar = new ArrayList<>(almacen.getProductos());
        
        // Eliminamos cada producto manualmente
        for (Producto producto : productosAEliminar) {
            eliminarProducto(producto.getId_producto());
        }
    }
    
    @Override
    @Transactional
    public Producto guardarProductoConDatos(Producto producto, List<Datos> especificaciones, List<Datos> componentes) {
        // Primero guardar el producto para obtener su ID
        Producto productoGuardado = productoRepositorio.save(producto);
        
        // Guardar especificaciones
        if (especificaciones != null) {
            for (Datos especificacion : especificaciones) {
                if (especificacion.getDescripcion() != null && !especificacion.getDescripcion().trim().isEmpty()) {
                    especificacion.setTipo(1); // Tipo 1 para especificaciones técnicas
                    especificacion.setProducto(productoGuardado);
                    datosRepositorio.save(especificacion);
                }
            }
        }
        
        // Guardar componentes
        if (componentes != null) {
            for (Datos componente : componentes) {
                if (componente.getDescripcion() != null && !componente.getDescripcion().trim().isEmpty()) {
                    componente.setTipo(0); // Tipo 0 para componentes compatibles
                    componente.setProducto(productoGuardado);
                    componente.setValor(null); // Asegurar que el valor sea nulo para componentes
                    datosRepositorio.save(componente);
                }
            }
        }
        
        return productoGuardado;
    }
    
    @Override
    @Transactional
    public Producto actualizarProductoConDatos(Long idProducto, Producto producto, List<Datos> especificaciones, List<Datos> componentes) {
        producto.setId_producto(idProducto);
        Producto productoGuardado = productoRepositorio.save(producto);

        // Obtener los datos actuales en la BD
        List<Datos> datosActuales = datosRepositorio.findByProductoId(idProducto);
        List<Long> idsNuevos = new ArrayList<>();

        // Unificar especificaciones y componentes
        List<Datos> nuevosDatos = new ArrayList<>();
        if (especificaciones != null) {
            for (Datos especificacion : especificaciones) {
                if (especificacion.getDescripcion() != null && !especificacion.getDescripcion().trim().isEmpty()) {
                    especificacion.setTipo(1);
                    especificacion.setProducto(productoGuardado);
                    Datos guardado = datosRepositorio.save(especificacion);
                    if (guardado.getId_datos() != null) {
                        idsNuevos.add(guardado.getId_datos());
                    }
                    nuevosDatos.add(guardado);
                }
            }
        }
        if (componentes != null) {
            for (Datos componente : componentes) {
                if (componente.getDescripcion() != null && !componente.getDescripcion().trim().isEmpty()) {
                    componente.setTipo(0);
                    componente.setProducto(productoGuardado);
                    componente.setValor(null);
                    Datos guardado = datosRepositorio.save(componente);
                    if (guardado.getId_datos() != null) {
                        idsNuevos.add(guardado.getId_datos());
                    }
                    nuevosDatos.add(guardado);
                }
            }
        }

        // Eliminar los datos que ya no están en la nueva lista
        for (Datos datoActual : datosActuales) {
            if (!idsNuevos.contains(datoActual.getId_datos())) {
                datosRepositorio.deleteById(datoActual.getId_datos());
            }
        }

        return productoGuardado;
    }
}
