package com.GPUHUB.GPUHUB.repositorio;

import com.GPUHUB.GPUHUB.modelo.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ProductoRepositorio extends JpaRepository<Producto, Long> {
    // Usando consulta JPQL explícita para evitar problemas con los nombres de los campos
    @Query("SELECT p FROM Producto p WHERE p.almacen.id_almacen = :idAlmacen")
    List<Producto> findByAlmacenId(@Param("idAlmacen") Long idAlmacen);
    
    @Query("SELECT p FROM Producto p WHERE p.almacen.tienda.idTienda = :idTienda")
    List<Producto> findByAlmacenTiendaIdTienda(@Param("idTienda") Long idTienda);
    
    @Query("SELECT COUNT(p) FROM Producto p WHERE p.almacen.id_almacen = :idAlmacen")
    int countByAlmacenId(@Param("idAlmacen") Long idAlmacen);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM Producto p WHERE p.almacen.id_almacen = :idAlmacen")
    void deleteByAlmacenId(@Param("idAlmacen") Long idAlmacen);

    @Query("SELECT p FROM Producto p WHERE LOWER(p.modelo) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(p.numeroSerie) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Producto> buscarPorModeloONumeroSerie(@Param("query") String query);

    // Nuevo: buscar por marca de ensambladora
    @Query("SELECT p FROM Producto p WHERE LOWER(p.marcaEnsambladora) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Producto> buscarPorMarcaEnsambladora(@Param("query") String query);

    // Nuevo: buscar por marca de tarjeta
    @Query("SELECT p FROM Producto p WHERE LOWER(p.marcaTarjeta) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Producto> buscarPorMarcaTarjeta(@Param("query") String query);

    // Nuevo: buscar por cualquiera de los criterios
    @Query("SELECT p FROM Producto p WHERE LOWER(p.modelo) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(p.numeroSerie) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(p.marcaEnsambladora) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(p.marcaTarjeta) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Producto> buscarPorCualquierCriterio(@Param("query") String query);
}
