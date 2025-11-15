package ar.edu.utnfrc.tpi.maestros.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ar.edu.utnfrc.tpi.maestros.entity.Camion;

public interface CamionRepository extends JpaRepository<Camion, Long> {}
