package ar.edu.utnfrc.tpi.operaciones.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Entity @Table(name="solicitud", schema="operaciones")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Solicitud {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    private Double origenLat;
    private Double origenLon;
    private Double destinoLat;
    private Double destinoLon;

    private String clienteCuit;       // ★ agregar
    private String contenedorCodigo;  // ★ agregar

    private String estado; // CREADA, ASIGNADA, EN_TRAMO, FINALIZADA
    private Double distanciaEstimKm;
    private Double duracionEstimMin;
    private Double costoEstimado;
    private Double costoReal;

    @Column(name = "duracion_real_min")
    private Double duracionRealMin;

    @OneToMany(mappedBy = "solicitud")
    private List<Ruta> rutas;
}
