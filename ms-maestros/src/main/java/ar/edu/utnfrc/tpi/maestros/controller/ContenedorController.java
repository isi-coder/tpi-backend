package ar.edu.utnfrc.tpi.maestros.controller;

import ar.edu.utnfrc.tpi.maestros.entity.Contenedor;
import ar.edu.utnfrc.tpi.maestros.repository.ContenedorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/contenedores")
@RequiredArgsConstructor
public class ContenedorController {

    private final ContenedorRepository repo;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Contenedor crear(@RequestBody Contenedor body) {
        // Si ya existe, devolvemos 400
        repo.findByCodigo(body.getCodigo())
                .ifPresent(existing -> {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Ya existe un contenedor con código " + body.getCodigo()
                    );
                });

        return repo.save(body);
    }

    @GetMapping("/por-codigo/{codigo}")
    public Contenedor porCodigo(@PathVariable("codigo") String codigo) {
        return repo.findByCodigo(codigo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
}

