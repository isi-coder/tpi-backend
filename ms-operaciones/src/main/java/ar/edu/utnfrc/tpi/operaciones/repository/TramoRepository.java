package ar.edu.utnfrc.tpi.operaciones.repository;

import ar.edu.utnfrc.tpi.operaciones.entity.Tramo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface TramoRepository extends JpaRepository<Tramo, Long> {

    // Para ver tramos asignados a un camión
    List<Tramo> findByCamionId(Long camionId);

    // Para saber cuántos tramos tiene una ruta
    long countByRutaId(Long rutaId);

    // Para saber cuántos tramos de esa ruta están en un cierto estado
    long countByRutaIdAndEstado(Long rutaId, String estado);

    List<Tramo> findByRutaIdOrderByIdAsc(Long rutaId);

    // 👇 NUEVO: todos los tramos de una solicitud
    List<Tramo> findByRutaSolicitudId(Long solicitudId);

    // 👇 NUEVO: para saber si un camión está ocupado (ASIGNADO / EN_CURSO)
    boolean existsByCamionIdAndEstadoIn(Long camionId, Collection<String> estados);

    // 👇 NUEVO: IDs de camiones ocupados
    @Query("select distinct t.camionId " +
            "from Tramo t " +
            "where t.camionId is not null and t.estado in :estados")
    List<Long> findDistinctCamionIdByEstadoIn(@Param("estados") Collection<String> estados);
}
