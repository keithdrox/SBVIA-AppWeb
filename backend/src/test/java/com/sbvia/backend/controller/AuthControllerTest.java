package com.sbvia.backend.controller;

import com.sbvia.backend.dto.LoginRequest;
import com.sbvia.backend.dto.RegisterRequest;
import com.sbvia.backend.entity.Rol;
import com.sbvia.backend.entity.Usuario;
import com.sbvia.backend.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

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
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private com.sbvia.backend.service.TokenBlacklistService tokenBlacklistService;

    private Usuario testUser;

    @BeforeEach
    void setUp() {
        usuarioRepository.deleteAll();
        testUser = Usuario.builder()
                .nombre("Test")
                .apellido("User")
                .email("test@example.com")
                .passwordHash(passwordEncoder.encode("password123"))
                .rol(Rol.ROLE_USER)
                .activo(true)
                .build();
        usuarioRepository.save(testUser);
    }

    @Test
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

    @Test
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
    void accesoSinToken() throws Exception {
        mockMvc.perform(get("/api/usuarios/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
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
