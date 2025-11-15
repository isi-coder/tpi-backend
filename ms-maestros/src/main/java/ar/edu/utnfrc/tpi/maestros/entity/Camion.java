package ar.edu.utnfrc.tpi.maestros.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity @Table(name="camion", schema="maestros")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Camion {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @NotBlank private String patente;
    @ManyToOne(optional=false) private TipoCamion tipo;
    private String transportista;
}
