package com.spiid.login.service.infrastructure.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Utilidad para hashear refresh tokens antes de almacenarlos.
 * Guardar solo el hash evita exponer tokens en caso de fuga de DB.
 */
public final class TokenHashing {

  private TokenHashing() {}

  /**
   * Calcula el hash SHA-256 de un texto y lo devuelve en formato hexadecimal.
   *
   * <p>
   * Este método:
   * <ul>
   *   <li>Recibe un {@link String} como entrada</li>
   *   <li>Lo convierte a bytes usando UTF-8</li>
   *   <li>Aplica el algoritmo criptográfico SHA-256</li>
   *   <li>Convierte el resultado binario (32 bytes) a una cadena hexadecimal (64 caracteres)</li>
   * </ul>
   *
   * <p>
   * El resultado es determinista:
   * <pre>
   * mismo input  → mismo hash
   * distinto input → distinto hash
   * </pre>
   *
   * @param input Texto de entrada a hashear (no debe ser null)
   * @return Hash SHA-256 representado como una cadena hexadecimal de 64 caracteres
   * @throws IllegalStateException si ocurre algún error al calcular el hash
   */
  public static String sha256Hex(String input) {
    try {
      // Obtiene una instancia de MessageDigest configurada con el algoritmo SHA-256
      MessageDigest md = MessageDigest.getInstance("SHA-256");

      // Convierte el String de entrada a un arreglo de bytes usando UTF-8
      // (esto garantiza que el hash sea consistente entre distintos sistemas)
      byte[] inputBytes = input.getBytes(StandardCharsets.UTF_8);

      // Calcula el hash SHA-256 del arreglo de bytes
      // El resultado es un arreglo de 32 bytes (256 bits)
      byte[] digest = md.digest(inputBytes);

      // Crea un StringBuilder con capacidad suficiente para el resultado hexadecimal
      // Cada byte se representa con 2 caracteres hexadecimales → 32 * 2 = 64
      StringBuilder sb = new StringBuilder(digest.length * 2);

      // Recorre cada byte del hash
      for (byte b : digest) {
        // Convierte el byte a su representación hexadecimal de 2 caracteres
        // %02x:
        //  - %x  → formato hexadecimal
        //  - 2   → ancho mínimo de 2 caracteres
        //  - 0   → rellena con ceros a la izquierda si es necesario
        sb.append(String.format("%02x", b));
      }

      // Devuelve el hash SHA-256 completo en formato hexadecimal
      return sb.toString();

    } catch (Exception e) {
      // Si ocurre cualquier error (algoritmo no disponible, input null, etc.)
      // se lanza una excepción de estado con el error original como causa
      throw new IllegalStateException("No se pudo calcular SHA-256", e);
    }
  }

}
