package ar.edu.utnfrc.tpi.operaciones.dtos;

import lombok.Data;
import java.util.List;

@Data
public class RutaTentativaMultiDTO {

    private double distanciaTotalKm;
    private double duracionTotalMin;
    private double costoEstimadoTotal;

    private List<RutaTentativaTramoDTO> tramos;
}
