package com.GPUHUB.GPUHUB.servicio;

import com.GPUHUB.GPUHUB.modelo.Almacen;
import com.GPUHUB.GPUHUB.modelo.Producto;
import com.GPUHUB.GPUHUB.repositorio.AlmacenRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AlmacenServicio {

    @Autowired
    private AlmacenRepositorio almacenRepositorio;
    
    @Autowired
    private ProductoServicio productoServicio;

    public List<Almacen> obtenerTodosLosAlmacenes() {
        return almacenRepositorio.findAll();
    }
    
    public List<Almacen> obtenerAlmacenesPorTienda(Long idTienda) {
        return almacenRepositorio.findByTiendaIdTienda(idTienda);
    }
    
    public Almacen obtenerAlmacenPorId(Long id) {
        Optional<Almacen> almacen = almacenRepositorio.findById(id);
        return almacen.orElse(null);
    }
    
    public Almacen guardarAlmacen(Almacen almacen) {
        return almacenRepositorio.save(almacen);
    }
    
    @Transactional
    public void eliminarAlmacen(Long id) {
        Almacen almacen = obtenerAlmacenPorId(id);
        if (almacen == null) {
            throw new RuntimeException("Almacén no encontrado con ID: " + id);
        }
        // Eliminar el almacén directamente, JPA y la base de datos harán el cascade
        almacenRepositorio.delete(almacen);
    }
}
