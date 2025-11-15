package ar.edu.utnfrc.tpi.maestros.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Entity
@Table(
        name = "contenedor",
        schema = "maestros",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_contenedor_codigo",
                        columnNames = "codigo"
                )
        }
)
public class Contenedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)   // 👈 importante
    private String codigo;

    private double peso;
    private double volumen;

    // getters/setters...
}

