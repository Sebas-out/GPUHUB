package com.GPUHUB.GPUHUB.repositorio;

import com.GPUHUB.GPUHUB.modelo.Tienda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TiendaRepositorio extends JpaRepository<Tienda, Long> {
    
    Optional<Tienda> findByRuc(String ruc);
    
    boolean existsByRuc(String ruc);
    
    boolean existsByNombreTienda(String nombreTienda);
    
    Optional<Tienda> findByNombreTienda(String nombreTienda);
}
