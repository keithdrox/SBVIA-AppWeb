# ADR-003: Revocación de Tokens JWT utilizando Redis (Blacklist)

## Estado
Aceptado — Actualizado 2026-08-08

## Contexto
El sistema SBVIA utiliza JWT (JSON Web Tokens) para implementar autenticación stateless. Por naturaleza, una vez que un JWT es emitido, el servidor no mantiene un registro de estado, lo que significa que un token es válido hasta su fecha de expiración, incluso si el usuario cierra sesión explícitamente o su cuenta es comprometida. Se necesita un mecanismo para invalidar estos tokens (Logout) para cumplir con el control de seguridad (OWASP A07: Fallas de autenticación).

## Decisión
Se implementa una **Blacklist (lista negra) en memoria usando Redis** para gestionar la revocación de tokens.
1. Cuando se genera un JWT, se incluye un identificador único (claim `jti`).
2. Al hacer logout, el servidor extrae el `jti` del token presentado y lo guarda en Redis.
3. El registro en Redis se configura con un Tiempo de Vida (TTL) exactamente igual al tiempo restante de expiración del token.
4. En cada petición autenticada, el `JwtAuthFilter` verifica que el `jti` del token no exista en Redis antes de procesar el acceso.

## Consecuencias positivas
- **Seguridad mejorada**: Permite invalidar tokens en tiempo real, cerrando una vulnerabilidad clásica de JWT.
- **Rendimiento**: Redis opera en memoria (RAM), por lo que las lecturas por cada request HTTP añaden una latencia mínima (< 2ms) en comparación a consultar una base de datos relacional.
- **Auto-mantenimiento**: Al usar el TTL de Redis, los tokens revocados se purgan automáticamente de la memoria una vez que su expiración natural ocurre, evitando que la blacklist crezca indefinidamente.

## Consecuencias negativas
- Introduce una dependencia de infraestructura adicional (servicio Redis).
- Si el servicio Redis falla, el proceso de autenticación podría degradarse si no se implementan políticas de fallo seguro o alta disponibilidad.
- Paradójicamente, hace que el mecanismo deje de ser 100% "stateless", ya que requiere estado (para tokens revocados) en el lado del servidor, aunque de manera optimizada y centralizada.

---

## Enmienda 2026-08-08 — Corrección de Seguridad de Cookie

### Problema identificado
En la Entrega 3, las cookies del accessToken solo tenían `HttpOnly`. Faltaban `Secure` y `SameSite`, lo que dejaba la cookie vulnerable a:
- **Ataques CSRF cross-site**: sin `SameSite`, la cookie se enviaría en peticiones desde otros orígenes.
- **Interceptación en HTTP**: sin `Secure`, la cookie viajaría sin cifrar en redes inseguras.

### Corrección aplicada
`AuthController.java` fue actualizado para agregar en **todos** los endpoints que emiten cookie (`/registro`, `/login`, `/refresh`, `/logout`):

```java
ResponseCookie.from("accessToken", token)
    .httpOnly(true)
    .secure(cookieSecure)    // true en producción (COOKIE_SECURE=true), false en dev HTTP
    .sameSite("Strict")      // CSRF mitigation — cookie no se envía cross-site
    .path("/")
    .maxAge(expiresIn)
    .build();
```

La propiedad `security.cookie.secure` en `application.yml` permite alternar `Secure` por entorno sin cambiar código:

```yaml
security:
  cookie:
    secure: ${COOKIE_SECURE:false}  # false en dev, true en producción
```
