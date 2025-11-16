package ar.edu.utnfrc.tpi.operaciones.controller;

import ar.edu.utnfrc.tpi.operaciones.dtos.ContenedorPendienteDTO;
import ar.edu.utnfrc.tpi.operaciones.service.SolicitudService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/contenedores")
@RequiredArgsConstructor
public class ContenedorOperadorController {

    private final SolicitudService solicitudService;

    // ==== Contenedores pendientes de entrega (OPERADOR / ADMIN) ====
    @GetMapping("/pendientes")
    @PreAuthorize("hasAnyRole('operador','admin')")
    public List<ContenedorPendienteDTO> listarPendientes() {
        return solicitudService.listarContenedoresPendientes();
    }
    // ==== Contenedores EN depósito (OPERADOR / ADMIN) ====
    @GetMapping("/en-deposito")
    @PreAuthorize("hasAnyRole('operador','admin')")
    public List<ContenedorPendienteDTO> listarEnDeposito() {
        return solicitudService.listarContenedoresEnDeposito();
    }

}
