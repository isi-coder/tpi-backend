// ms-operaciones/src/main/java/.../dtos/SeguimientoDTO.java
package ar.edu.utnfrc.tpi.operaciones.dtos;

import lombok.Data;

import java.util.List;

@Data
public class SeguimientoDTO {
    private String contenedorCodigo;
    private String estadoSolicitud;

    private Double costoEstimado;
    private Double costoReal;

    private Double tiempoEstimadoMin;
    private Double tiempoRealMin;   // si querés, por ahora null

    private List<TramoSeguimientoDTO> tramos;
}
