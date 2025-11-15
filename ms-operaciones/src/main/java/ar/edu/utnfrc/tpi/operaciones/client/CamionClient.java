package ar.edu.utnfrc.tpi.operaciones.client;

import ar.edu.utnfrc.tpi.operaciones.dtos.CamionDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CamionClient {

    private final RestClient.Builder builder;

    private RestClient client() {
        return builder
                .baseUrl("http://localhost:8092")  // ms-maestros
                .build();
    }

    public CamionDTO getById(Long id) {
        try {
            return client()
                    .get()
                    .uri("/camiones/{id}", id)
                    .retrieve()
                    .body(CamionDTO.class);
        } catch (RestClientException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Camión " + id + " no existe en ms-maestros"
            );
        }
    }

    // 👇 NUEVO: listar todos los camiones de ms-maestros
    public List<CamionDTO> listAll() {
        try {
            return client()
                    .get()
                    .uri("/camiones")
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<CamionDTO>>() {});
        } catch (RestClientException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "No se pudieron obtener los camiones desde ms-maestros",
                    e
            );
        }
    }

    public boolean existsById(Long id) {
        try {
            getById(id);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
