package com.GPUHUB.GPUHUB.repositorio;

import com.GPUHUB.GPUHUB.modelo.Almacen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlmacenRepositorio extends JpaRepository<Almacen, Long> {
    List<Almacen> findAll();
    List<Almacen> findByTiendaIdTienda(Long idTienda);
}
