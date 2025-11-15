package ar.edu.utnfrc.tpi.operaciones.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ar.edu.utnfrc.tpi.operaciones.entity.Ruta;

import java.util.Optional;

public interface RutaRepository extends JpaRepository<Ruta, Long> {
    Optional<Ruta> findBySolicitudId(Long solicitudId);
}
