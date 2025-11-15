package ar.edu.utnfrc.tpi.maestros.repository;

import ar.edu.utnfrc.tpi.maestros.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    Optional<Cliente> findByCuit(String cuit);
}
