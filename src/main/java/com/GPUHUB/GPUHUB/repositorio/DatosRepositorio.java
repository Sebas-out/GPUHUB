package com.GPUHUB.GPUHUB.repositorio;

import com.GPUHUB.GPUHUB.modelo.Datos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface DatosRepositorio extends JpaRepository<Datos, Long> {
    
    @Modifying
    @Transactional
    @Query("DELETE FROM Datos d WHERE d.producto.id_producto = :productoId")
    void deleteByProductoId(@Param("productoId") Long productoId);

    @Query("SELECT d FROM Datos d WHERE d.producto.id_producto = :productoId")
    java.util.List<Datos> findByProductoId(@Param("productoId") Long productoId);
}
