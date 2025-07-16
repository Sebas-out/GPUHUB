package com.GPUHUB.GPUHUB.servicio;

import com.GPUHUB.GPUHUB.modelo.Trabajador;
import com.GPUHUB.GPUHUB.modelo.Tienda;

import java.util.List;
import java.util.Optional;

public interface TrabajadorServicio {
    
    Trabajador guardar(Trabajador trabajador);
    
    List<Trabajador> listarTodos();
    
    List<Trabajador> listarPorTienda(Tienda tienda);
    
    List<Trabajador> listarActivosPorTienda(Tienda tienda);
    
    Trabajador buscarPorId(Long id);
    
    Optional<Trabajador> buscarPorDni(String dni);
    
    Trabajador actualizar(Trabajador trabajador);
    
    void eliminar(Long id);
    
    boolean existePorDni(String dni);
    
    boolean existePorTelefono(String telefono);
    
    Optional<Trabajador> buscarPorTelefono(String telefono);
    
    Optional<Trabajador> buscarPorCorreo(String correo);
}
