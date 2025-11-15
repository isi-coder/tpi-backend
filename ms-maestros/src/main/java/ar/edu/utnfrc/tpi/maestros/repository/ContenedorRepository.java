package ar.edu.utnfrc.tpi.maestros.repository;

import ar.edu.utnfrc.tpi.maestros.entity.Contenedor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ContenedorRepository extends JpaRepository<Contenedor, Long> {

    Optional<Contenedor> findByCodigo(String codigo);  // 👈 que devuelva Optional
}

