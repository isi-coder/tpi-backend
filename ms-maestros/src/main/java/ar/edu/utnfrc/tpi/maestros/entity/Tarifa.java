package ar.edu.utnfrc.tpi.maestros.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="tarifa", schema="maestros")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Tarifa {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    private String descripcion;
    private Double precioPorKm;

    // 👇 nuevos campos de configuración global
    private Double valorLitroCombustible; // $/litro
    private Double cargoGestionPorTramo;  // $ fijo por tramo
}
