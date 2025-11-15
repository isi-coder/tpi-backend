package ar.edu.utnfrc.tpi.operaciones.dtos;

import lombok.Data;

@Data
public class ContenedorDTO {
    private Long id;
    private String codigo;
    private Double peso;
    private Double volumen;
}
