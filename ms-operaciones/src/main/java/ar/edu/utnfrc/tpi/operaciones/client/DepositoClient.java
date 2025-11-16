package ar.edu.utnfrc.tpi.operaciones.client;

import ar.edu.utnfrc.tpi.operaciones.dtos.DepositoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class DepositoClient {

    private final RestClient.Builder builder;

    private RestClient client() {
        return builder
                .baseUrl("http://localhost:8092")  // igual que CamionClient y TarifaClient
                .build();
    }

    public DepositoDTO getById(Long id) {
        try {
            return client()
                    .get()
                    .uri("/depositos/{id}", id)
                    .retrieve()
                    .body(DepositoDTO.class);
        } catch (RestClientException e) {
            // si ms-maestros devuelve 404 o error, lo traduzco a "depósito inexistente"
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Depósito inexistente o error al consultar ms-maestros", e
            );
        }
    }

    public DepositoDTO[] listAll() {
        try {
            return client()
                    .get()
                    .uri("/depositos")
                    .retrieve()
                    .body(DepositoDTO[].class);
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No se pudieron listar los depósitos", e
            );
        }
    }
}
