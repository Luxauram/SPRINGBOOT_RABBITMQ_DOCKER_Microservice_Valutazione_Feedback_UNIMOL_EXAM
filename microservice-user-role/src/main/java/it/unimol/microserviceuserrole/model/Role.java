package it.unimol.microserviceuserrole.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entità JPA che rappresenta un ruolo utente nel sistema.
 * Ogni ruolo ha un identificativo univoco, un nome e una descrizione opzionale.
 */
@Entity
@Table(name = "roles")
public class Role {

  @Id
  @Column(name = "roleId", nullable = false, length = 255)
  private String id;

  @Column(name = "name", nullable = false, unique = true, length = 100)
  private String name;

  @Column(name = "description", length = 500)
  private String description;

  /**
   * Costruttore vuoto richiesto da JPA.
   */
  public Role() {
  }

  /**
   * Costruttore completo per creare un nuovo ruolo.
   *
   * @param id l'identificativo univoco del ruolo
   * @param name il nome del ruolo
   * @param description la descrizione del ruolo
   */
  public Role(String id, String name, String description) {
    this.id = id;
    this.name = name;
    this.description = description;
  }

  /**
   * Restituisce l'ID del ruolo.
   *
   * @return l'identificativo del ruolo
   */
  public String getId() {
    return id;
  }

  /**
   * Imposta l'ID del ruolo.
   *
   * @param id il nuovo identificativo
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Restituisce il nome del ruolo.
   *
   * @return il nome del ruolo
   */
  public String getName() {
    return name;
  }

  /**
   * Imposta il nome del ruolo.
   *
   * @param name il nuovo nome
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Restituisce la descrizione del ruolo.
   *
   * @return la descrizione del ruolo
   */
  public String getDescription() {
    return description;
  }

  /**
   * Imposta la descrizione del ruolo.
   *
   * @param description la nuova descrizione
   */
  public void setDescription(String description) {
    this.description = description;
  }

  @Override
  public String toString() {
    return "Role{"
        + "id='" + id + '\''
        + ", name='" + name + '\''
        + ", description='" + description + '\''
        + '}';
  }
}