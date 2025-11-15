package ar.edu.utnfrc.tpi.maestros.controller;

import ar.edu.utnfrc.tpi.maestros.entity.Cliente;
import ar.edu.utnfrc.tpi.maestros.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteRepository repo;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Cliente crear(@RequestBody Cliente body) {
        // Podés agregar validaciones de CUIT único si querés
        return repo.save(body);
    }

    @GetMapping("/por-cuit/{cuit}")
    public Cliente porCuit(@PathVariable("cuit") String cuit) {
        return repo.findByCuit(cuit)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
}
