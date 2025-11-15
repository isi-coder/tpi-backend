package ar.edu.utnfrc.tpi.operaciones.dtos;

import lombok.Data;

@Data
public class RutaTentativaTramoDTO {

    private int nro;

    private double desdeLat;
    private double desdeLon;

    private double hastaLat;
    private double hastaLon;

    private Double distanciaKm;
    private Double duracionMin;
    private Double costoEstimadoTramo;
}
