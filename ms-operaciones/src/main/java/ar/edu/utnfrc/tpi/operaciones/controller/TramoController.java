package ar.edu.utnfrc.tpi.operaciones.controller;

import ar.edu.utnfrc.tpi.operaciones.entity.Tramo;
import ar.edu.utnfrc.tpi.operaciones.service.TramoService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ar.edu.utnfrc.tpi.operaciones.dtos.CamionDTO;

import java.util.List;

@RestController
@RequestMapping("/tramos")
@RequiredArgsConstructor
public class TramoController {

    private final TramoService tramoService;

    // ======= OPERADOR / ADMIN =======

    @GetMapping
    @PreAuthorize("hasAnyRole('operador','admin')")
    public List<Tramo> listar() {
        return tramoService.listar();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('operador','admin')")
    public Tramo getById(@PathVariable("id") Long id) {
        return tramoService.getById(id);
    }

    @PostMapping("/{id}/asignar-camion")
    @PreAuthorize("hasAnyRole('operador','admin')")
    public Tramo asignarCamion(
            @PathVariable("id") Long tramoId,
            @RequestParam("camionId") Long camionId
    ) {
        return tramoService.asignarCamion(tramoId, camionId);
    }

    // ======= CAMIONES LIBRES / OCUPADOS (OPERADOR/ADMIN) =======

    @GetMapping("/camiones/ocupados")
    @PreAuthorize("hasAnyRole('operador','admin')")
    public List<CamionDTO> camionesOcupados() {
        return tramoService.listarCamionesOcupados();
    }

    @GetMapping("/camiones/libres")
    @PreAuthorize("hasAnyRole('operador','admin')")
    public List<CamionDTO> camionesLibres() {
        return tramoService.listarCamionesLibres();
    }


    // ======= TRANSPORTISTA =======

    // Ver tramos de un camión (útil para transportista)
    @GetMapping("/por-camion/{camionId}")
    @PreAuthorize("hasAnyRole('transportista','operador','admin')")
    public List<Tramo> porCamion(@PathVariable("camionId") Long camionId) {
        return tramoService.listarPorCamion(camionId);
    }

    @PostMapping("/{id}/inicio")
    @PreAuthorize("hasRole('transportista')")
    public Tramo marcarInicio(@PathVariable("id") Long tramoId) {
        return tramoService.marcarInicio(tramoId);
    }

    @PostMapping("/{id}/fin")
    @PreAuthorize("hasRole('transportista')")
    public Tramo marcarFin(@PathVariable("id") Long tramoId) {
        return tramoService.marcarFin(tramoId);
    }
}
