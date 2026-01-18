package it.unimol.microserviceuserrole.repository;

import it.unimol.microserviceuserrole.model.Role;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository per la gestione delle operazioni CRUD sui ruoli.
 * Fornisce metodi per l'accesso ai dati relativi ai ruoli nel database.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, String> {

  Optional<Role> findByName(String name);

  boolean existsByName(String name);
}