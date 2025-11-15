package ar.edu.utnfrc.tpi.maestros.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity @Table(name="tipo_camion", schema="maestros")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TipoCamion {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @NotBlank private String nombre;

    @Column(name = "capacidad_peso")
    private Double capacidadPeso;

    @Column(name = "capacidad_volumen")
    private Double capacidadVolumen;

    @Column(name = "costo_por_km")
    private Double costoPorKm;

    // 👇 NUEVO CAMPO: PERMITIR NULLS
    @Column(name = "consumo_litros_por_km", nullable = true)
    private Double consumoLitrosPorKm;
}
