package com.GPUHUB.GPUHUB.modelo;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "cambios")
public class Cambios {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCambio;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_trabajador", nullable = true)
    private Trabajador trabajador;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto", nullable = true) // Cambiado a true para permitir null
    private Producto producto;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_almacen", nullable = true)
    private Almacen almacen;
    
    @Column(nullable = false)
    private String tipoCambio; // "CREO", "EDITO", "ELIMINO"
    
    @Column(columnDefinition = "TEXT")
    private String descripcionCambio; // Detalles del cambio realizado
    
    @Column(columnDefinition = "TEXT")
    private String datosAnteriores; // JSON con los datos anteriores (para ediciones)
    
    @Column(columnDefinition = "TEXT")
    private String datosNuevos; // JSON con los datos nuevos
    
    @CreationTimestamp
    @Column(name = "fecha_cambio", nullable = false, updatable = false)
    private LocalDateTime fechaCambio;
    
    // Método para obtener el nombre completo del trabajador
    @Transient
    public String getNombreTrabajador() {
        if (trabajador != null) {
            return trabajador.getNombres() + " " + trabajador.getApellidos();
        }
        return "Trabajador desconocido";
    }
    
    // Método para obtener el DNI del trabajador
    @Transient
    public String getDniTrabajador() {
        if (trabajador != null) {
            return trabajador.getDni();
        }
        return "";
    }
    
    // Método para obtener información del almacén
    @Transient
    public String getInfoAlmacen() {
        if (producto != null && producto.getAlmacen() != null) {
            Almacen almacen = producto.getAlmacen();
            return almacen.getNombre() + " - " + almacen.getDireccion();
        }
        return "Almacén no disponible";
    }
} 