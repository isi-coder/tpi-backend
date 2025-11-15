package ar.edu.utnfrc.tpi.maestros.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity @Table(name="deposito", schema="maestros")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Deposito {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @NotBlank private String nombre;
    @NotNull private Double lat;
    @NotNull private Double lon;
    private String direccion;

    // 👇 nuevo: costo por día de estadía
    @Column(name = "costo_diario_estadia", nullable = true) // 👈 IMPORTANTE
    private Double costoDiarioEstadia;

}
