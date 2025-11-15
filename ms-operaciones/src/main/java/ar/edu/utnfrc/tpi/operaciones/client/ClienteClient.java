package ar.edu.utnfrc.tpi.operaciones.client;

import ar.edu.utnfrc.tpi.operaciones.dtos.ClienteDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
public class ClienteClient {

    private final RestClient.Builder builder;

    private RestClient client() {
        return builder
                .baseUrl("http://localhost:8085/mstr") // vía gateway
                .build();
    }

    private String currentToken() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = (Jwt) jwtAuth.getPrincipal();
            return jwt.getTokenValue();
        }
        return null;
    }

    public ClienteDTO buscarPorCuit(String cuit) {
        return client()
                .get()
                .uri("/clientes/por-cuit/{cuit}", cuit)
                .headers(h -> {
                    String token = currentToken();
                    if (token != null) {
                        h.setBearerAuth(token);
                    }
                })
                .retrieve()
                .body(ClienteDTO.class);
    }

    public ClienteDTO crear(ClienteDTO dto) {
        return client()
                .post()
                .uri("/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(h -> {
                    String token = currentToken();
                    if (token != null) {
                        h.setBearerAuth(token);
                    }
                })
                .body(dto)
                .retrieve()
                .body(ClienteDTO.class);
    }

    /**
     * Devuelve un cliente existente por CUIT, o lo crea si no existe.
     */
    public ClienteDTO getOrCreate(ClienteDTO dto) {
        try {
            return buscarPorCuit(dto.getCuit());
        } catch (HttpClientErrorException.NotFound ex) {
            return crear(dto);
        }
    }
}
