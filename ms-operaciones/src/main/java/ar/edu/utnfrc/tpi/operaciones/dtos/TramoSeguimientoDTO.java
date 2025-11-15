// ms-operaciones/src/main/java/.../dtos/TramoSeguimientoDTO.java
package ar.edu.utnfrc.tpi.operaciones.dtos;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TramoSeguimientoDTO {
    private int nro;                 // 1, 2, 3...
    private String estado;           // PENDIENTE, EN_CURSO, FINALIZADO
    private LocalDateTime inicioReal;
    private LocalDateTime finReal;
}
