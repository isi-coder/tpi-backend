// ms-operaciones/src/main/java/ar/edu/utnfrc/tpi/operaciones/dtos/TarifaDTO.java
package ar.edu.utnfrc.tpi.operaciones.dtos;

import lombok.Data;

@Data
public class TarifaDTO {
    private Long id;
    private String descripcion;
    private Double precioPorKm;
    private Double valorLitroCombustible;
    private Double cargoGestionPorTramo;
}
