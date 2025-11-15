package ar.edu.utnfrc.tpi.operaciones.dtos;

import lombok.Data;

@Data
public class RutaTentativaDepositoRequest {

    private Double origenLat;
    private Double origenLon;

    private Double destinoLat;
    private Double destinoLon;

    private Long depositoId;
}
