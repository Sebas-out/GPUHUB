package com.GPUHUB.GPUHUB.servicio;

import com.GPUHUB.GPUHUB.modelo.Tienda;

import java.util.List;

public interface TiendaServicio {
    
    List<Tienda> listarTodasLasTiendas();
    
    Tienda guardarTienda(Tienda tienda);
    
    Tienda obtenerTiendaPorId(Long id);
    
    Tienda actualizarTienda(Tienda tienda);
    
    void eliminarTienda(Long id);
    
    boolean existePorRuc(String ruc);
    
    boolean existePorNombreTienda(String nombreTienda);
}
