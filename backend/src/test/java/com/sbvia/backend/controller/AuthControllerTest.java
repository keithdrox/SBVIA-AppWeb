package com.sbvia.backend.controller;

import com.sbvia.backend.dto.LoginRequest;
import com.sbvia.backend.dto.RegisterRequest;
import com.sbvia.backend.entity.EstadoUsuario;
import com.sbvia.backend.entity.Rol;
import com.sbvia.backend.entity.Usuario;
import com.sbvia.backend.repository.EstadoUsuarioRepository;
import com.sbvia.backend.repository.RolRepository;
import com.sbvia.backend.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private EstadoUsuarioRepository estadoUsuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private com.sbvia.backend.security.JwtService jwtService;

    @MockitoBean
    private com.sbvia.backend.service.TokenBlacklistService tokenBlacklistService;

    private Usuario testUser;

    @BeforeEach
    void setUp() {
        usuarioRepository.deleteAll();
        rolRepository.deleteAll();
        Rol rolTest = Rol.builder()
                .nombre("ROLE_USER")
                .descripcion("Alumno o conductor en práctica")
                .build();
        rolTest = rolRepository.save(rolTest);

        EstadoUsuario estadoActivo = estadoUsuarioRepository.save(EstadoUsuario.builder()
                .nombre("ACTIVO")
                .descripcion("Cuenta habilitada")
                .permiteAcceso(true)
                .build());
        testUser = Usuario.builder()
                .nombres("Test")
                .apellidos("User")
                .correo("test@example.com")
                .contrasenaHash(passwordEncoder.encode("password123"))
                .rol(rolTest)
                .estadoUsuario(estadoActivo)
                .cuentaBloqueada(false)
                .build();
        usuarioRepository.save(testUser);
    }

    @Test
    @DisplayName("Login exitoso retorna access token y oculta refresh token")
    void loginExitoso() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setCorreo("test@example.com");
        request.setPassword("password123");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").doesNotExist())
                .andReturn();

        String token = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("accessToken").asText();
        assertThat(jwtService.extractIssuer(token)).isEqualTo("sbvia-api");
        assertThat(jwtService.extractAudience(token)).containsExactly("sbvia-web");
        assertThat(jwtService.extractNotBefore(token)).isNotNull();
    }

    @Test
    @DisplayName("Login: cookie accessToken tiene HttpOnly y SameSite=Strict")
    void loginCookieTieneHttpOnlyYSameSite() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setCorreo("test@example.com");
        request.setPassword("password123");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpServletResponse response = result.getResponse();
        java.util.List<String> setCookieHeaders = response.getHeaders("Set-Cookie");
        String accessCookie = setCookieHeaders.stream()
                .filter(cookie -> cookie.startsWith("accessToken="))
                .findFirst()
                .orElseThrow();

        assertThat(accessCookie)
                .as("La cabecera Set-Cookie debe existir en la respuesta de login")
                .isNotNull();
        assertThat(accessCookie)
                .as("La cookie debe tener HttpOnly para prevenir acceso desde JavaScript (mitigación XSS)")
                .containsIgnoringCase("HttpOnly");
        assertThat(accessCookie)
                .as("La cookie debe tener SameSite=Strict para prevenir CSRF en peticiones cross-site")
                .containsIgnoringCase("SameSite=Strict");
        assertThat(setCookieHeaders).anySatisfy(cookie ->
                assertThat(cookie).startsWith("XSRF-TOKEN=").doesNotContainIgnoringCase("HttpOnly"));
    }

    @Test
    @DisplayName("Registro: cookie accessToken tiene HttpOnly y SameSite=Strict")
    void registroCookieTieneHttpOnlyYSameSite() throws Exception {
        usuarioRepository.deleteAll();

        RegisterRequest request = new RegisterRequest();
        request.setNombres("Nuevo");
        request.setApellidos("Conductor");
        request.setCorreo("nuevo@sbvia.com");
        request.setPassword("password123");
        request.setTelefono("0999999999");

        MvcResult result = mockMvc.perform(post("/api/auth/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        java.util.List<String> setCookieHeaders = result.getResponse().getHeaders("Set-Cookie");

        assertThat(setCookieHeaders)
                .as("Registro debe emitir cookies de CSRF, acceso y renovación")
                .hasSize(3);
        assertThat(setCookieHeaders.stream()
                .filter(cookie -> !cookie.startsWith("XSRF-TOKEN=")))
                .allSatisfy(cookie -> {
                    assertThat(cookie).containsIgnoringCase("HttpOnly");
                    assertThat(cookie).containsIgnoringCase("SameSite=Strict");
                });
        assertThat(setCookieHeaders).anySatisfy(cookie ->
                assertThat(cookie).startsWith("XSRF-TOKEN=").doesNotContainIgnoringCase("HttpOnly"));
        assertThat(setCookieHeaders).anySatisfy(cookie -> assertThat(cookie).startsWith("refreshToken="));
    }

    @Test
    @DisplayName("Login con clave incorrecta retorna 401")
    void loginClaveIncorrecta() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setCorreo("test@example.com");
        request.setPassword("wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Registro con email duplicado retorna 409 Conflict")
    void registroEmailDuplicado() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setNombres("Nuevo");
        request.setApellidos("User");
        request.setCorreo("test@example.com");
        request.setPassword("password123");
        request.setTelefono("0999999999");

        mockMvc.perform(post("/api/auth/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Acceso sin token retorna Problem Details 401")
    void accesoSinToken() throws Exception {
        mockMvc.perform(get("/api/usuarios/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("No autenticado"))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.detail").exists())
                .andExpect(jsonPath("$.instance").value("/api/usuarios/me"));
    }

    @Test
    @DisplayName("Acceso con token válido retorna datos del usuario")
    void accesoConTokenValido() throws Exception {
        LoginRequest loginReq = new LoginRequest();
        loginReq.setCorreo("test@example.com");
        loginReq.setPassword("password123");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginReq)))
                .andReturn();

        String response = result.getResponse().getContentAsString();
        String token = objectMapper.readTree(response).get("accessToken").asText();

        mockMvc.perform(get("/api/usuarios/me")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correo").value("test@example.com"));
    }

    @Test
    @DisplayName("Usuario autenticado sin rol administrativo recibe Problem Details 403")
    void accesoSinRolAdministrativo() throws Exception {
        LoginRequest loginReq = new LoginRequest();
        loginReq.setCorreo("test@example.com");
        loginReq.setPassword("password123");

        MvcResult login = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andReturn();
        String token = objectMapper.readTree(login.getResponse().getContentAsString())
                .get("accessToken").asText();

        mockMvc.perform(get("/api/usuarios")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("Forbidden"))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.instance").value("/api/usuarios"));
    }
}
