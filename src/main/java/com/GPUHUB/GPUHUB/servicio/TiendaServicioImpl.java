package com.GPUHUB.GPUHUB.servicio;

import com.GPUHUB.GPUHUB.modelo.Tienda;
import com.GPUHUB.GPUHUB.repositorio.TiendaRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TiendaServicioImpl implements TiendaServicio {

    private final TiendaRepositorio tiendaRepositorio;

    @Autowired
    public TiendaServicioImpl(TiendaRepositorio tiendaRepositorio) {
        this.tiendaRepositorio = tiendaRepositorio;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Tienda> listarTodasLasTiendas() {
        return tiendaRepositorio.findAll();
    }

    @Override
    public Tienda guardarTienda(Tienda tienda) {
        if (existePorRuc(tienda.getRuc())) {
            throw new RuntimeException("Ya existe una tienda con el RUC: " + tienda.getRuc());
        }
        if (existePorNombreTienda(tienda.getNombreTienda())) {
            throw new RuntimeException("Ya existe una tienda con el nombre: " + tienda.getNombreTienda());
        }
        return tiendaRepositorio.save(tienda);
    }

    @Override
    @Transactional(readOnly = true)
    public Tienda obtenerTiendaPorId(Long id) {
        return tiendaRepositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("Tienda no encontrada con ID: " + id));
    }

    @Override
    public Tienda actualizarTienda(Tienda tienda) {
        Tienda tiendaExistente = obtenerTiendaPorId(tienda.getIdTienda());
        
        if (!tiendaExistente.getRuc().equals(tienda.getRuc()) && existePorRuc(tienda.getRuc())) {
            throw new RuntimeException("Ya existe una tienda con el RUC: " + tienda.getRuc());
        }
        
        if (!tiendaExistente.getNombreTienda().equals(tienda.getNombreTienda()) && 
            existePorNombreTienda(tienda.getNombreTienda())) {
            throw new RuntimeException("Ya existe una tienda con el nombre: " + tienda.getNombreTienda());
        }
        
        tiendaExistente.setNombreTienda(tienda.getNombreTienda());
        tiendaExistente.setRuc(tienda.getRuc());
        tiendaExistente.setDireccion(tienda.getDireccion());
        
        return tiendaRepositorio.save(tiendaExistente);
    }

    @Override
    public void eliminarTienda(Long id) {
        Tienda tienda = obtenerTiendaPorId(id);
        if (!tienda.getUsuarios().isEmpty()) {
            throw new RuntimeException("No se puede eliminar la tienda porque tiene usuarios asociados");
        }
        tiendaRepositorio.delete(tienda);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorRuc(String ruc) {
        return tiendaRepositorio.existsByRuc(ruc);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorNombreTienda(String nombreTienda) {
        return tiendaRepositorio.existsByNombreTienda(nombreTienda);
    }
}
