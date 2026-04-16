package com.spiid.login.service.domain.port.in;


import com.spiid.login.service.domain.model.RoleCatalogItem;

import java.util.List;
import java.util.UUID;

/**
 * Caso de uso principal de Autenticación y Autorización.
 *
 * Esta interfaz define las operaciones del dominio relacionadas con:
 * - Registro de usuarios
 * - Autenticación (login)
 * - Renovación de tokens (refresh)
 * - Consulta del usuario autenticado
 *
 * No contiene lógica de infraestructura (JWT, DB, HTTP, etc),
 * solo define el contrato que deben implementar los adaptadores.
 */
public interface AuthUseCase {

    /**
     * Caso de uso 1: Registro de usuario.
     *
     * Crea un nuevo usuario en el sistema y genera sus tokens de autenticación.
     *
     * @param email        Correo electrónico del usuario (identificador único).
     * @param password     Contraseña en texto plano (se encripta en la implementación).
     * @param roleCodes    Lista de códigos de roles asignados al usuario.
     * @param userAgent    Información del cliente (navegador / app).
     * @param ipAddress    Dirección IP desde donde se realiza el registro.
     *
     * @return AuthResultDto
     *         - accessToken: JWT de acceso
     *         - refreshToken: JWT para renovar sesión
     *         - user: información básica del usuario creado
     */
    AuthResultDto register(
            String email,
            String password,
            List<Short> roleCodes,
            String userAgent,
            String ipAddress
    );

    /**
     * Caso de uso 2: Login (autenticación).
     *
     * Valida las credenciales del usuario y genera nuevos tokens
     * si la autenticación es exitosa.
     *
     * @param email        Correo electrónico del usuario.
     * @param password     Contraseña ingresada.
     * @param userAgent    Información del cliente que inicia sesión.
     * @param ipAddress    Dirección IP del login.
     *
     * @return AuthResultDto
     *         Contiene los tokens y los datos del usuario autenticado.
     */
    AuthResultDto login(
            String email,
            String password,
            String userAgent,
            String ipAddress
    );

    /**
     * Caso de uso 3: Refresh de tokens.
     *
     * Permite renovar el accessToken cuando ha expirado,
     * validando previamente el refreshToken.
     *
     * @param refreshToken Token de refresco válido.
     * @param userAgent    Cliente desde donde se solicita el refresh.
     * @param ipAddress    Dirección IP de la solicitud.
     *
     * @return AuthResultDto
     *         Nuevos tokens y la información del usuario.
     */
    AuthResultDto refresh(
            String refreshToken,
            String userAgent,
            String ipAddress
    );

    /**
     * Caso de uso 4: Obtener información del usuario autenticado.
     *
     * Se utiliza típicamente en endpoints como /me
     * para devolver la información del usuario a partir
     * de su identificador.
     *
     * @param userId UUID del usuario autenticado (extraído del token).
     *
     * @return UserViewDto
     *         Vista del usuario sin exponer datos sensibles.
     */
    UserViewDto me(UUID userId);

    /**
     * Resultado estándar de autenticación.
     *
     * Se reutiliza en:
     * - register
     * - login
     * - refresh
     *
     * Agrupa los tokens y la información del usuario.
     */
    record AuthResultDto(
            String accessToken,
            String refreshToken,
            UserViewDto user
    ) {}

    /**
     * Vista de usuario expuesta al cliente.
     *
     * No debe contener información sensible
     * como contraseñas, hashes, etc.
     */
    record UserViewDto(
            UUID id,
            String email,
            boolean enabled,
            List<RoleCatalogItem> roles
    ) {}

    /**
     *  Se modifica el puerto de entrada, se agrega
     * @param idToken
     * @return
     */
    String loginWithGoogle(String idToken, String role, String tenantId);

}