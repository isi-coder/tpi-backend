package ar.edu.utnfrc.tpi.maestros.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ar.edu.utnfrc.tpi.maestros.entity.TipoCamion;

public interface TipoCamionRepository extends JpaRepository<TipoCamion, Long> {}
