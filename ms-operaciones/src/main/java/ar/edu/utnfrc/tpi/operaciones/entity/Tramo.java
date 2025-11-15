package ar.edu.utnfrc.tpi.operaciones.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name="tramo", schema="operaciones")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Tramo {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional=false)
    private Ruta ruta;

    @NotNull private Double desdeLat;
    @NotNull private Double desdeLon;
    @NotNull private Double hastaLat;
    @NotNull private Double hastaLon;

    private Double distanciaKm;
    private Double duracionMin;

    private Long camionId;
    private String transportistaUsername;

    private String estado; // PENDIENTE, ASIGNADO, EN_CURSO, FINALIZADO

    private LocalDateTime inicioReal;
    private LocalDateTime finReal;

    // 👇 NUEVOS
    private Long depositoId;
    private String tipoTramo;
}
