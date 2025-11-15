package ar.edu.utnfrc.tpi.operaciones.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CamionDTO {
    private Long id;
    private String patente;
    private String transportista; // muy importante para el rol transportista
    private TipoCamionDTO tipo;
}
