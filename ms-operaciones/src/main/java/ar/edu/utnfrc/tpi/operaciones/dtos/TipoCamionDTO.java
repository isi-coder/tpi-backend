package ar.edu.utnfrc.tpi.operaciones.dtos;

import lombok.Data;

import lombok.Data;

@Data
public class TipoCamionDTO {
    private Long id;
    private String nombre;
    private Double capacidadPeso;
    private Double capacidadVolumen;
    private Double costoPorKm;
    private Double consumoLitrosPorKm;
}