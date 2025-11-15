package ar.edu.utnfrc.tpi.maestros.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ar.edu.utnfrc.tpi.maestros.entity.Tarifa;

public interface TarifaRepository extends JpaRepository<Tarifa, Long> {}
