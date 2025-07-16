package com.GPUHUB.GPUHUB.repositorio;

import com.GPUHUB.GPUHUB.modelo.Tienda;
import com.GPUHUB.GPUHUB.modelo.Trabajador;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TrabajadorRepositorio extends JpaRepository<Trabajador, Long> {
    
    Optional<Trabajador> findByDni(String dni);
    
    Optional<Trabajador> findByTelefono(String telefono);
    
    boolean existsByDni(String dni);
    
    boolean existsByTelefono(String telefono);
    
    List<Trabajador> findByTienda(Tienda tienda);
    
    List<Trabajador> findByTiendaAndEstado(Tienda tienda, Integer estado);
    
    List<Trabajador> findByRol(String rol);
    
    List<Trabajador> findByEstado(Integer estado);
    
    Optional<Trabajador> findByCorreo(String correo);
}
