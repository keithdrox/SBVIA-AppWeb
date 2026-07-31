package com.sbvia.backend.security;

import com.sbvia.backend.service.TokenBlacklistService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.util.WebUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro JWT que se ejecuta una vez por cada request HTTP.
 * Extiende OncePerRequestFilter de Spring Security.
 *
 * Flujo:
 * 1. Extrae el token del encabezado Authorization: Bearer [token]
 * 2. Valida la firma y la expiración con JwtService.validateToken()
 * 3. Consulta Redis para verificar que el JTI no está en la blacklist
 * 4. Establece el UsernamePasswordAuthenticationToken en el SecurityContextHolder
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String jwt = null;
        
        // 1. Intentar extraer el token de la Cookie (HttpOnly)
        Cookie cookie = WebUtils.getCookie(request, "accessToken");
        if (cookie != null) {
            jwt = cookie.getValue();
        } 
        
        // 2. Si no hay cookie, hacer fallback al header Authorization (Para Postman/Swagger)
        if (jwt == null) {
            final String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                jwt = authHeader.substring(7);
            }
        }

        if (jwt == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 2. Extraer el email del token
            final String email = jwtService.extractEmail(jwt);

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // 3. Consultar Redis: verificar que el JTI no está en la blacklist
                String jti = jwtService.extractJti(jwt);
                if (tokenBlacklistService.isTokenBlacklisted(jti)) {
                    filterChain.doFilter(request, response);
                    return;
                }

                // 4. Cargar UserDetails desde la BD
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                // 5. Validar firma y expiración
                if (jwtService.validateToken(jwt, userDetails)) {
                    // 6. Establecer el SecurityContext
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // Token inválido: no se establece autenticación, se continúa la cadena
            logger.debug("Token JWT inválido: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
