package com.GPUHUB.GPUHUB.repositorio;

import com.GPUHUB.GPUHUB.modelo.Tienda;
import com.GPUHUB.GPUHUB.modelo.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepositorio extends JpaRepository<Usuario, Long> {
    
    Optional<Usuario> findByCorreo(String correo);
    
    boolean existsByCorreo(String correo);
    
    boolean existsByDni(String dni);
    
    boolean existsByTelefono(String telefono);
    
    List<Usuario> findByTienda(Tienda tienda);
    
    @Query("SELECT u FROM Usuario u WHERE u.tienda.idTienda = :idTienda")
    List<Usuario> findByTiendaId(@Param("idTienda") Long idTienda);
}
