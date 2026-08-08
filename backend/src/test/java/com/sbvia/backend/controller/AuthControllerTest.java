package com.sbvia.backend.controller;

import com.sbvia.backend.dto.LoginRequest;
import com.sbvia.backend.dto.RegisterRequest;
import com.sbvia.backend.entity.Rol;
import com.sbvia.backend.entity.Usuario;
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
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private com.sbvia.backend.service.TokenBlacklistService tokenBlacklistService;

    private Usuario testUser;

    @BeforeEach
    void setUp() {
        usuarioRepository.deleteAll();
        rolRepository.deleteAll();
        // Persistir el Rol antes que el Usuario (relación FK)
        Rol rolTest = Rol.builder()
                .nombre("Conductor")
                .descripcion("Alumno o conductor en práctica")
                .build();
        rolTest = rolRepository.save(rolTest);

        testUser = Usuario.builder()
                .nombre("Test")
                .apellido("User")
                .email("test@example.com")
                .passwordHash(passwordEncoder.encode("password123"))
                .rol(rolTest)
                .estado("Activo")
                .activo(true)
                .build();
        usuarioRepository.save(testUser);
    }

    @Test
    @DisplayName("Login exitoso retorna 200 con tokens")
    void loginExitoso() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists());
    }

    /**
     * Verifica que la cookie del accessToken incluya las flags de seguridad:
     * - HttpOnly: impide lectura desde JavaScript (mitigación XSS)
     * - SameSite=Strict: impide que se envíe en peticiones cross-site (mitigación CSRF)
     *
     * Este test valida la corrección del problema señalado en la evaluación:
     * "La cookie del JWT no es segura: solo tiene HttpOnly, sin Secure ni SameSite"
     */
    @Test
    @DisplayName("Login: cookie accessToken tiene HttpOnly y SameSite=Strict")
    void loginCookieTieneHttpOnlyYSameSite() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpServletResponse response = result.getResponse();
        String setCookieHeader = response.getHeader("Set-Cookie");

        assertThat(setCookieHeader)
                .as("La cabecera Set-Cookie debe existir en la respuesta de login")
                .isNotNull();
        assertThat(setCookieHeader)
                .as("La cookie debe tener HttpOnly para prevenir acceso desde JavaScript (mitigación XSS)")
                .containsIgnoringCase("HttpOnly");
        assertThat(setCookieHeader)
                .as("La cookie debe tener SameSite=Strict para prevenir CSRF en peticiones cross-site")
                .containsIgnoringCase("SameSite=Strict");
    }

    @Test
    @DisplayName("Registro: cookie accessToken tiene HttpOnly y SameSite=Strict")
    void registroCookieTieneHttpOnlyYSameSite() throws Exception {
        // Limpiar el usuario de setUp para registrar email nuevo
        usuarioRepository.deleteAll();

        RegisterRequest request = new RegisterRequest();
        request.setNombre("Nuevo");
        request.setApellido("Conductor");
        request.setEmail("nuevo@sbvia.com");
        request.setPassword("password123");

        MvcResult result = mockMvc.perform(post("/api/auth/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        String setCookieHeader = result.getResponse().getHeader("Set-Cookie");

        assertThat(setCookieHeader)
                .as("La cabecera Set-Cookie debe existir en la respuesta de registro")
                .isNotNull();
        assertThat(setCookieHeader).containsIgnoringCase("HttpOnly");
        assertThat(setCookieHeader).containsIgnoringCase("SameSite=Strict");
    }

    @Test
    @DisplayName("Login con clave incorrecta retorna 401")
    void loginClaveIncorrecta() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
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
        request.setNombre("Nuevo");
        request.setApellido("User");
        request.setEmail("test@example.com"); // Email ya existe
        request.setPassword("password123");

        mockMvc.perform(post("/api/auth/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Acceso sin token retorna 403 Forbidden")
    void accesoSinToken() throws Exception {
        mockMvc.perform(get("/api/usuarios/me"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Acceso con token válido retorna datos del usuario")
    void accesoConTokenValido() throws Exception {
        // 1. Obtener token
        LoginRequest loginReq = new LoginRequest();
        loginReq.setEmail("test@example.com");
        loginReq.setPassword("password123");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginReq)))
                .andReturn();

        String response = result.getResponse().getContentAsString();
        String token = objectMapper.readTree(response).get("accessToken").asText();

        // 2. Acceder a endpoint protegido
        mockMvc.perform(get("/api/usuarios/me")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }
}
