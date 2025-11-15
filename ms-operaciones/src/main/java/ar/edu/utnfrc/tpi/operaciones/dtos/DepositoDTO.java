package ar.edu.utnfrc.tpi.operaciones.dtos;

import lombok.Data;

@Data
public class DepositoDTO {
    private Long id;
    private String nombre;
    private Double lat;
    private Double lon;
    private String direccion;
    private Double costoDiarioEstadia;
}
