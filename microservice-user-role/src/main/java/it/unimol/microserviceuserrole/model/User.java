package it.unimol.microserviceuserrole.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;

/**
 * Entità JPA che rappresenta un utente nel sistema.
 * Contiene le informazioni personali dell'utente, le credenziali
 * e il ruolo assegnato.
 */
@Entity
@Table(name = "users")
public class User {

  @Id
  private String id;

  @Column(unique = true, nullable = false)
  private String username;

  @Column(unique = true, nullable = false)
  private String email;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String surname;

  @Column(nullable = false)
  private String password;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "last_login")
  private LocalDateTime lastLogin;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "role_id")
  private Role role;

  /**
   * Costruttore vuoto richiesto da JPA.
   * Inizializza automaticamente la data di creazione.
   */
  public User() {
    this.createdAt = LocalDateTime.now(ZoneId.systemDefault());
  }

  /**
   * Costruttore completo per creare un nuovo utente.
   *
   * @param id l'identificativo univoco dell'utente
   * @param username il nome utente
   * @param email l'indirizzo email
   * @param name il nome
   * @param surname il cognome
   * @param password la password hashata
   * @param role il ruolo assegnato
   */
  public User(String id, String username, String email, String name, String surname,
      String password, Role role) {
    this();
    this.id = id;
    this.username = username;
    this.email = email;
    this.name = name;
    this.surname = surname;
    this.password = password;
    this.role = role;
  }

  /**
   * Restituisce l'ID dell'utente.
   *
   * @return l'identificativo dell'utente
   */
  public String getId() {
    return id;
  }

  /**
   * Imposta l'ID dell'utente.
   *
   * @param id il nuovo identificativo
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Restituisce il nome utente.
   *
   * @return il nome utente
   */
  public String getUsername() {
    return username;
  }

  /**
   * Imposta il nome utente.
   *
   * @param username il nuovo nome utente
   */
  public void setUsername(String username) {
    this.username = username;
  }

  /**
   * Restituisce l'email dell'utente.
   *
   * @return l'indirizzo email
   */
  public String getEmail() {
    return email;
  }

  /**
   * Imposta l'email dell'utente.
   *
   * @param email il nuovo indirizzo email
   */
  public void setEmail(String email) {
    this.email = email;
  }

  /**
   * Restituisce il nome dell'utente.
   *
   * @return il nome
   */
  public String getName() {
    return name;
  }

  /**
   * Imposta il nome dell'utente.
   *
   * @param name il nuovo nome
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Restituisce il cognome dell'utente.
   *
   * @return il cognome
   */
  public String getSurname() {
    return surname;
  }

  /**
   * Imposta il cognome dell'utente.
   *
   * @param surname il nuovo cognome
   */
  public void setSurname(String surname) {
    this.surname = surname;
  }

  /**
   * Restituisce la password hashata dell'utente.
   *
   * @return la password hashata
   */
  public String getPassword() {
    return password;
  }

  /**
   * Imposta la password dell'utente.
   *
   * @param password la nuova password hashata
   */
  public void setPassword(String password) {
    this.password = password;
  }

  /**
   * Restituisce la data di creazione dell'account.
   *
   * @return la data e ora di creazione
   */
  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  /**
   * Imposta la data di creazione dell'account.
   *
   * @param createdAt la nuova data di creazione
   */
  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  /**
   * Restituisce la data dell'ultimo accesso.
   *
   * @return la data e ora dell'ultimo login, o null se mai effettuato
   */
  public LocalDateTime getLastLogin() {
    return lastLogin;
  }

  /**
   * Imposta la data dell'ultimo accesso.
   *
   * @param lastLogin la nuova data di ultimo login
   */
  public void setLastLogin(LocalDateTime lastLogin) {
    this.lastLogin = lastLogin;
  }

  /**
   * Restituisce il ruolo dell'utente.
   *
   * @return il ruolo assegnato
   */
  public Role getRole() {
    return role;
  }

  /**
   * Imposta il ruolo dell'utente.
   *
   * @param role il nuovo ruolo
   */
  public void setRole(Role role) {
    this.role = role;
  }

  /**
   * Restituisce il nome del ruolo dell'utente.
   *
   * @return il nome del ruolo, o null se non assegnato
   */
  public String getRoleName() {
    return role != null ? role.getName() : null;
  }

  /**
   * Aggiorna la data dell'ultimo accesso all'istante corrente.
   */
  public void updateLastLogin() {
    this.lastLogin = LocalDateTime.now(ZoneId.systemDefault());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o instanceof User user) {
      return Objects.equals(id, user.id);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    return "User{"
        + "id='" + id + '\''
        + ", username='" + username + '\''
        + ", email='" + email + '\''
        + ", name='" + name + '\''
        + ", surname='" + surname + '\''
        + ", role=" + (role != null ? role.getName() : "null")
        + '}';
  }
}