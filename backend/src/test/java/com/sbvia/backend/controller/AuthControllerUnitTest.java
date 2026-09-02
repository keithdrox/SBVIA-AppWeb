package com.sbvia.backend.controller;

import com.sbvia.backend.dto.AuthResponse;
import com.sbvia.backend.dto.LoginRequest;
import com.sbvia.backend.dto.RefreshTokenRequest;
import com.sbvia.backend.exception.RateLimitExceededException;
import com.sbvia.backend.service.AuthService;
import com.sbvia.backend.service.LoginRateLimiter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import jakarta.servlet.http.HttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerUnitTest {

    @Mock
    private AuthService authService;

    @Mock
    private LoginRateLimiter loginRateLimiter;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void configurarCookiesSeguras() {
        ReflectionTestUtils.setField(authController, "cookieSecure", true);
    }

    @Test
    void logoutPriorizaElTokenDeLaCookie() {
        authController.logout("Bearer token-header", "token-cookie");

        verify(authService).logout("token-cookie");
    }

    @Test
    void logoutAceptaBearerCuandoNoHayCookie() {
        authController.logout("Bearer token-header", null);

        verify(authService).logout("token-header");
    }

    @Test
    void logoutSinTokenSoloLimpiaLasCookies() {
        ResponseEntity<Void> respuesta = authController.logout("Basic credencial", null);

        verify(authService, never()).logout(org.mockito.ArgumentMatchers.anyString());
        assertThat(respuesta.getStatusCode().value()).isEqualTo(204);
        assertThat(respuesta.getHeaders().get("Set-Cookie")).hasSize(2);
    }

    @Test
    void refreshPriorizaElTokenDeLaCookie() {
        when(authService.refresh("refresh-cookie")).thenReturn(respuesta());
        when(authService.getRefreshExpirationSeconds()).thenReturn(3_600L);

        authController.refresh("refresh-cookie", solicitud("refresh-body"));

        verify(authService).refresh("refresh-cookie");
    }

    @Test
    void refreshAceptaElTokenDelCuerpoSiNoHayCookie() {
        when(authService.refresh("refresh-body")).thenReturn(respuesta());
        when(authService.getRefreshExpirationSeconds()).thenReturn(3_600L);

        authController.refresh(null, solicitud("refresh-body"));

        verify(authService).refresh("refresh-body");
    }

    @Test
    void conservaLaVigenciaEnSegundosDeLaCookieDeAcceso() {
        when(authService.refresh("refresh-cookie")).thenReturn(respuesta());
        when(authService.getRefreshExpirationSeconds()).thenReturn(604_800L);

        ResponseEntity<AuthResponse> respuesta = authController.refresh("refresh-cookie", null);

        assertThat(respuesta.getHeaders().get("Set-Cookie"))
                .anySatisfy(cookie -> assertThat(cookie).contains("accessToken=access-nuevo", "Max-Age=3600"));
    }

    @Test
    void refreshRechazaLaAusenciaDeToken() {
        assertThatThrownBy(() -> authController.refresh(null, null))
                .isInstanceOf(org.springframework.security.authentication.BadCredentialsException.class)
                .hasMessage("Refresh token ausente o no proporcionado");
    }

    @Test
    void refreshRechazaUnTokenEnBlanco() {
        assertThatThrownBy(() -> authController.refresh(null, solicitud("  ")))
                .isInstanceOf(org.springframework.security.authentication.BadCredentialsException.class)
                .hasMessage("Refresh token ausente o no proporcionado");
    }

    @Test
    void loginRechazaCuentoSegunRateLimiterAntesDeAutenticar() {
        when(request.getRemoteAddr()).thenReturn("10.0.0.7");
        org.mockito.Mockito.doThrow(new RateLimitExceededException("Demasiados intentos fallidos"))
                .when(loginRateLimiter).check("10.0.0.7");

        LoginRequest body = new LoginRequest();
        body.setEmail("admin@sbvia.com");
        body.setPassword("password123");

        assertThatThrownBy(() -> authController.login(body, request))
                .isInstanceOf(RateLimitExceededException.class)
                .hasMessage("Demasiados intentos fallidos");

        verify(authService, never()).login(body);
    }

    @Test
    void loginRegistraFalloCuandoLaAutenticacionFalla() {
        when(request.getRemoteAddr()).thenReturn("10.0.0.8");
        when(authService.login(org.mockito.ArgumentMatchers.any(LoginRequest.class)))
                .thenThrow(new org.springframework.security.authentication.BadCredentialsException("bad"));

        LoginRequest body = new LoginRequest();
        body.setEmail("admin@sbvia.com");
        body.setPassword("incorrecta");

        assertThatThrownBy(() -> authController.login(body, request))
                .isInstanceOf(org.springframework.security.authentication.BadCredentialsException.class);

        verify(loginRateLimiter).recordFailure("10.0.0.8");
    }

    private RefreshTokenRequest solicitud(String token) {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken(token);
        return request;
    }

    private AuthResponse respuesta() {
        return AuthResponse.builder()
                .accessToken("access-nuevo")
                .refreshToken("refresh-nuevo")
                .expiresIn(3_600L)
                .tokenType("Bearer")
                .build();
    }
}
