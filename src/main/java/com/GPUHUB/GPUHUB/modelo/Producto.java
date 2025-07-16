package com.GPUHUB.GPUHUB.modelo;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "producto")
@JsonIdentityInfo(
    generator = ObjectIdGenerators.PropertyGenerator.class,
    property = "id_producto"
)
public class Producto {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_producto;
    
    @Column(name = "marca_ensambladora", nullable = false)
    private String marcaEnsambladora;
    
    @Column(name = "marca_tarjeta", nullable = false)
    private String marcaTarjeta;
    
    @Column(name = "numero_serie", nullable = false)
    private String numeroSerie;
    
    @Column(nullable = false)
    private String modelo;
    
    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] imagen; // Almacena la imagen como un arreglo de bytes
    
    @Column(columnDefinition = "TEXT")
    private String descripcion;
    
    @Column(nullable = false)
    private String estado;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_almacen", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @JsonBackReference
    private Almacen almacen;
    
    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL)
    private List<Datos> datos = new ArrayList<>();
    
    // Método para establecer la imagen
    public void setImagen(byte[] imagen) {
        this.imagen = imagen;
    }
    
    // Método para obtener la imagen
    public byte[] getImagen() {
        return this.imagen;
    }
}
