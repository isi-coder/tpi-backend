package ar.edu.utnfrc.tpi.operaciones.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="ruta", schema="operaciones")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Ruta {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional=false) private Solicitud solicitud;
    private Double distanciaTotalKm;
    private Double duracionTotalMin;
}
