package com.GPUHUB.GPUHUB.modelo;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "trabajadores")
public class Trabajador {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idTrabajador;
    
    @NotBlank(message = "El DNI es obligatorio")
    @Size(min = 8, max = 8, message = "El DNI debe tener 8 dígitos")
    @Pattern(regexp = "\\d+", message = "El DNI solo debe contener números")
    @Column(unique = true, nullable = false, length = 8)
    private String dni;
    
    @NotBlank(message = "Los nombres son obligatorios")
    @Size(max = 100, message = "Los nombres no pueden tener más de 100 caracteres")
    @Column(nullable = false, length = 100)
    private String nombres;
    
    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(max = 100, message = "Los apellidos no pueden tener más de 100 caracteres")
    @Column(nullable = false, length = 100)
    private String apellidos;
    
    @NotBlank(message = "El número de teléfono es obligatorio")
    @Size(min = 9, max = 9, message = "El número de teléfono debe tener 9 dígitos")
    @Pattern(regexp = "\\d+", message = "El teléfono solo debe contener números")
    @Column(unique = true, nullable = false, length = 9)
    private String telefono;
    
    @NotNull(message = "La fecha de acceso es obligatoria")
    @Column(name = "fecha_acceso", nullable = false)
    private LocalDate fechaAcceso;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tienda", nullable = false)
    private Tienda tienda;
    
    @Column(nullable = false, columnDefinition = "TINYINT(1) DEFAULT 1")
    private Integer estado = 1; // 1 = Con acceso, 0 = Sin acceso
    
    @Column(nullable = false, length = 20)
    private String rol = "TRABAJADOR"; // Valor por defecto
    
    @Transient
    public String getEstadoTexto() {
        return estado != null && estado == 1 ? "Tiene acceso" : "Sin acceso";
    }
    
    public Integer getEstado() {
        return estado;
    }
    
    public void setEstado(Integer estado) {
        this.estado = estado;
    }
    
    // Método para verificar si el trabajador tiene acceso
    public boolean tieneAcceso() {
        return estado != null && estado == 1;
    }
    
    @CreationTimestamp
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;
    
    @UpdateTimestamp
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;
    
    @Column(name = "ultimo_acceso")
    private LocalDateTime ultimoAcceso;
    
    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El formato del correo no es válido")
    @Column(unique = true, nullable = false, length = 100)
    private String correo;
    
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    @Column(nullable = false, length = 255)
    private String password;
    
    // Método para actualizar la fecha de último acceso
    public void actualizarUltimoAcceso() {
        this.ultimoAcceso = LocalDateTime.now();
    }
    
    // Método para verificar si el acceso está activo según la fecha
    public boolean accesoActivo() {
        if (fechaAcceso == null) {
            return false;
        }
        LocalDate hoy = LocalDate.now();
        return !hoy.isAfter(fechaAcceso) && estado == 1;
    }
}
