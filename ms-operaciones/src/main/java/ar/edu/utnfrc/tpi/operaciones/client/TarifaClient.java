// ms-operaciones/src/main/java/ar/edu/utnfrc/tpi/operaciones/client/TarifaClient.java
package ar.edu.utnfrc.tpi.operaciones.client;

import ar.edu.utnfrc.tpi.operaciones.dtos.TarifaDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class TarifaClient {

    private final RestClient.Builder builder;

    private RestClient client() {
        return builder
                // 👉 directo al ms-maestros
                .baseUrl("http://localhost:8092")
                .build();
    }

    public TarifaDTO getTarifaActual() {
        try {
            return client()
                    .get()
                    .uri("/tarifas/actual")
                    .retrieve()
                    .body(TarifaDTO.class);
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "No se pudo obtener la configuración de tarifas desde ms-maestros", e
            );
        }
    }
}
