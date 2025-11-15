package ar.edu.utnfrc.tpi.maestros.controller;

import ar.edu.utnfrc.tpi.maestros.entity.Camion;
import ar.edu.utnfrc.tpi.maestros.repository.CamionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

// ms-maestros/src/main/java/.../controller/CamionController.java
@RestController
@RequestMapping("/camiones")
@RequiredArgsConstructor
public class CamionController {

    private final CamionRepository repo;

    @GetMapping("/{id}")
    public Camion getById(@PathVariable("id") Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @GetMapping
    public List<Camion> list() {
        return repo.findAll();
    }
}

