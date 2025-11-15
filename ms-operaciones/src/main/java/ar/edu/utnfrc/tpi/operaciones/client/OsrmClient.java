// ar.edu.utnfrc.tpi.operaciones.client.OsrmClient.java
package ar.edu.utnfrc.tpi.operaciones.client;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
public class OsrmClient {

    @Value("${osrm.base-url:http://localhost:5000}")
    String baseUrl;

    private final RestClient client;

    public String routeRaw(double lon1, double lat1, double lon2, double lat2) {
        String url = String.format(
                "%s/route/v1/driving/%.6f,%.6f;%.6f,%.6f?overview=false",
                baseUrl, lon1, lat1, lon2, lat2
        );
        return client.get()
                .uri(url)
                .retrieve()
                .body(String.class);
    }

}
