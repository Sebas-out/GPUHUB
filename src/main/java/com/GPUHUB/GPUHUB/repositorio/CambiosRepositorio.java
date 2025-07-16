package com.GPUHUB.GPUHUB.repositorio;

import com.GPUHUB.GPUHUB.modelo.Cambios;
import com.GPUHUB.GPUHUB.modelo.Tienda;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CambiosRepositorio extends JpaRepository<Cambios, Long> {
    
    // Obtener todos los cambios de una tienda específica
    @Query("SELECT c FROM Cambios c WHERE c.producto.almacen.tienda = :tienda ORDER BY c.fechaCambio DESC")
    List<Cambios> findByTienda(@Param("tienda") Tienda tienda);
    
    // Obtener cambios de una tienda con paginación
    @Query("SELECT c FROM Cambios c WHERE c.producto.almacen.tienda = :tienda ORDER BY c.fechaCambio DESC")
    Page<Cambios> findByTienda(@Param("tienda") Tienda tienda, Pageable pageable);
    
    // Obtener cambios de un trabajador específico
    @Query("SELECT c FROM Cambios c WHERE c.trabajador = :trabajador ORDER BY c.fechaCambio DESC")
    List<Cambios> findByTrabajador(@Param("trabajador") com.GPUHUB.GPUHUB.modelo.Trabajador trabajador);
    
    // Obtener cambios de un producto específico
    @Query("SELECT c FROM Cambios c WHERE c.producto.id_producto = :idProducto ORDER BY c.fechaCambio DESC")
    List<Cambios> findByProductoId(@Param("idProducto") Long idProducto);
    
    // Obtener cambios por tipo de cambio
    @Query("SELECT c FROM Cambios c WHERE c.producto.almacen.tienda = :tienda AND c.tipoCambio = :tipoCambio ORDER BY c.fechaCambio DESC")
    List<Cambios> findByTiendaAndTipoCambio(@Param("tienda") Tienda tienda, @Param("tipoCambio") String tipoCambio);
    
    // Contar cambios por tienda
    @Query("SELECT COUNT(c) FROM Cambios c WHERE c.producto.almacen.tienda = :tienda")
    long countByTienda(@Param("tienda") Tienda tienda);

    @Modifying
    @Transactional
    @Query("UPDATE Cambios c SET c.producto = null WHERE c.producto.id_producto = :idProducto")
    void setProductoNullByProductoId(@Param("idProducto") Long idProducto);
} 