package ar.edu.utnfrc.tpi.operaciones.dtos;

import lombok.Data;

@Data
public class ClienteDTO {
    private Long id;
    private String nombre;
    private String cuit;
    private String email;
}
