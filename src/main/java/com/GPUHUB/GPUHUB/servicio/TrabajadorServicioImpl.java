package com.GPUHUB.GPUHUB.servicio;

import com.GPUHUB.GPUHUB.modelo.Tienda;
import com.GPUHUB.GPUHUB.modelo.Trabajador;
import com.GPUHUB.GPUHUB.repositorio.TrabajadorRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class TrabajadorServicioImpl implements TrabajadorServicio {

    private final TrabajadorRepositorio trabajadorRepositorio;

    @Autowired
    public TrabajadorServicioImpl(TrabajadorRepositorio trabajadorRepositorio) {
        this.trabajadorRepositorio = trabajadorRepositorio;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorDni(String dni) {
        if (dni == null || dni.trim().isEmpty()) {
            System.out.println("DNI nulo o vacío, retornando falso");
            return false;
        }
        // Normalizar el DNI (eliminar espacios y convertir a mayúsculas)
        String dniNormalizado = dni.trim().toUpperCase();
        System.out.println("Buscando DNI normalizado: '" + dniNormalizado + "'");
        
        // Usar find para obtener más información sobre el trabajador si existe
        Optional<Trabajador> trabajadorOpt = trabajadorRepositorio.findByDni(dniNormalizado);
        boolean existe = trabajadorOpt.isPresent();
        
        System.out.println("¿DNI existe en la base de datos? " + existe);
        if (existe) {
            Trabajador t = trabajadorOpt.get();
            System.out.println("Trabajador encontrado - ID: " + t.getIdTrabajador() + 
                             ", DNI: '" + t.getDni() + "'" +
                             ", Nombres: " + t.getNombres() + " " + t.getApellidos());
        }
        
        return existe;
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean existePorTelefono(String telefono) {
        return trabajadorRepositorio.existsByTelefono(telefono);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Trabajador> buscarPorTelefono(String telefono) {
        return trabajadorRepositorio.findByTelefono(telefono);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Trabajador> buscarPorCorreo(String correo) {
        return trabajadorRepositorio.findByCorreo(correo);
    }
    
    @Override
    @Transactional
    public Trabajador guardar(Trabajador trabajador) {
        // Asegurarse de que la tienda esté asignada
        if (trabajador.getTienda() == null) {
            throw new RuntimeException("No se puede guardar un trabajador sin tienda asignada");
        }
        
        // Normalizar el DNI (eliminar espacios y convertir a mayúsculas)
        if (trabajador.getDni() != null) {
            String dniNormalizado = trabajador.getDni().trim().toUpperCase();
            System.out.println("Guardando trabajador con DNI: " + dniNormalizado);
            trabajador.setDni(dniNormalizado);
        }
        
        // Guardar el trabajador
        try {
            Trabajador guardado = trabajadorRepositorio.save(trabajador);
            System.out.println("Trabajador guardado exitosamente con ID: " + guardado.getIdTrabajador());
            return guardado;
        } catch (Exception e) {
            System.err.println("Error al guardar el trabajador: " + e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Trabajador> listarTodos() {
        return trabajadorRepositorio.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Trabajador> listarPorTienda(Tienda tienda) {
        return trabajadorRepositorio.findByTienda(tienda);
    }
    

    
    @Override
    @Transactional(readOnly = true)
    public List<Trabajador> listarActivosPorTienda(Tienda tienda) {
        return trabajadorRepositorio.findByTiendaAndEstado(tienda, 1);
    }

    @Override
    @Transactional(readOnly = true)
    public Trabajador buscarPorId(Long id) {
        return trabajadorRepositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("Trabajador no encontrado con ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Trabajador> buscarPorDni(String dni) {
        return trabajadorRepositorio.findByDni(dni);
    }

    @Override
    @Transactional
    public Trabajador actualizar(Trabajador trabajador) {
        if (!trabajadorRepositorio.existsById(trabajador.getIdTrabajador())) {
            throw new RuntimeException("No se puede actualizar. Trabajador no encontrado con ID: " + trabajador.getIdTrabajador());
        }
        
        // Verificar si el DNI ya está en uso por otro trabajador
        trabajadorRepositorio.findByDni(trabajador.getDni())
                .ifPresent(t -> {
                    if (!t.getIdTrabajador().equals(trabajador.getIdTrabajador())) {
                        throw new RuntimeException("El DNI ya está en uso por otro trabajador");
                    }
                });
        
        // Verificar si el teléfono ya está en uso por otro trabajador
        trabajadorRepositorio.findByTelefono(trabajador.getTelefono())
                .ifPresent(t -> {
                    if (!t.getIdTrabajador().equals(trabajador.getIdTrabajador())) {
                        throw new RuntimeException("El teléfono ya está en uso por otro trabajador");
                    }
                });
        
        return trabajadorRepositorio.save(trabajador);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID del trabajador no puede ser nulo");
        }
        
        if (!trabajadorRepositorio.existsById(id)) {
            throw new RuntimeException("No se encontró el trabajador con ID: " + id);
        }
        
        try {
            trabajadorRepositorio.deleteById(id);
            System.out.println("Trabajador eliminado exitosamente con ID: " + id);
        } catch (Exception e) {
            System.err.println("Error al eliminar el trabajador con ID " + id + ": " + e.getMessage());
            throw new RuntimeException("Error al eliminar el trabajador: " + e.getMessage(), e);
        }
    }


}
