package it.unimol.microserviceuserrole.repository;

import it.unimol.microserviceuserrole.model.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository per la gestione delle operazioni CRUD sugli utenti.
 * Fornisce metodi per l'accesso ai dati relativi agli utenti nel database.
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {

  Optional<User> findByUsername(String username);

  Optional<User> findByEmail(String email);

  @Query("SELECT u FROM User u WHERE u.role.name = :roleName")
  List<User> findByRoleName(@Param("roleName") String roleName);

  @Query("SELECT u FROM User u WHERE u.role.id = :roleId")
  List<User> findByRoleId(@Param("roleId") String roleId);

  boolean existsByUsername(String username);

  boolean existsByEmail(String email);

  @Query("SELECT COUNT(u) FROM User u WHERE u.role.name = 'SUPER_ADMIN'")
  long countSuperAdmins();
}