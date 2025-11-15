package ar.edu.utnfrc.tpi.operaciones.dtos;

import lombok.Data;

@Data
public class SolicitudCreateRequest {

    private ClienteDTO cliente;
    private ContenedorDTO contenedor;

    private Double origenLat;
    private Double origenLon;
    private Double destinoLat;
    private Double destinoLon;

    private Long origenDepositoId;   // opcionales por ahora
    private Long destinoDepositoId;
}
