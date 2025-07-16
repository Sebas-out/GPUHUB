package com.GPUHUB.GPUHUB.modelo;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

@Data
@Entity
@Table(name = "usuarios")
public class Usuario implements UserDetails {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idUsuario;
    
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
    
    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "El formato del correo electrónico no es válido")
    @Column(unique = true, nullable = false, length = 100)
    private String correo;
    
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    @Column(name = "password", nullable = false)
    private String password;
    
    @NotBlank(message = "El número de teléfono es obligatorio")
    @Size(min = 9, max = 9, message = "El número de teléfono debe tener 9 dígitos")
    @Pattern(regexp = "\\d+", message = "El teléfono solo debe contener números")
    @Column(unique = true, nullable = false, length = 9)
    private String telefono;
    
    @Column(name = "ultimo_acceso")
    private LocalDateTime ultimoAcceso;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tienda", nullable = false)
    private Tienda tienda;
    
    @CreationTimestamp
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;
    
    @UpdateTimestamp
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;
    
    @NotBlank(message = "El nombre de tu primo favorito es obligatorio")
    @Size(max = 100, message = "El nombre de tu primo favorito no puede tener más de 100 caracteres")
    @Column(name = "primo_favorito", length = 100, nullable = false)
    private String primoFavorito;

    @NotBlank(message = "Tu animal favorito es obligatorio")
    @Size(max = 100, message = "El animal favorito no puede tener más de 100 caracteres")
    @Column(name = "animal_favorito", length = 100, nullable = false)
    private String animalFavorito;

    @NotBlank(message = "Tu lugar favorito es obligatorio")
    @Size(max = 100, message = "El lugar favorito no puede tener más de 100 caracteres")
    @Column(name = "lugar_favorito", length = 100, nullable = false)
    private String lugarFavorito;
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Todos los usuarios tendrán rol USER por defecto
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }
    
    @Override
    public String getPassword() {
        return password;
    }
    
    @Override
    public String getUsername() {
        return correo;
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        return true;
    }
    

}
