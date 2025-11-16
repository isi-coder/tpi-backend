package ar.edu.utnfrc.tpi.operaciones.service;

import ar.edu.utnfrc.tpi.operaciones.client.CamionClient;
import ar.edu.utnfrc.tpi.operaciones.client.ContenedorClient;
import ar.edu.utnfrc.tpi.operaciones.client.TarifaClient;
import ar.edu.utnfrc.tpi.operaciones.dtos.CamionDTO;
import ar.edu.utnfrc.tpi.operaciones.dtos.ContenedorDTO;
import ar.edu.utnfrc.tpi.operaciones.dtos.TarifaDTO;
import ar.edu.utnfrc.tpi.operaciones.dtos.TipoCamionDTO;
import ar.edu.utnfrc.tpi.operaciones.entity.Solicitud;
import ar.edu.utnfrc.tpi.operaciones.entity.Tramo;
import ar.edu.utnfrc.tpi.operaciones.repository.SolicitudRepository;
import ar.edu.utnfrc.tpi.operaciones.repository.TramoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ar.edu.utnfrc.tpi.operaciones.client.DepositoClient;
import ar.edu.utnfrc.tpi.operaciones.dtos.DepositoDTO;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Service
@RequiredArgsConstructor
public class TramoService {

    private final TramoRepository tramoRepo;
    private final SolicitudRepository solicitudRepo;
    private final CamionClient camionClient;
    private final ContenedorClient contenedorClient;
    private final TarifaClient tarifaClient;
    private final DepositoClient depositoClient;
    private final SolicitudService solicitudService;

    // ======= CONSULTAS BÁSICAS =======

    public List<Tramo> listar() {
        return tramoRepo.findAll();
    }

    public Tramo getById(Long id) {
        return tramoRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    public List<Tramo> listarPorCamion(Long camionId) {
        return tramoRepo.findByCamionId(camionId);
    }

    // ======= CAMIONES LIBRES / OCUPADOS (operador/admin) =======

    public List<CamionDTO> listarCamionesOcupados() {
        List<Long> idsOcupados = tramoRepo.findDistinctCamionIdByEstadoIn(
                List.of("ASIGNADO", "EN_CURSO")
        );

        if (idsOcupados.isEmpty()) {
            return List.of();
        }

        Set<Long> ocupadosSet = new HashSet<>(idsOcupados);
        List<CamionDTO> todos = camionClient.listAll();

        return todos.stream()
                .filter(c -> c.getId() != null && ocupadosSet.contains(c.getId()))
                .toList();
    }

    public List<CamionDTO> listarCamionesLibres() {
        List<CamionDTO> todos = camionClient.listAll();
        List<Long> idsOcupados = tramoRepo.findDistinctCamionIdByEstadoIn(
                List.of("ASIGNADO", "EN_CURSO")
        );
        Set<Long> ocupadosSet = new HashSet<>(idsOcupados);

        return todos.stream()
                .filter(c -> c.getId() != null && !ocupadosSet.contains(c.getId()))
                .toList();
    }

    // ======= ASIGNAR CAMIÓN (operador/admin) =======

    @Transactional
    public Tramo asignarCamion(Long tramoId, Long camionId) {
        Tramo tramo = getById(tramoId);

        if (!"PENDIENTE".equalsIgnoreCase(tramo.getEstado())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Sólo se puede asignar camión a tramos en estado PENDIENTE"
            );
        }

        // 1) Obtenemos el camión con su tipo
        CamionDTO camion = camionClient.getById(camionId);
        if (camion.getTipo() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El camión no tiene tipo asociado, no se puede validar capacidad"
            );
        }

        // 2) Obtenemos el contenedor de la solicitud
        String codigoContenedor = tramo.getRuta()
                .getSolicitud()
                .getContenedorCodigo();

        ContenedorDTO contenedor = contenedorClient.buscarPorCodigo(codigoContenedor);

        // 3) Validamos peso y volumen contra el tipo de camión
        TipoCamionDTO tipo = camion.getTipo();

        Double pesoMax = tipo.getCapacidadPeso();
        Double volumenMax = tipo.getCapacidadVolumen();

        if (contenedor.getPeso() != null && pesoMax != null &&
                contenedor.getPeso() > pesoMax) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    String.format(
                            "El contenedor (peso=%.2f) supera la capacidad de peso del camión (%.2f)",
                            contenedor.getPeso(), pesoMax
                    )
            );
        }

        if (contenedor.getVolumen() != null && volumenMax != null &&
                contenedor.getVolumen() > volumenMax) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    String.format(
                            "El contenedor (volumen=%.2f) supera la capacidad de volumen del camión (%.2f)",
                            contenedor.getVolumen(), volumenMax
                    )
            );
        }

        // 4) Validación extra: que el camión no esté ya asignado a otro tramo activo
        boolean camionOcupado = tramoRepo.existsByCamionIdAndEstadoIn(
                camionId,
                List.of("ASIGNADO", "EN_CURSO")
        );

        if (camionOcupado) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El camión ya está asignado a otro tramo activo"
            );
        }

        // 5) Si pasó las validaciones, asignamos
        tramo.setCamionId(camionId);
        tramo.setEstado("ASIGNADO");
        return tramoRepo.save(tramo);
    }

    // ======= INICIO DE TRAMO (rol: transportista) =======

    @Transactional
    public Tramo marcarInicio(Long tramoId) {
        Tramo tramo = getById(tramoId);

        if (tramo.getCamionId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El tramo no tiene camión asignado"
            );
        }

        if ("FINALIZADO".equalsIgnoreCase(tramo.getEstado())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El tramo ya está finalizado"
            );
        }

        if ("EN_CURSO".equalsIgnoreCase(tramo.getEstado())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El tramo ya estaba en curso"
            );
        }

        if (!"PENDIENTE".equalsIgnoreCase(tramo.getEstado()) &&
                !"ASIGNADO".equalsIgnoreCase(tramo.getEstado())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El tramo debe estar PENDIENTE o ASIGNADO para iniciarse"
            );
        }

        // Paso a EN_CURSO y guardo fecha/hora real
        tramo.setEstado("EN_CURSO");
        tramo.setInicioReal(LocalDateTime.now());
        tramoRepo.save(tramo);

        // Actualizo la solicitud: CREADA → EN_TRANSITO
        Solicitud sol = tramo.getRuta().getSolicitud();
        if ("CREADA".equalsIgnoreCase(sol.getEstado())) {
            sol.setEstado("EN_TRANSITO");
            solicitudRepo.save(sol);
        }

        return tramo;
    }

    // ======= FIN DE TRAMO (rol: transportista) =======

    @Transactional
    public Tramo marcarFin(Long tramoId) {
        Tramo tramo = getById(tramoId);

        if (!"EN_CURSO".equalsIgnoreCase(tramo.getEstado())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Sólo se puede finalizar un tramo en estado EN_CURSO"
            );
        }

        tramo.setEstado("FINALIZADO");
        tramo.setFinReal(LocalDateTime.now());
        tramoRepo.save(tramo);

        // Busco TODOS los tramos de la solicitud
        Long solicitudId = tramo.getRuta().getSolicitud().getId();
        List<Tramo> tramosSoloSolicitud = tramoRepo.findByRutaSolicitudId(solicitudId);

        boolean quedanActivos = tramosSoloSolicitud.stream()
                .anyMatch(t -> !"FINALIZADO".equalsIgnoreCase(t.getEstado()));

        // Si no queda ningún tramo activo, calculamos costo real y tiempo real total
        if (!quedanActivos) {
            Solicitud sol = tramo.getRuta().getSolicitud();
            sol.setEstado("COMPLETADA");

            // === 1) Cálculo de tarifa REAL base (transporte) ===
            TarifaDTO tarifa = tarifaClient.getTarifaActual();

            double kmTotales = tramosSoloSolicitud.stream()
                    .mapToDouble(t -> t.getDistanciaKm() != null ? t.getDistanciaKm() : 0d)
                    .sum();

            double costoReal = 0d;

            // Tomamos un camión (asumimos mismo tipo para todos los tramos de la solicitud)
            Long camionId = tramosSoloSolicitud.stream()
                    .map(Tramo::getCamionId)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(null);

            if (camionId != null) {
                CamionDTO camion = camionClient.getById(camionId);
                TipoCamionDTO tipo = camion.getTipo();

                double costoCamionPorKm = tipo.getCostoPorKm() != null ? tipo.getCostoPorKm() : 0d;
                double consumoLitrosPorKm = tipo.getConsumoLitrosPorKm() != null ? tipo.getConsumoLitrosPorKm() : 0d;
                double valorLitro = tarifa.getValorLitroCombustible() != null
                        ? tarifa.getValorLitroCombustible()
                        : 0d;

                double costoKmCamion = kmTotales * costoCamionPorKm;
                double costoCombustible = kmTotales * consumoLitrosPorKm * valorLitro;

                costoReal += costoKmCamion + costoCombustible;
            }

            // === 2) Costo de estadía en depósitos ===
            double costoEstadia = 0d;

            // Agrupo tramos por depósito (depositoId != null)
            Map<Long, List<Tramo>> tramosPorDeposito = tramosSoloSolicitud.stream()
                    .filter(t -> t.getDepositoId() != null)
                    .collect(Collectors.groupingBy(Tramo::getDepositoId));

            for (Map.Entry<Long, List<Tramo>> entry : tramosPorDeposito.entrySet()) {
                Long depositoId = entry.getKey();
                List<Tramo> tramosDep = entry.getValue().stream()
                        .sorted(Comparator.comparing(Tramo::getId))
                        .toList();

                // Necesitamos al menos un tramo de llegada y uno de salida
                if (tramosDep.size() < 2) continue;

                Tramo llegada = tramosDep.get(0);
                Tramo salida  = tramosDep.get(1);

                if (llegada.getFinReal() == null || salida.getInicioReal() == null) continue;

                long dias = ChronoUnit.DAYS.between(
                        llegada.getFinReal().toLocalDate(),
                        salida.getInicioReal().toLocalDate()
                );

                // Mínimo 1 día de estadía si hay diferencia
                if (dias < 1) dias = 1;

                DepositoDTO deposito = depositoClient.getById(depositoId);
                Double costoDia = deposito.getCostoDiarioEstadia() != null
                        ? deposito.getCostoDiarioEstadia()
                        : 0d;

                costoEstadia += dias * costoDia;
            }

            costoReal += costoEstadia;

            // === 3) Cargo de gestión por tramo ===
            long cantidadTramos = tramosSoloSolicitud.size();
            double cargoGestion = tarifa.getCargoGestionPorTramo() != null
                    ? tarifa.getCargoGestionPorTramo()
                    : 0d;

            costoReal += cantidadTramos * cargoGestion;

            sol.setCostoReal(costoReal);

            // === 4) Tiempo real total de la solicitud ===
            var inicioOpt = tramosSoloSolicitud.stream()
                    .map(Tramo::getInicioReal)
                    .filter(Objects::nonNull)
                    .min(Comparator.naturalOrder());

            var finOpt = tramosSoloSolicitud.stream()
                    .map(Tramo::getFinReal)
                    .filter(Objects::nonNull)
                    .max(Comparator.naturalOrder());

            if (inicioOpt.isPresent() && finOpt.isPresent()) {
                long minutos = Duration.between(inicioOpt.get(), finOpt.get()).toMinutes();
                sol.setDuracionRealMin((double) minutos);
            }

            solicitudRepo.save(sol);
        }

        return tramo;
    }


}
