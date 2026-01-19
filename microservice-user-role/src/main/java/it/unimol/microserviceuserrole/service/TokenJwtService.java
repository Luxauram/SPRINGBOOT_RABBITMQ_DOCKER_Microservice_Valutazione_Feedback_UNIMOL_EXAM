package it.unimol.microserviceuserrole.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import it.unimol.microserviceuserrole.dto.auth.TokenDto;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Servizio per la gestione dei token JWT.
 * Fornisce funzionalità per la generazione, validazione, refresh e invalidazione dei token.
 */
@Service
public class TokenJwtService {

  private static final Set<String> invalidatedTokens = ConcurrentHashMap.newKeySet();
  @Value("${jwt.private-key}")
  private String privateKeyString;
  @Value("${jwt.public-key}")
  private String publicKeyString;
  @Value("${jwt.expiration}")
  private Long jwtExpiration;
  private PrivateKey privateKey;
  private PublicKey publicKey;

  /**
   * Decifra e restituisce la chiave privata per la firma dei token JWT.
   *
   * @return La chiave privata come oggetto PrivateKey.
   * @throws RuntimeException Se la chiave privata non è in formato Base64 valido o se si verifica
   *                          un errore durante la generazione della chiave.
   */
  private PrivateKey getPrivateKey() {
    if (this.privateKey == null) {
      try {
        byte[] keyBytes = Base64.getDecoder().decode(this.privateKeyString);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        this.privateKey = keyFactory.generatePrivate(spec);
      } catch (IllegalArgumentException e) {
        throw new RuntimeException("Chiave privata non è in formato Base64 valido", e);
      } catch (Exception e) {
        throw new RuntimeException("Errore prv_key, controlla application.properties", e);
      }
    }
    return privateKey;
  }

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
        throw new RuntimeException("Errore pub_key, controlla application.properties", e);
      }
    }
    return publicKey;
  }

  /**
   * Estrae un claim specifico dal token JWT utilizzando una funzione di risoluzione.
   *
   * @param <T>            Il tipo del claim da estrarre.
   * @param token          Il token JWT da cui estrarre il claim.
   * @param claimsResolver La funzione per risolvere il claim dai claims del token.
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
    return Jwts.parserBuilder()
        .setSigningKey(getPublicKey())
        .build()
        .parseClaimsJws(token)
        .getBody();
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
  private Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  /**
   * Verifica se il token JWT è scaduto.
   *
   * @param token Il token JWT da verificare.
   * @return true se il token è scaduto, false altrimenti.
   */
  @SuppressWarnings("JavaUtilDate")
  private boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date());
  }

  /**
   * Verifica se il token JWT è valido.
   *
   * @param token Il token JWT da verificare.
   * @return true se il token è valido, false altrimenti.
   */
  public boolean isTokenValid(String token) {
    return !isTokenExpired(token) && !invalidatedTokens.contains(token);
  }

  /**
   * Genera un nuovo token JWT con i dati dell'utente.
   *
   * @param userId   L'ID dell'utente.
   * @param username Il name utente.
   * @param role     Il role dell'utente.
   * @return Un oggetto TokenJWTDto contenente il token JWT generato.
   */
  @SuppressWarnings("JavaUtilDate")
  public TokenDto generateToken(String userId, String username, String role) {
    Map<String, Object> claims = new HashMap<>();

    long now = System.currentTimeMillis();
    long expiration = now + (this.jwtExpiration * 1000);

    String token = Jwts.builder()
        .setClaims(claims)
        .setSubject(userId)
        .setIssuedAt(new Date(now))
        .setExpiration(new Date(expiration))
        .claim("username", username)
        .claim("role", role)
        .signWith(getPrivateKey(), SignatureAlgorithm.RS256)
        .compact();

    return new TokenDto(token, "Bearer", this.jwtExpiration);
  }

  /**
   * Analizza un token JWT e restituisce un oggetto TokenJWTDto.
   *
   * @param token Il token JWT da analizzare.
   * @return Un oggetto TokenJWTDto contenente il token JWT.
   */
  @SuppressWarnings("JavaUtilDate")
  public TokenDto parseToken(String token) {
    long remainingTime = 0;
    try {
      Date expiration = extractExpiration(token);
      remainingTime = Math.max(0, (expiration.getTime() - System.currentTimeMillis()) / 1000);
    } catch (Exception e) {
      remainingTime = 0;
    }

    return new TokenDto(token, "Bearer", remainingTime);
  }

  /**
   * Invalida un token aggiungendolo all'insieme di token invalidati.
   *
   * @param token Il token da invalidare.
   */
  public void invalidateToken(String token) {
    invalidatedTokens.add(token);
  }

  /**
   * Effettua il refresh di un token JWT, generando un nuovo token con gli stessi dati dell'utente.
   *
   * @param token Il token JWT da aggiornare.
   * @return Un nuovo oggetto TokenJWTDto contenente il token JWT aggiornato.
   * @throws RuntimeException Se il token è già stato invalidato.
   */
  public TokenDto refreshToken(String token) throws RuntimeException {
    if (invalidatedTokens.contains(token)) {
      throw new RuntimeException("Token già invalidato, non è possibile effettuare il refresh");
    }

    Claims claims = extractAllClaims(token);
    String userId = claims.getSubject();
    String username = claims.get("username", String.class);
    String role = claims.get("role", String.class);

    invalidateToken(token);

    return generateToken(userId, username, role);
  }

  /**
   * Estrae il token JWT dall'header Authorization rimuovendo il prefisso "Bearer ".
   *
   * @param authHeader L'header Authorization completo (es. "Bearer eyJ0eXAiOiJKV1...")
   * @return Il token JWT pulito senza il prefisso "Bearer "
   * @throws SecurityException Se l'header è null, vuoto o non ha il formato corretto
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
}