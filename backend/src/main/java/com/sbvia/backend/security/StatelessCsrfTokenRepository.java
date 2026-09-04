package com.sbvia.backend.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.DeferredCsrfToken;

/**
 * {@link CsrfTokenRepository} para backend JWT stateless. Delega todo en el
 * repositorio de cookie salvo el borrado del token, que se ignora.
 *
 * <p>Spring Security borra la cookie XSRF-TOKEN en cada request autenticado
 * ({@code CsrfAuthenticationStrategy}, rotación pensada para login con sesión)
 * y nada la vuelve a emitir, así que el double-submit nunca se estabiliza y
 * todos los POST/PUT/DELETE responden 403. Sin sesiones no hay fijación de
 * sesión que mitigar con esa rotación; la validación encabezado == cookie
 * sigue activa. Ver ADR-009.
 */
final class StatelessCsrfTokenRepository implements CsrfTokenRepository {

    private final CsrfTokenRepository delegate;

    StatelessCsrfTokenRepository(CsrfTokenRepository delegate) {
        this.delegate = delegate;
    }

    @Override
    public CsrfToken generateToken(HttpServletRequest request) {
        return delegate.generateToken(request);
    }

    @Override
    public void saveToken(CsrfToken token, HttpServletRequest request, HttpServletResponse response) {
        if (token != null) {
            delegate.saveToken(token, request, response);
        }
    }

    @Override
    public CsrfToken loadToken(HttpServletRequest request) {
        return delegate.loadToken(request);
    }

    @Override
    public DeferredCsrfToken loadDeferredToken(HttpServletRequest request, HttpServletResponse response) {
        return delegate.loadDeferredToken(request, response);
    }
}
