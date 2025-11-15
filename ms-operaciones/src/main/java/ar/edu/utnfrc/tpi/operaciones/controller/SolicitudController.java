package ar.edu.utnfrc.tpi.operaciones.controller;

import ar.edu.utnfrc.tpi.operaciones.dtos.*;
import ar.edu.utnfrc.tpi.operaciones.entity.Solicitud;
import ar.edu.utnfrc.tpi.operaciones.service.SolicitudService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/solicitudes")
@RequiredArgsConstructor
public class SolicitudController {

    private final SolicitudService service;

    // ==== Crear solicitud (CLIENTE) ====
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('cliente')")
    public Map<String, Object> crear(@RequestBody SolicitudCreateRequest body) {
        Solicitud s = service.crearSolicitud(body);
        return toDto(s);
    }

    // ==== Obtener por id (OPERADOR/ADMIN/CLIENTE) ====
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('cliente','operador','admin')")
    public Map<String, Object> getById(@PathVariable Long id) {
        Solicitud s = service.getById(id);
        return toDto(s);
    }

    // ==== Listar todas (OPERADOR/ADMIN) ====
    @GetMapping
    @PreAuthorize("hasAnyRole('operador','admin')")
    public List<Map<String, Object>> listar() {
        return service.listar()
                .stream()
                .map(this::toDto)
                .toList();
    }

    // ==== Seguimiento por contenedor (CLIENTE) ====
    @GetMapping("/seguimiento/{contenedorCodigo}")
    @PreAuthorize("hasRole('cliente')")
    public SeguimientoDTO seguimientoPorContenedor(
            @PathVariable("contenedorCodigo") String contenedorCodigo) {
        return service.seguimientoPorContenedor(contenedorCodigo);
    }

    // ==== Rutas tentativas (OPERADOR / ADMIN) ====
    @PostMapping("/rutas-tentativas")
    @PreAuthorize("hasAnyRole('operador','admin')")
    public List<RutaTentativaDTO> rutasTentativas(@RequestBody SolicitudCreateRequest body) {
        return service.calcularRutasTentativas(body);
    }

    // ==== Rutas tentativas CON depósito (OPERADOR / ADMIN) ====
    @PostMapping("/rutas-tentativas-deposito")
    @PreAuthorize("hasAnyRole('operador','admin')")
    public RutaTentativaMultiDTO rutasTentativasConDeposito(
            @RequestBody RutaTentativaDepositoRequest body) {

        return service.calcularRutaTentativaConDeposito(body);
    }


    // ==== Solicitudes pendientes de entrega (OPERADOR / ADMIN) ====
    @GetMapping("/pendientes")
    @PreAuthorize("hasAnyRole('operador','admin')")
    public ResponseEntity<List<Map<String, Object>>> listarPendientes() {

        List<Solicitud> pendientes = service.listarPendientes();

        List<Map<String, Object>> body = pendientes.stream()
                .map(this::toDto)
                .toList();

        return ResponseEntity.ok(body);
    }

    // ==== Mapper a DTO simple ====
    private Map<String, Object> toDto(Solicitud s) {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id", s.getId());
        dto.put("estado", s.getEstado());
        dto.put("clienteCuit", s.getClienteCuit());
        dto.put("contenedorCodigo", s.getContenedorCodigo());
        dto.put("origenLat", s.getOrigenLat());
        dto.put("origenLon", s.getOrigenLon());
        dto.put("destinoLat", s.getDestinoLat());
        dto.put("destinoLon", s.getDestinoLon());
        dto.put("distanciaEstimKm", s.getDistanciaEstimKm());
        dto.put("duracionEstimMin", s.getDuracionEstimMin());
        dto.put("costoEstimado", s.getCostoEstimado());
        dto.put("costoReal", s.getCostoReal());
        dto.put("duracionRealMin", s.getDuracionRealMin());
        return dto;
    }
}
