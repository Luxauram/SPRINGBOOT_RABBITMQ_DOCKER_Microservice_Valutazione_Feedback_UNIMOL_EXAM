package it.unimol.microserviceassessmentfeedback.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Servizio per la validazione e l'estrazione di informazioni dai token JWT. Gestisce la verifica
 * della firma, la scadenza e l'estrazione dei claims.
 */
@Service
public class JwtValidationService {

  @Value("${jwt.public-key}")
  private String publicKeyString;
  private PublicKey publicKey;

  // ============ Costruttore ============

  // ============ Metodi Override ============

  // ============ Getters & Setters & Bool ============

  /**
   * Decifra e restituisce la chiave pubblica per la verifica dei token JWT.
   *
   * @return la chiave pubblica RSA
   * @throws RuntimeException se la chiave non può essere decodificata
   */
  private PublicKey getPublicKey() {
    if (this.publicKey == null) {
      try {
        byte[] keyBytes = Base64.getDecoder().decode(this.publicKeyString);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        this.publicKey = keyFactory.generatePublic(spec);
      } catch (IllegalArgumentException e) {
        throw new RuntimeException("Chiave pubblica non è in formato Base64 valido", e);
      } catch (Exception e) {
        throw new RuntimeException("Errore nella decodifica della chiave pubblica", e);
      }
    }
    return publicKey;
  }

  /**
   * Verifica se il token JWT è scaduto.
   *
   * @param token il token JWT da verificare
   * @return true se il token è scaduto, false altrimenti
   */
  public boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date());
  }

  /**
   * Verifica se il token JWT è valido. Controlla la firma, la scadenza e la struttura del token.
   *
   * @param token il token JWT da validare
   * @return true se il token è valido, false altrimenti
   */
  public boolean isTokenValid(String token) {
    try {
      Claims claims = extractAllClaims(token);

      return !isTokenExpired(token);
    } catch (Exception e) {
      System.err.println("Token validation failed: " + e.getMessage());
      return false;
    }
  }

  // ============ Metodi di Classe ============

  /**
   * Estrae un claim specifico dal token JWT.
   *
   * @param <T>            il tipo del claim da estrarre
   * @param token          il token JWT
   * @param claimsResolver funzione per estrarre il claim desiderato
   * @return il valore del claim estratto
   */
  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  /**
   * Estrae tutti i claims dal token JWT.
   *
   * @param token il token JWT
   * @return i claims contenuti nel token
   * @throws RuntimeException se il token non è valido o non può essere parsato
   */
  private Claims extractAllClaims(String token) {
    try {
      return Jwts.parserBuilder()
          .setSigningKey(getPublicKey())
          .build()
          .parseClaimsJws(token)
          .getBody();
    } catch (JwtException e) {
      throw new RuntimeException("Token JWT non valido: " + e.getMessage(), e);
    } catch (Exception e) {
      throw new RuntimeException("Errore durante l'analisi del token: " + e.getMessage(), e);
    }
  }

  /**
   * Estrae l'ID utente dal token JWT.
   *
   * @param token il token JWT
   * @return l'ID dell'utente
   */
  public String extractUserId(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  /**
   * Estrae il nome utente dal token JWT.
   *
   * @param token il token JWT
   * @return il nome utente
   */
  public String extractUsername(String token) {
    return extractClaim(token, claims -> claims.get("username", String.class));
  }

  /**
   * Estrae il ruolo dell'utente dal token JWT.
   *
   * @param token il token JWT
   * @return il ruolo dell'utente
   */
  public String extractRole(String token) {
    return extractClaim(token, claims -> claims.get("role", String.class));
  }

  /**
   * Estrae la data di scadenza dal token JWT.
   *
   * @param token il token JWT
   * @return la data di scadenza del token
   */
  public Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  /**
   * Valida il token e restituisce le informazioni dell'utente. Utile per i controller.
   *
   * @param token il token JWT da validare
   * @return un oggetto UserInfo contenente userId, username e role
   * @throws RuntimeException se il token non è valido o scaduto
   */
  public UserInfo validateTokenAndGetUserInfo(String token) {
    if (!isTokenValid(token)) {
      throw new RuntimeException("Token non valido o scaduto");
    }

    String userId = extractUserId(token);
    String username = extractUsername(token);
    String role = extractRole(token);

    return new UserInfo(userId, username, role);
  }

  /**
   * Estrae il token JWT dall'header Authorization rimuovendo il prefisso "Bearer ".
   *
   * @param authHeader l'header Authorization della richiesta HTTP
   * @return il token JWT estratto
   * @throws SecurityException se l'header è mancante o in formato errato
   */
  public String extractTokenFromHeader(String authHeader) {
    if (authHeader == null || authHeader.trim().isEmpty()) {
      throw new SecurityException("Header Authorization mancante");
    }

    if (!authHeader.startsWith("Bearer ")) {
      throw new SecurityException("Header Authorization deve iniziare con 'Bearer '");
    }

    String token = authHeader.substring(7).trim();

    if (token.isEmpty()) {
      throw new SecurityException("Token JWT mancante nell'header Authorization");
    }

    return token;
  }

  /**
   * Estrae l'ID dello studente dal token JWT.
   *
   * @param token il token JWT
   * @return l'ID dello studente
   * @throws IllegalStateException se lo studentId non è presente nel token
   * @throws RuntimeException      se si verifica un errore durante l'estrazione
   */
  public String extractStudentId(String token) {
    try {
      Claims claims = extractAllClaims(token);

      if (claims.containsKey("studentId")) {
        Object studentId = claims.get("studentId");
        return String.valueOf(studentId);
      }

      String role = claims.get("role", String.class);
      if ("ROLE_STUDENT".equals(role)) {
        String subject = claims.getSubject();
        if (subject != null && !subject.isEmpty()) {
          return subject;
        }
      }

      if ("ROLE_STUDENT".equals(role) && claims.containsKey("userId")) {
        Object userId = claims.get("userId");
        return String.valueOf(userId);
      }

      throw new IllegalStateException(
          "StudentId non trovato nel token JWT o non nel formato atteso");

    } catch (Exception e) {
      throw new RuntimeException("Errore nell'estrazione dello studentId dal token JWT", e);
    }
  }

  /**
   * Estrae l'ID del docente dal token JWT.
   *
   * @param token il token JWT
   * @return l'ID del docente
   * @throws IllegalStateException se il teacherId non è presente nel token
   * @throws RuntimeException      se si verifica un errore durante l'estrazione
   */
  public String extractTeacherId(String token) {
    try {
      Claims claims = extractAllClaims(token);

      if (claims.containsKey("teacherId")) {
        Object teacherId = claims.get("teacherId");
        return String.valueOf(teacherId);
      }

      String role = claims.get("role", String.class);
      if ("ROLE_TEACHER".equals(role)) {
        String subject = claims.getSubject();
        if (subject != null && !subject.isEmpty()) {
          return subject;
        }
      }

      if ("ROLE_TEACHER".equals(role) && claims.containsKey("userId")) {
        Object userId = claims.get("userId");
        return String.valueOf(userId);
      }

      throw new IllegalStateException(
          "TeacherId non trovato nel token JWT o non nel formato atteso");

    } catch (Exception e) {
      throw new RuntimeException("Errore nell'estrazione del teacherId dal token JWT", e);
    }
  }

  /**
   * Record per contenere le informazioni dell'utente estratte dal token.
   *
   * @param userId   l'ID dell'utente
   * @param username il nome utente
   * @param role     il ruolo dell'utente
   */
  public record UserInfo(String userId, String username, String role) {

  }
}