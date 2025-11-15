// ms-maestros/src/main/java/ar/edu/utnfrc/tpi/maestros/controller/TarifaController.java
package ar.edu.utnfrc.tpi.maestros.controller;

import ar.edu.utnfrc.tpi.maestros.entity.Tarifa;
import ar.edu.utnfrc.tpi.maestros.repository.TarifaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tarifas")
@RequiredArgsConstructor
public class TarifaController {

    private final TarifaRepository repo;

    // ✅ Obtener la tarifa global actual (id = 1)
    @GetMapping("/actual")
    public Tarifa getTarifaActual() {
        return repo.findById(1L)
                .orElseThrow(() -> new IllegalStateException("Falta configurar la tarifa global (id=1)"));
    }

    // ✅ Crear / Actualizar la tarifa global (id = 1)
    @PutMapping("/actual")
    @ResponseStatus(HttpStatus.OK)
    public Tarifa upsertTarifaActual(@RequestBody Tarifa body) {

        Tarifa tarifa = repo.findById(1L).orElse(new Tarifa());
        tarifa.setId(1L); // forzamos que sea siempre la global

        tarifa.setDescripcion(body.getDescripcion());
        tarifa.setPrecioPorKm(body.getPrecioPorKm());
        tarifa.setValorLitroCombustible(body.getValorLitroCombustible());
        tarifa.setCargoGestionPorTramo(body.getCargoGestionPorTramo());

        return repo.save(tarifa);
    }
}
