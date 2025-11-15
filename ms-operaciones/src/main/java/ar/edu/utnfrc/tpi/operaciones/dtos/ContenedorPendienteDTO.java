package ar.edu.utnfrc.tpi.operaciones.dtos;

import lombok.Data;

@Data
public class ContenedorPendienteDTO {

    private Long solicitudId;
    private String contenedorCodigo;
    private String clienteCuit;
    private String estadoSolicitud;

    // Ubicación “funcional” del contenedor
    // POSIBLES VALORES: EN_ORIGEN / EN_VIAJE / EN_DEPOSITO / EN_DESTINO
    private String ubicacionActual;
}
