package com.sbvia.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Emite la cookie XSRF-TOKEN cuando el cliente aún no tiene una. Angular la
 * replica en el encabezado X-XSRF-TOKEN (double-submit cookie) para las
 * operaciones mutables. Se ejecuta antes de la cadena para que la respuesta
 * aún no esté comprometida. Ver ADR-009.
 */
@Component
@RequiredArgsConstructor
final class CsrfTokenIssuerFilter extends OncePerRequestFilter {

    private final CsrfTokenRepository csrfTokenRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (csrfTokenRepository.loadToken(request) == null) {
            csrfTokenRepository.saveToken(csrfTokenRepository.generateToken(request), request, response);
        }
        filterChain.doFilter(request, response);
    }
}
