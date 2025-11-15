package ar.edu.utnfrc.tpi.common.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GeoPointDTO {
    private Double lat;
    private Double lon;
}
