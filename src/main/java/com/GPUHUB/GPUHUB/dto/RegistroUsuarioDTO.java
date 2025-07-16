package com.GPUHUB.GPUHUB.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import jakarta.persistence.Transient;

@Data
public class RegistroUsuarioDTO {
    
    @NotBlank(message = "El DNI es obligatorio")
    @Size(min = 8, max = 8, message = "El DNI debe tener 8 dígitos")
    @Pattern(regexp = "\\d+", message = "El DNI solo debe contener números")
    private String dni;
    
    @NotBlank(message = "Los nombres son obligatorios")
    @Size(max = 100, message = "Los nombres no pueden tener más de 100 caracteres")
    private String nombres;
    
    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(max = 100, message = "Los apellidos no pueden tener más de 100 caracteres")
    private String apellidos;
    
    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "El formato del correo electrónico no es válido")
    private String correo;
    
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    private String contrasena;
    
    @NotBlank(message = "La confirmación de contraseña es obligatoria")
    @Size(min = 8, message = "La confirmación de contraseña debe tener al menos 8 caracteres")
    private String confirmarContrasena;
    
    @NotBlank(message = "El número de teléfono es obligatorio")
    @Size(min = 9, max = 9, message = "El número de teléfono debe tener 9 dígitos")
    @Pattern(regexp = "\\d+", message = "El teléfono solo debe contener números")
    private String telefono;
    
    @NotBlank(message = "La dirección es obligatoria")
    @Size(max = 255, message = "La dirección no puede tener más de 255 caracteres")
    private String direccion;
    
    @NotBlank(message = "El RUC es obligatorio")
    @Size(min = 11, max = 11, message = "El RUC debe tener 11 dígitos")
    @Pattern(regexp = "\\d+", message = "El RUC solo debe contener números")
    private String ruc;
    
    @NotBlank(message = "El nombre de la tienda es obligatorio")
    @Size(max = 100, message = "El nombre de la tienda no puede tener más de 100 caracteres")
    private String nombreTienda;
    @Transient
    private transient MultipartFile imagenTienda;

    @NotBlank(message = "El nombre de tu primo favorito es obligatorio")
    @Size(max = 100, message = "El nombre de tu primo favorito no puede tener más de 100 caracteres")
    private String primoFavorito;

    @NotBlank(message = "Tu animal favorito es obligatorio")
    @Size(max = 100, message = "El animal favorito no puede tener más de 100 caracteres")
    private String animalFavorito;

    @NotBlank(message = "Tu lugar favorito es obligatorio")
    @Size(max = 100, message = "El lugar favorito no puede tener más de 100 caracteres")
    private String lugarFavorito;
}
