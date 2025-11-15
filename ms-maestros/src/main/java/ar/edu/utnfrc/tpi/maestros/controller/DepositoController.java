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

    @PreAuthorize("hasAnyRole('operador','administrador')")
    @PostMapping public Deposito create(@RequestBody Deposito d){ return repo.save(d); }

    @PreAuthorize("hasAnyRole('cliente','operador','administrador')")
    @GetMapping public List<Deposito> list(){ return repo.findAll(); }

    @PreAuthorize("hasAnyRole('operador','administrador')")
    @PutMapping("/{id}")
    public Deposito update(@PathVariable("id") Long id, @RequestBody Deposito dto) {
        Deposito d = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        d.setNombre(dto.getNombre());
        d.setDireccion(dto.getDireccion());
        d.setLat(dto.getLat());
        d.setLon(dto.getLon());
        return repo.save(d);
    }

}


