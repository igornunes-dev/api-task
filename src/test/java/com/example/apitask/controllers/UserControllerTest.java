package com.example.apitask.controllers;

import com.example.apitask.dtos.users.UsersRequestDTO;
import com.example.apitask.dtos.users.UsersResponseDTO;
import com.example.apitask.factories.UserFactory;
import com.example.apitask.infra.security.TokenService;
import com.example.apitask.models.Users;
import com.example.apitask.repositories.UsersRepository;
import com.example.apitask.services.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;


import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private UsersRepository usersRepository;

    private final UserFactory usersFactory = new UserFactory();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldCreateUserSuccessfully() throws Exception {
        UsersRequestDTO requestDTO = usersFactory.createUserRequest(1);
        UsersResponseDTO responseDTO = usersFactory.createUserResponse(1);

        when(userService.createUsers(any(UsersRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/auth/create")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(responseDTO.id().toString()))
                .andExpect(jsonPath("$.email").value(responseDTO.email()));

        verify(userService, times(1)).createUsers(any(UsersRequestDTO.class));
    }

    @Test
    void shouldReturnBadRequestWhenInvalidData() throws Exception {
        UsersRequestDTO invalidRequest = new UsersRequestDTO("", "");

        mockMvc.perform(post("/auth/create")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUsers(any());
    }

    @Test
    void shouldLoginSuccessfully() throws Exception {
        // Cria o request DTO
        UsersRequestDTO requestDTO = usersFactory.createUserRequest(1);

        // Cria um usuário simulado (principal) com dados do banco
        Users mockUser = new Users();
        mockUser.setEmail(requestDTO.email());
        mockUser.setStreakData(LocalDate.of(2025, 9, 19));

        // Mock da autenticação
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(mockUser);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);

        // Mock do token
        String fakeToken = "fake-jwt-token";
        when(tokenService.generateToken(mockUser)).thenReturn(fakeToken);

        // Mock do repositório
        when(usersRepository.findByEmail(requestDTO.email())).thenReturn(Optional.of(mockUser));

        // Executa a requisição
        mockMvc.perform(post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(fakeToken))
                .andExpect(jsonPath("$.username").value(requestDTO.email().split("@")[0]))
                .andExpect(jsonPath("$.streakDate").value(mockUser.getStreakData().toString()));

        // Verifica interações
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(tokenService, times(1)).generateToken(mockUser);
        verify(usersRepository, times(1)).findByEmail(requestDTO.email());
    }

    @Test
    void shouldListUsersSuccessfully() throws Exception {
        // Cria uma lista de usuários simulados
        List<UsersResponseDTO> mockUsers = List.of(
                usersFactory.createUserResponse(1),
                usersFactory.createUserResponse(2)
        );

        // Mock do service
        when(userService.listUsers()).thenReturn(mockUsers);

        // Executa a requisição
        mockMvc.perform(get("/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(mockUsers.size()))
                .andExpect(jsonPath("$[0].id").value(mockUsers.get(0).id().toString()))
                .andExpect(jsonPath("$[0].email").value(mockUsers.get(0).email()))
                .andExpect(jsonPath("$[1].id").value(mockUsers.get(1).id().toString()))
                .andExpect(jsonPath("$[1].email").value(mockUsers.get(1).email()));

        // Verifica se o service foi chamado
        verify(userService, times(1)).listUsers();
    }

    private final UUID sampleUserId = UUID.randomUUID();

    @Test
    void shouldDeleteUserSuccessfully() throws Exception {
        // Apenas simulamos que o método do service será chamado
        doNothing().when(userService).deleteUserById(sampleUserId);

        mockMvc.perform(delete("/users/{id}", sampleUserId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).deleteUserById(sampleUserId);
    }

    @Test
    void shouldReturnUserSequenceSuccessfully() throws Exception {
        Integer mockSequence = 42;
        when(userService.getSequenceByUser()).thenReturn(mockSequence);

        mockMvc.perform(get("/users/sequence")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(mockSequence.toString()));

        verify(userService, times(1)).getSequenceByUser();
    }
}

