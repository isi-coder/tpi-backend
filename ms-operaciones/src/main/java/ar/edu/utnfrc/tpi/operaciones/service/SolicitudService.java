package ar.edu.utnfrc.tpi.operaciones.service;

import ar.edu.utnfrc.tpi.operaciones.client.*;
import ar.edu.utnfrc.tpi.operaciones.dtos.*;
import ar.edu.utnfrc.tpi.operaciones.entity.Ruta;
import ar.edu.utnfrc.tpi.operaciones.entity.Solicitud;
import ar.edu.utnfrc.tpi.operaciones.entity.Tramo;
import ar.edu.utnfrc.tpi.operaciones.repository.RutaRepository;
import ar.edu.utnfrc.tpi.operaciones.repository.SolicitudRepository;
import ar.edu.utnfrc.tpi.operaciones.repository.TramoRepository;
import com.jayway.jsonpath.JsonPath;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class SolicitudService {

    private final SolicitudRepository solRepo;
    private final RutaRepository rutaRepo;
    private final TramoRepository tramoRepo;
    private final OsrmClient osrm;
    private final ClienteClient clienteClient;
    private final ContenedorClient contenedorClient;
    private final CamionClient camionClient;
    private final TarifaClient tarifaClient;
    private final DepositoClient depositoClient;

    // ===========================================================
    // ===============   CREAR SOLICITUD + RUTA   =================
    // ===========================================================
    @Transactional
    public Solicitud crearSolicitud(SolicitudCreateRequest req) {

        // 1) Cliente: buscar por CUIT o crearlo
        ClienteDTO cliReq = new ClienteDTO();
        cliReq.setCuit(req.getCliente().getCuit());
        cliReq.setNombre(req.getCliente().getNombre());

        ClienteDTO clienteCreado;
        try {
            clienteCreado = clienteClient.getOrCreate(cliReq);
        } catch (RestClientException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Error al validar/crear cliente en ms-maestros", ex
            );
        }

        // 2) Contenedor: buscar por código o crearlo
        ContenedorDTO contReq = new ContenedorDTO();
        contReq.setCodigo(req.getContenedor().getCodigo());
        contReq.setPeso(req.getContenedor().getPeso());
        contReq.setVolumen(req.getContenedor().getVolumen());

        ContenedorDTO contenedorCreado;
        try {
            contenedorCreado = contenedorClient.getOrCreate(contReq);
        } catch (RestClientException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Error al validar/crear contenedor en ms-maestros", ex
            );
        }

        // 3) Crear entidad Solicitud
        Solicitud sol = new Solicitud();
        sol.setEstado("PROGRAMADA");
        sol.setOrigenLat(req.getOrigenLat());
        sol.setOrigenLon(req.getOrigenLon());
        sol.setDestinoLat(req.getDestinoLat());
        sol.setDestinoLon(req.getDestinoLon());
        sol.setClienteCuit(clienteCreado.getCuit());
        sol.setContenedorCodigo(contenedorCreado.getCodigo());

        sol = solRepo.save(sol);

        // 4) Llamar a OSRM para distancia / duración
        double distanciaKm = 10.0;
        double duracionMin = 15.0;

        try {
            String json = osrm.routeRaw(
                    req.getOrigenLon(), req.getOrigenLat(),
                    req.getDestinoLon(), req.getDestinoLat()
            );
            Double distM = JsonPath.read(json, "$.routes[0].distance");
            Double durS = JsonPath.read(json, "$.routes[0].duration");

            if (distM != null) distanciaKm = distM / 1000.0;
            if (durS != null) duracionMin = durS / 60.0;

        } catch (RestClientException ignored) {
        }

        sol.setDistanciaEstimKm(distanciaKm);
        sol.setDuracionEstimMin(duracionMin);

        // 5) TARIFA ESTIMADA INTELIGENTE

        // 5.1) Buscar camiones aptos según peso/volumen del contenedor
        List<CamionDTO> camiones = camionClient.listAll();
        List<CamionDTO> aptos = camiones.stream()
                .filter(c -> c.getTipo() != null)
                .filter(c -> c.getTipo().getCapacidadPeso() != null
                        && c.getTipo().getCapacidadPeso() >= contenedorCreado.getPeso())
                .filter(c -> c.getTipo().getCapacidadVolumen() != null
                        && c.getTipo().getCapacidadVolumen() >= contenedorCreado.getVolumen())
                .toList();

        if (aptos.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No existen camiones aptos para este contenedor según peso/volumen"
            );
        }

        // 5.2) Promedios para el cálculo
        double promedioCostoKm = aptos.stream()
                .map(c -> c.getTipo().getCostoPorKm())
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(500.0); // fallback mínimo

        double promedioConsumo = aptos.stream()
                .map(c -> c.getTipo().getConsumoLitrosPorKm())
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.3); // fallback

        // 5.3) Tarifa actual
        TarifaDTO tarifa = tarifaClient.getTarifaActual();
        double valorLitro = tarifa.getValorLitroCombustible();

        // 5.4) Fórmula de Costo Estimado
        double km = distanciaKm;

        double costoAproxKm = km * promedioCostoKm;
        double costoAproxComb = km * promedioConsumo * valorLitro;
        double costoGestion = tarifa.getCargoGestionPorTramo() != null
                ? tarifa.getCargoGestionPorTramo()
                : 0;

        double costoEstimado = costoAproxKm + costoAproxComb + costoGestion;

        sol.setCostoEstimado(costoEstimado);
        sol.setDuracionEstimMin(duracionMin);

        sol = solRepo.save(sol);

        // 6) Crear Ruta
        Ruta ruta = rutaRepo.save(Ruta.builder()
                .solicitud(sol)
                .distanciaTotalKm(distanciaKm)
                .duracionTotalMin(duracionMin)
                .build());

        // 7) Crear Tramo origen-destino
        tramoRepo.save(Tramo.builder()
                .ruta(ruta)
                .desdeLat(req.getOrigenLat())
                .desdeLon(req.getOrigenLon())
                .hastaLat(req.getDestinoLat())
                .hastaLon(req.getDestinoLon())
                .distanciaKm(distanciaKm)
                .duracionMin(duracionMin)
                .estado("PENDIENTE")
                .build());

        return sol;
    }

    // ===========================================================
    // ========   RUTAS TENTATIVAS (Operador/Admin)   ============
    // ===========================================================
    @Transactional(readOnly = true)
    public List<RutaTentativaDTO> calcularRutasTentativas(SolicitudCreateRequest req) {

        // 1) Llamar a OSRM para distancia / duración (sin persistir nada)
        double distanciaKm = 10.0;
        double duracionMin = 15.0;

        try {
            String json = osrm.routeRaw(
                    req.getOrigenLon(), req.getOrigenLat(),
                    req.getDestinoLon(), req.getDestinoLat()
            );
            Double distM = JsonPath.read(json, "$.routes[0].distance");
            Double durS = JsonPath.read(json, "$.routes[0].duration");

            if (distM != null) distanciaKm = distM / 1000.0;
            if (durS != null) duracionMin = durS / 60.0;

        } catch (RestClientException ignored) {
        }

        // 2) Buscar camiones aptos según peso/volumen del contenedor
        Double peso = req.getContenedor().getPeso();
        Double volumen = req.getContenedor().getVolumen();

        List<CamionDTO> camiones = camionClient.listAll();
        List<CamionDTO> aptos = camiones.stream()
                .filter(c -> c.getTipo() != null)
                .filter(c -> c.getTipo().getCapacidadPeso() != null
                        && peso != null
                        && c.getTipo().getCapacidadPeso() >= peso)
                .filter(c -> c.getTipo().getCapacidadVolumen() != null
                        && volumen != null
                        && c.getTipo().getCapacidadVolumen() >= volumen)
                .toList();

        if (aptos.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No existen camiones aptos para este contenedor según peso/volumen"
            );
        }

        // 3) Promedios para el cálculo
        double promedioCostoKm = aptos.stream()
                .map(c -> c.getTipo().getCostoPorKm())
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(500.0); // fallback mínimo

        double promedioConsumo = aptos.stream()
                .map(c -> c.getTipo().getConsumoLitrosPorKm())
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.3); // fallback

        // 4) Tarifa actual
        TarifaDTO tarifa = tarifaClient.getTarifaActual();
        double valorLitro = tarifa.getValorLitroCombustible();

        // 5) Cálculo de costo estimado para la ruta directa
        double km = distanciaKm;

        double costoAproxKm = km * promedioCostoKm;
        double costoAproxComb = km * promedioConsumo * valorLitro;
        double costoGestion = tarifa.getCargoGestionPorTramo() != null
                ? tarifa.getCargoGestionPorTramo()
                : 0;

        double costoEstimadoTotal = costoAproxKm + costoAproxComb + costoGestion;

        // 6) Armar DTO de tramo
        RutaTentativaTramoDTO tramo = new RutaTentativaTramoDTO();
        tramo.setNro(1);
        tramo.setDesdeLat(req.getOrigenLat());
        tramo.setDesdeLon(req.getOrigenLon());
        tramo.setHastaLat(req.getDestinoLat());
        tramo.setHastaLon(req.getDestinoLon());
        tramo.setDistanciaKm(distanciaKm);
        tramo.setDuracionMin(duracionMin);
        tramo.setCostoEstimadoTramo(costoEstimadoTotal); // 1 solo tramo

        // 7) Armar DTO de ruta
        RutaTentativaDTO rutaDto = new RutaTentativaDTO();
        rutaDto.setDistanciaTotalKm(distanciaKm);
        rutaDto.setDuracionTotalMin(duracionMin);
        rutaDto.setCostoEstimadoTotal(costoEstimadoTotal);
        rutaDto.setTramos(List.of(tramo));

        // Por ahora devolvemos una única ruta tentativa (directa)
        return List.of(rutaDto);
    }

    @Transactional(readOnly = true)
    public RutaTentativaMultiDTO calcularRutaTentativaConDeposito(RutaTentativaDepositoRequest req) {

        if (req.getDepositoId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Falta depositoId");
        }

        // 1) Obtener depósito desde ms-maestros
        DepositoDTO dep = depositoClient.getById(req.getDepositoId());
        if (dep == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Depósito inexistente");
        }

        // DTO respuesta final
        RutaTentativaMultiDTO resp = new RutaTentativaMultiDTO();
        List<RutaTentativaTramoDTO> tramos = new ArrayList<>();

        double distanciaTotal = 0.0;
        double duracionTotal = 0.0;
        double costoTotal = 0.0;

        // =======================================================
        // TRAMO 1: ORIGEN → DEPOSITO
        // =======================================================
        double d1 = 0, t1 = 0;

        try {
            String json = osrm.routeRaw(
                    req.getOrigenLon(), req.getOrigenLat(),
                    dep.getLon(), dep.getLat()
            );
            Double distM = JsonPath.read(json, "$.routes[0].distance");
            Double durS = JsonPath.read(json, "$.routes[0].duration");

            if (distM != null) d1 = distM / 1000.0;
            if (durS != null) t1 = durS / 60.0;

        } catch (Exception ignored) { }

        double costo1 = calcularCostoEstimado(d1);

        RutaTentativaTramoDTO tramo1 = new RutaTentativaTramoDTO();
        tramo1.setNro(1);
        tramo1.setDesdeLat(req.getOrigenLat());
        tramo1.setDesdeLon(req.getOrigenLon());
        tramo1.setHastaLat(dep.getLat());
        tramo1.setHastaLon(dep.getLon());
        tramo1.setDistanciaKm(d1);
        tramo1.setDuracionMin(t1);
        tramo1.setCostoEstimadoTramo(costo1);

        tramos.add(tramo1);
        distanciaTotal += d1;
        duracionTotal += t1;
        costoTotal += costo1;

        // =======================================================
        // TRAMO 2: DEPOSITO → DESTINO
        // =======================================================
        double d2 = 0, t2 = 0;

        try {
            String json = osrm.routeRaw(
                    dep.getLon(), dep.getLat(),
                    req.getDestinoLon(), req.getDestinoLat()
            );
            Double distM = JsonPath.read(json, "$.routes[0].distance");
            Double durS = JsonPath.read(json, "$.routes[0].duration");

            if (distM != null) d2 = distM / 1000.0;
            if (durS != null) t2 = durS / 60.0;

        } catch (Exception ignored) { }

        double costo2 = calcularCostoEstimado(d2);

        RutaTentativaTramoDTO tramo2 = new RutaTentativaTramoDTO();
        tramo2.setNro(2);
        tramo2.setDesdeLat(dep.getLat());
        tramo2.setDesdeLon(dep.getLon());
        tramo2.setHastaLat(req.getDestinoLat());
        tramo2.setHastaLon(req.getDestinoLon());
        tramo2.setDistanciaKm(d2);
        tramo2.setDuracionMin(t2);
        tramo2.setCostoEstimadoTramo(costo2);

        tramos.add(tramo2);
        distanciaTotal += d2;
        duracionTotal += t2;
        costoTotal += costo2;

        // =======================================================
        // ARMAR RESPUESTA FINAL
        // =======================================================
        resp.setDistanciaTotalKm(distanciaTotal);
        resp.setDuracionTotalMin(duracionTotal);
        resp.setCostoEstimadoTotal(costoTotal);
        resp.setTramos(tramos);

        return resp;
    }

    private double calcularCostoEstimado(double km) {

        // 1) Buscar camiones aptos (no se usa el contenedor aún, promedio general)
        List<CamionDTO> camiones = camionClient.listAll();

        double promedioCostoKm = camiones.stream()
                .filter(c -> c.getTipo() != null)
                .map(c -> c.getTipo().getCostoPorKm())
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(500.0);

        double promedioConsumo = camiones.stream()
                .filter(c -> c.getTipo() != null)
                .map(c -> c.getTipo().getConsumoLitrosPorKm())
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.30);

        TarifaDTO tarifa = tarifaClient.getTarifaActual();

        double costoKm = km * promedioCostoKm;
        double costoComb = km * promedioConsumo * tarifa.getValorLitroCombustible();
        double gestion = tarifa.getCargoGestionPorTramo();

        return costoKm + costoComb + gestion;
    }



    // ===========================================================
    // ============   SOLICITUDES PENDIENTES (OPERADOR) ==========
    // ===========================================================
    public List<Solicitud> listarPendientes() {
        List<String> estadosPendientes = List.of("CREADA", "PROGRAMADA", "EN_TRANSITO");
        return solRepo.findByEstadoIn(estadosPendientes);
    }

    // ===========================================================
    // =========== CONTENEDORES PENDIENTES (OPERADOR) ============
    // ===========================================================

    public List<ContenedorPendienteDTO> listarContenedoresPendientes() {

        // Estados que consideramos "pendientes de entrega"
        List<String> estadosPendientes = List.of("CREADA", "PROGRAMADA", "EN_TRANSITO");

        List<Solicitud> solicitudes = solRepo.findByEstadoIn(estadosPendientes);

        List<ContenedorPendienteDTO> resultado = new ArrayList<>();

        for (Solicitud sol : solicitudes) {

            Ruta ruta = rutaRepo.findBySolicitudId(sol.getId())
                    .orElse(null);

            List<Tramo> tramos = ruta != null
                    ? tramoRepo.findByRutaIdOrderByIdAsc(ruta.getId())
                    : List.of();

            String ubicacion = calcularUbicacionContenedor(sol, tramos);

            ContenedorPendienteDTO dto = new ContenedorPendienteDTO();
            dto.setSolicitudId(sol.getId());
            dto.setContenedorCodigo(sol.getContenedorCodigo());
            dto.setClienteCuit(sol.getClienteCuit());
            dto.setEstadoSolicitud(sol.getEstado());
            dto.setUbicacionActual(ubicacion);

            resultado.add(dto);
        }

        return resultado;
    }

    /**
     * Determina la ubicación "lógica" del contenedor según los tramos.
     * EN_ORIGEN / EN_VIAJE / EN_DEPOSITO / EN_DESTINO
     */
    /**
     * Determina la ubicación "lógica" del contenedor según los tramos.
     * Por ahora: EN_ORIGEN / EN_VIAJE / EN_DESTINO
     * (cuando agreguemos depósitos sumamos EN_DEPOSITO).
     */
    private String calcularUbicacionContenedor(Solicitud sol, List<Tramo> tramos) {

        if (tramos == null || tramos.isEmpty()) {
            // No hay tramos cargados aún: asumimos que sigue en origen
            return "EN_ORIGEN";
        }

        boolean hayEnCurso = tramos.stream()
                .anyMatch(t -> "EN_CURSO".equalsIgnoreCase(t.getEstado()));

        if (hayEnCurso) {
            return "EN_VIAJE";
        }

        boolean hayFinalizado = tramos.stream()
                .anyMatch(t -> "FINALIZADO".equalsIgnoreCase(t.getEstado()));

        if (!hayFinalizado) {
            // Hay tramos pero ninguno finalizado y ninguno en curso ⇒ aún en origen
            return "EN_ORIGEN";
        }

        // Si la solicitud ya está completada → destino
        if ("COMPLETADA".equalsIgnoreCase(sol.getEstado())
                || "ENTREGADA".equalsIgnoreCase(sol.getEstado())) {
            return "EN_DESTINO";
        }

        // Hay tramos finalizados pero la solicitud aún no se cerró formalmente.
        // Interpretamos como que ya llegó a destino.
        return "EN_DESTINO";
    }


    // ===========================================================
    // ===================   SEGUIMIENTO   ========================
    // ===========================================================
    public SeguimientoDTO seguimientoPorContenedor(String contenedorCodigo) {

        // 1) Buscar solicitud
        Solicitud sol = solRepo.findByContenedorCodigo(contenedorCodigo)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No existe solicitud para contenedor " + contenedorCodigo
                ));

        // 2) Buscar ruta
        Ruta ruta = rutaRepo.findBySolicitudId(sol.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Solicitud sin ruta asociada"
                ));

        List<Tramo> tramos = tramoRepo.findByRutaIdOrderByIdAsc(ruta.getId());

        // 3) Armar DTO
        SeguimientoDTO dto = new SeguimientoDTO();
        dto.setContenedorCodigo(sol.getContenedorCodigo());
        dto.setEstadoSolicitud(sol.getEstado());
        dto.setCostoEstimado(sol.getCostoEstimado());
        dto.setCostoReal(sol.getCostoReal());
        dto.setTiempoEstimadoMin(sol.getDuracionEstimMin());
        dto.setTiempoRealMin(sol.getDuracionRealMin());

        List<TramoSeguimientoDTO> lista = new ArrayList<>();
        int nro = 1;

        for (Tramo t : tramos) {
            TramoSeguimientoDTO td = new TramoSeguimientoDTO();
            td.setNro(nro++);
            td.setEstado(t.getEstado());
            td.setInicioReal(t.getInicioReal());
            td.setFinReal(t.getFinReal());
            lista.add(td);
        }

        dto.setTramos(lista);
        return dto;
    }

    // ===========================================================
    // ======================   CONSULTAS   =======================
    // ===========================================================
    public Solicitud getById(Long id) {
        return solRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    public List<Solicitud> listar() {
        return solRepo.findAll();
    }
}
