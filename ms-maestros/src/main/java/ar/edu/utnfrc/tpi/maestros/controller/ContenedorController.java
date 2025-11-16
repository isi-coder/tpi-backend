package ar.edu.utnfrc.tpi.maestros.controller;

import ar.edu.utnfrc.tpi.maestros.entity.Contenedor;
import ar.edu.utnfrc.tpi.maestros.repository.ContenedorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/contenedores")
@RequiredArgsConstructor
public class ContenedorController {

    private final ContenedorRepository repo;

    // ==== Crear contenedor ====
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Contenedor crear(@RequestBody Contenedor body) {

        // verificar código único
        repo.findByCodigo(body.getCodigo())
                .ifPresent(existing -> {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Ya existe un contenedor con código " + body.getCodigo()
                    );
                });

        return repo.save(body);
    }

    // ==== Listar todos los contenedores ====
    @GetMapping
    public List<Contenedor> listar() {
        return repo.findAll();
    }

    // ==== Obtener por id (si lo necesitás) ====
    @GetMapping("/{id}")
    public Contenedor porId(@PathVariable("id") Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    // ==== Obtener por código ====
    @GetMapping("/por-codigo/{codigo}")
    public Contenedor porCodigo(@PathVariable("codigo") String codigo) {
        return repo.findByCodigo(codigo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
}


