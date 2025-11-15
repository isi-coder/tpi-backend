package ar.edu.utnfrc.tpi.operaciones.client;

import ar.edu.utnfrc.tpi.operaciones.dtos.ContenedorDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
@RequiredArgsConstructor
public class ContenedorClient {

    private final RestClient.Builder builder;

    private RestClient client() {
        // SIEMPRE pasar por el gateway
        return builder
                .baseUrl("http://localhost:8085/mstr")
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

    private void withAuth(org.springframework.http.HttpHeaders h) {
        String token = currentToken();
        if (token != null) {
            h.setBearerAuth(token);
        }
    }

    // ===== BÚSQUEDA POR CÓDIGO =====
    public ContenedorDTO buscarPorCodigo(String codigo) {
        return client()
                .get()
                .uri("/contenedores/por-codigo/{codigo}", codigo)
                .headers(this::withAuth)
                .retrieve()
                .body(ContenedorDTO.class);
    }

    // ===== CREAR CONTENEDOR =====
    public ContenedorDTO crear(ContenedorDTO dto) {
        return client()
                .post()
                .uri("/contenedores")
                .headers(h -> {
                    withAuth(h);
                    h.setContentType(MediaType.APPLICATION_JSON);
                })
                .body(dto)
                .retrieve()
                .body(ContenedorDTO.class);
    }

    // ===== O LO TRAE, O LO CREA =====
    public ContenedorDTO getOrCreate(ContenedorDTO dto) {
        try {
            return buscarPorCodigo(dto.getCodigo());
        } catch (HttpClientErrorException.NotFound ex) {
            return crear(dto);
        }
    }

    public void validarContenedorPorCodigo(String codigo) {
        try {
            buscarPorCodigo(codigo);
        } catch (RestClientException e) {
            throw e;
        }
    }
}
