package ar.edu.utnfrc.tpi.maestros.controller;

import ar.edu.utnfrc.tpi.maestros.entity.Deposito;
import ar.edu.utnfrc.tpi.maestros.repository.DepositoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/depositos")
@RequiredArgsConstructor
public class DepositoController {

    private final DepositoRepository repo;

    // ==== Crear depósito ====
    @PreAuthorize("hasAnyRole('operador','administrador')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Deposito create(@RequestBody Deposito d) {
        return repo.save(d);
    }

    // ==== Listar todos los depósitos ====
    @PreAuthorize("hasAnyRole('cliente','operador','administrador')")
    @GetMapping
    public List<Deposito> list() {
        return repo.findAll();
    }

    // ==== Obtener depósito por id (lo usa ms-operaciones) ====
    @PreAuthorize("hasAnyRole('cliente','operador','administrador')")
    @GetMapping("/{id}")
    public Deposito getById(@PathVariable("id") Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    // ==== Actualizar depósito ====
    @PreAuthorize("hasAnyRole('operador','administrador')")
    @PutMapping("/{id}")
    public Deposito update(@PathVariable("id") Long id, @RequestBody Deposito dto) {
        Deposito d = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        d.setNombre(dto.getNombre());
        d.setDireccion(dto.getDireccion());
        d.setLat(dto.getLat());
        d.setLon(dto.getLon());
        d.setCostoDiarioEstadia(dto.getCostoDiarioEstadia()); // importante para la estadía
        return repo.save(d);
    }
}
