package com.GPUHUB.GPUHUB.modelo;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "almacen")
@JsonIdentityInfo(
    generator = ObjectIdGenerators.PropertyGenerator.class,
    property = "id_almacen"
)
public class Almacen {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_almacen;
    
    @Column(nullable = false)
    private String nombre;
    
    @Column(nullable = false)
    private String direccion;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tienda", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @JsonBackReference
    private Tienda tienda;
    
    @OneToMany(mappedBy = "almacen", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<Producto> productos = new ArrayList<>();
    
    // Métodos de ayuda para la relación bidireccional
    public void addProducto(Producto producto) {
        productos.add(producto);
        producto.setAlmacen(this);
    }
    
    public void removeProducto(Producto producto) {
        productos.remove(producto);
        producto.setAlmacen(null);
    }
}
