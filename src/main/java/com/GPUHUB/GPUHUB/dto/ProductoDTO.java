package com.GPUHUB.GPUHUB.dto;

import com.GPUHUB.GPUHUB.modelo.Datos;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductoDTO {
    private Long id_producto;
    private String marcaEnsambladora;
    private String marcaTarjeta;
    private String numeroSerie;
    private String modelo;
    private String descripcion;
    private String estado;
    private Long idAlmacen;
    private List<Datos> especificaciones = new ArrayList<>();
    private List<Datos> componentes = new ArrayList<>();
    private byte[] imagen; // Almacena la imagen como un arreglo de bytes
    private String imagenBase64; // Almacena la imagen en formato Base64 para el frontend
}
