package ar.edu.utnfrc.tpi.maestros.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ar.edu.utnfrc.tpi.maestros.entity.Deposito;

public interface DepositoRepository extends JpaRepository<Deposito, Long> { }
