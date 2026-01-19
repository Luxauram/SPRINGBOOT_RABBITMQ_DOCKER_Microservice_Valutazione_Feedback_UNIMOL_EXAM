package it.unimol.apigateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Servizio per la validazione e gestione dei token JWT.
 * Fornisce metodi per estrarre informazioni dai token, validarli
 * e gestire le chiavi crittografiche utilizzate per la firma.
 */
@Component
public class JwtValidationService {

  @SuppressWarnings("UnusedVariable")
  @Value("${jwt.private-key}")
  private String privateKeyString;

  @SuppressWarnings("UnusedVariable")
  @Value("${jwt.expiration}")
  private Long jwtExpiration;

  @SuppressWarnings("UnusedVariable")
  private PrivateKey privateKey;

  @Value("${jwt.public-key}")
  private String publicKeyString;
  private PublicKey publicKey;

  /**
   * Decifra e restituisce la chiave pubblica per la verifica dei token JWT.
   *
   * @return La chiave pubblica come oggetto PublicKey.
   * @throws RuntimeException Se la chiave pubblica non è in formato Base64 valido o se si verifica
   *                          un errore durante la generazione della chiave.
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
   * Estrae un claim specifico dal token JWT.
   *
   * @param <T> il tipo del claim da estrarre
   * @param token          Il token JWT da cui estrarre il claim.
   * @param claimsResolver La funzione per estrarre il claim desiderato.
   * @return Il valore del claim estratto.
   */
  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  /**
   * Estrae tutti i claims dal token JWT.
   *
   * @param token Il token JWT da cui estrarre i claims.
   * @return Un oggetto Claims contenente tutti i claims del token.
   * @throws RuntimeException Se il token non è valido o se si verifica un errore durante
   *                          l'estrazione dei claims.
   */
  private Claims extractAllClaims(String token) {
    try {
      return Jwts.parserBuilder()
          .setSigningKey(getPublicKey())
          .setAllowedClockSkewSeconds(Long.MAX_VALUE / 1000)
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
   * @param token Il token JWT da cui estrarre l'ID utente.
   * @return L'ID utente come stringa.
   */
  public String extractUserId(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  /**
   * Estrae il name utente dal token JWT.
   *
   * @param token Il token JWT da cui estrarre il name utente.
   * @return Il name utente come stringa.
   */
  public String extractUsername(String token) {
    return extractClaim(token, claims -> claims.get("username", String.class));
  }

  /**
   * Estrae il role dell'utente dal token JWT.
   *
   * @param token Il token JWT da cui estrarre il role.
   * @return Il role come stringa.
   */
  public String extractRole(String token) {
    return extractClaim(token, claims -> claims.get("role", String.class));
  }

  /**
   * Estrae la data di scadenza dal token JWT.
   *
   * @param token Il token JWT da cui estrarre la data di scadenza.
   * @return La data di scadenza come oggetto Date.
   */
  public Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  /**
   * Verifica se il token JWT è scaduto.
   *
   * @param token Il token JWT da verificare.
   * @return true se il token è scaduto, false altrimenti.
   */
  @SuppressWarnings("JavaUtilDate")
  public boolean isTokenExpired(String token) {
    try {
      return extractExpiration(token).before(new Date());
    } catch (RuntimeException e) {
      if (e.getMessage() != null && e.getMessage().contains("expired")) {
        return true;
      }
      throw e;
    }
  }

  /**
   * Verifica se il token JWT è valido.
   *
   * @param token Il token JWT da verificare.
   * @return true se il token è valido, false altrimenti.
   */
  public boolean isTokenValid(String token) {
    try {
      /*Claims claims = extractAllClaims(token);*/
      extractAllClaims(token);

      return !isTokenExpired(token);
    } catch (Exception e) {
      System.err.println("Token validation failed: " + e.getMessage());
      return false;
    }
  }

  /**
   * Valida il token e restituisce le informazioni dell'utente.
   * Utile per i controller.
   *
   * @param token il token JWT da validare
   * @return le informazioni dell'utente estratte dal token
   * @throws RuntimeException se il token non è valido o è scaduto
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
   * @param authHeader l'header Authorization contenente il token
   * @return il token JWT estratto
   * @throws SecurityException se l'header è mancante, vuoto o non ha il formato corretto
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
   * @param token il token JWT da cui estrarre l'ID studente
   * @return l'ID dello studente come stringa
   * @throws IllegalStateException se lo studentId non è presente nel token
   * @throws RuntimeException se si verifica un errore durante l'estrazione
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

    } catch (IllegalStateException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException("Errore nell'estrazione dello studentId dal token JWT", e);
    }
  }

  /**
   * Estrae l'ID del docente dal token JWT.
   *
   * @param token il token JWT da cui estrarre l'ID docente
   * @return l'ID del docente come stringa
   * @throws IllegalStateException se il teacherId non è presente nel token
   * @throws RuntimeException se si verifica un errore durante l'estrazione
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

    } catch (IllegalStateException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException("Errore nell'estrazione del teacherId dal token JWT", e);
    }
  }

  /**
   * Record per contenere le informazioni dell'utente estratte dal token.
   *
   * @param userId l'ID dell'utente
   * @param username il nome utente
   * @param role il ruolo dell'utente
   */
  public record UserInfo(String userId, String username, String role) {

  }
}