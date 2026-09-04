# ADR-009: CSRF double-submit en backend JWT stateless

- **Estado:** Aceptado
- **Fecha:** 2026-09-04
- **Contexto:** SBVIA usa JWT stateless (sin `HttpSession`) con cookies HttpOnly (`accessToken`, `refreshToken`) y cabecera `Authorization` como respaldo. Spring Security 6 aplica por defecto `CsrfAuthenticationStrategy`, que borra la cookie `XSRF-TOKEN` en cada request autenticado (rotación diseñada para login con sesión). Además, ningún GET emitía el token. Resultado: el flujo double-submit nunca se estabilizaba y **todos** los POST/PUT/DELETE del frontend respondían 403 `Invalid CSRF token`.

## Decisión

1. Envolver el repositorio en `StatelessCsrfTokenRepository`, que ignora el borrado del token: `CsrfConfigurer` añade `CsrfAuthenticationStrategy` a la estrategia de sesión **siempre** (no se puede quitar por configuración) y ese borrado en cada request autenticado es lo que rompía el double-submit. Sin sesiones no hay fijación de sesión que mitigar con esa rotación.
2. Emitir la cookie `XSRF-TOKEN` antes de la cadena (`CsrfTokenIssuerFilter`) cuando el cliente no tiene una, manteniendo la validación `X-XSRF-TOKEN == cookie` en operaciones mutables.
3. Compartir un único bean `CsrfTokenRepository` (httpOnly false, en `CsrfConfig` para evitar referencia circular con `SecurityConfig`) entre la configuración y el filtro de emisión.

## Consecuencias

- Los POST/PUT/DELETE del Angular vuelven a funcionar enviando el encabezado que el propio `HttpClient` replica desde la cookie.
- La protección CSRF sigue activa (no se deshabilita): un sitio atacante no puede leer la cookie por SOP y por tanto no puede forjar el encabezado.
- Las cookies de sesión siguen con `SameSite` por defecto del navegador como defensa en profundidad.
- Riesgo aceptado: sin rotación post-login, el token CSRF vive hasta que el navegador lo descarte; aceptable porque el secreto real (JWT) sí expira y el token CSRF solo tiene valor junto a las cookies de la víctima.
