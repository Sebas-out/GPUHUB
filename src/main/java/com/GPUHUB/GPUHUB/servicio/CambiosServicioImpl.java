package com.GPUHUB.GPUHUB.servicio;

import com.GPUHUB.GPUHUB.modelo.Cambios;
import com.GPUHUB.GPUHUB.modelo.Producto;
import com.GPUHUB.GPUHUB.modelo.Trabajador;
import com.GPUHUB.GPUHUB.modelo.Tienda;
import com.GPUHUB.GPUHUB.repositorio.CambiosRepositorio;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CambiosServicioImpl implements CambiosServicio {

    @Autowired
    private CambiosRepositorio cambiosRepositorio;
    
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    @Transactional
    public Cambios guardarCambio(Cambios cambio) {
        return cambiosRepositorio.save(cambio);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cambios> obtenerCambiosPorTienda(Tienda tienda) {
        return cambiosRepositorio.findByTienda(tienda);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Cambios> obtenerCambiosPorTienda(Tienda tienda, Pageable pageable) {
        return cambiosRepositorio.findByTienda(tienda, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cambios> obtenerCambiosPorTrabajador(Trabajador trabajador) {
        return cambiosRepositorio.findByTrabajador(trabajador);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cambios> obtenerCambiosPorProducto(Long idProducto) {
        return cambiosRepositorio.findByProductoId(idProducto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cambios> obtenerCambiosPorTiendaYTipo(Tienda tienda, String tipoCambio) {
        return cambiosRepositorio.findByTiendaAndTipoCambio(tienda, tipoCambio);
    }

    @Override
    @Transactional(readOnly = true)
    public long contarCambiosPorTienda(Tienda tienda) {
        return cambiosRepositorio.countByTienda(tienda);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Cambios obtenerCambioPorId(Long id) {
        return cambiosRepositorio.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public void registrarCreacionProducto(Producto producto, Trabajador trabajador) {
        try {
            Cambios cambio = new Cambios();
            cambio.setTrabajador(trabajador);
            cambio.setProducto(producto);
            cambio.setTipoCambio("CREO");
            cambio.setDescripcionCambio("Creó el producto: " + producto.getMarcaEnsambladora() + " " + producto.getModelo());
            cambio.setAlmacen(producto.getAlmacen());
            
            // Guardar datos nuevos como JSON
            Map<String, Object> datosNuevos = new HashMap<>();
            datosNuevos.put("marcaEnsambladora", producto.getMarcaEnsambladora());
            datosNuevos.put("marcaTarjeta", producto.getMarcaTarjeta());
            datosNuevos.put("modelo", producto.getModelo());
            datosNuevos.put("numeroSerie", producto.getNumeroSerie());
            datosNuevos.put("descripcion", producto.getDescripcion());
            datosNuevos.put("estado", producto.getEstado());
            datosNuevos.put("almacen", producto.getAlmacen() != null ? producto.getAlmacen().getNombre() : "N/A");
            
            cambio.setDatosNuevos(objectMapper.writeValueAsString(datosNuevos));
            cambio.setDatosAnteriores("{}"); // No hay datos anteriores en creación
            
            guardarCambio(cambio);
        } catch (JsonProcessingException e) {
            // Log del error pero no interrumpir el flujo principal
            System.err.println("Error al registrar creación de producto: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void registrarEdicionProducto(Producto productoAnterior, Producto productoNuevo, Trabajador trabajador) {
        try {
            Cambios cambio = new Cambios();
            cambio.setTrabajador(trabajador);
            cambio.setProducto(productoNuevo);
            cambio.setTipoCambio("EDITO");
            cambio.setDescripcionCambio("Editó el producto: " + productoNuevo.getMarcaEnsambladora() + " " + productoNuevo.getModelo());
            cambio.setAlmacen(productoNuevo.getAlmacen());
            
            // Guardar datos anteriores como JSON
            Map<String, Object> datosAnteriores = new HashMap<>();
            datosAnteriores.put("marcaEnsambladora", productoAnterior.getMarcaEnsambladora());
            datosAnteriores.put("marcaTarjeta", productoAnterior.getMarcaTarjeta());
            datosAnteriores.put("modelo", productoAnterior.getModelo());
            datosAnteriores.put("numeroSerie", productoAnterior.getNumeroSerie());
            datosAnteriores.put("descripcion", productoAnterior.getDescripcion());
            datosAnteriores.put("estado", productoAnterior.getEstado());
            datosAnteriores.put("almacen", productoAnterior.getAlmacen() != null ? productoAnterior.getAlmacen().getNombre() : "N/A");
            
            // Guardar datos nuevos como JSON
            Map<String, Object> datosNuevos = new HashMap<>();
            datosNuevos.put("marcaEnsambladora", productoNuevo.getMarcaEnsambladora());
            datosNuevos.put("marcaTarjeta", productoNuevo.getMarcaTarjeta());
            datosNuevos.put("modelo", productoNuevo.getModelo());
            datosNuevos.put("numeroSerie", productoNuevo.getNumeroSerie());
            datosNuevos.put("descripcion", productoNuevo.getDescripcion());
            datosNuevos.put("estado", productoNuevo.getEstado());
            datosNuevos.put("almacen", productoNuevo.getAlmacen() != null ? productoNuevo.getAlmacen().getNombre() : "N/A");
            
            cambio.setDatosAnteriores(objectMapper.writeValueAsString(datosAnteriores));
            cambio.setDatosNuevos(objectMapper.writeValueAsString(datosNuevos));
            
            guardarCambio(cambio);
        } catch (JsonProcessingException e) {
            // Log del error pero no interrumpir el flujo principal
            System.err.println("Error al registrar edición de producto: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void registrarEliminacionProducto(Producto producto, Trabajador trabajador) {
        // Método deshabilitado: ya no se registran eliminaciones de productos
    }
} 