package com.GPUHUB.GPUHUB.modelo;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "datos")
public class Datos {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_datos;
    
    @Column(nullable = false, columnDefinition = "TINYINT(1)")
    private Integer tipo; // 0 o 1
    
    @Column(nullable = false)
    private String descripcion;
    
    @Column(nullable = true)
    private String valor; // Puede ser nulo
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto", nullable = false)
    private Producto producto;
}
