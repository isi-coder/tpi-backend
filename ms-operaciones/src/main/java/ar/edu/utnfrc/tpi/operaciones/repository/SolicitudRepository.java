package ar.edu.utnfrc.tpi.operaciones.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ar.edu.utnfrc.tpi.operaciones.entity.Solicitud;

import java.util.List;
import java.util.Optional;

public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {

    Optional<Solicitud> findByContenedorCodigo(String contenedorCodigo);

    List<Solicitud> findByEstadoIn(List<String> estados);

}