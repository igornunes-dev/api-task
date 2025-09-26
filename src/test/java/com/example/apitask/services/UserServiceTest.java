package com.example.apitask.services;

import com.example.apitask.dtos.users.UsersRequestDTO;
import com.example.apitask.dtos.users.UsersResponseDTO;
import com.example.apitask.email.EmailPublisher;
import com.example.apitask.exceptions.EmailAlreadyExistsException;
import com.example.apitask.exceptions.ResourceNotFoundException;
import com.example.apitask.factories.UserFactory;
import com.example.apitask.helpers.HashPassword;
import com.example.apitask.infra.security.TokenService;
import com.example.apitask.mappers.UsersMapper;
import com.example.apitask.models.Users;
import com.example.apitask.repositories.UsersRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    private UserService userService;

    @Mock
    private UsersRepository usersRepository;

    @Mock
    private UsersMapper usersMapper;

    @Mock
    private HashPassword hashPassword;

    @Mock
    private TokenService tokenService;

    @Mock
    private EmailPublisher emailPublisher;

    private UserFactory usersFactory;

    @BeforeEach
    void setUp() {
        //Procure nessa class(this) todos os campos que tem a nomenclatura de @Mock e inicialize ele!
        MockitoAnnotations.openMocks(this);
        userService = new UserService(usersRepository, usersMapper, hashPassword, tokenService, emailPublisher);
        usersFactory = new UserFactory();
    }

    @Test
    void shouldCreateUserSuccessfully() {
        UsersRequestDTO requestDTO = usersFactory.createUserRequest(1);
        Users userEntity = usersFactory.createUser(1);
        UsersResponseDTO responseDTO = usersFactory.createUserResponse(1);

        when(usersMapper.toEntity(requestDTO)).thenReturn(userEntity);
        when(usersRepository.findByEmail(userEntity.getEmail())).thenReturn(Optional.empty());
        when(hashPassword.hashEncode(requestDTO.password())).thenReturn("hashed-password");
        when(usersMapper.toDTO(any(Users.class))).thenReturn(responseDTO);

        UsersResponseDTO result = userService.createUsers(requestDTO);

        assertThat(result).isNotNull();
        assertThat(result.email()).isEqualTo(requestDTO.email());
        verify(usersRepository).save(userEntity);
        verify(hashPassword).hashEncode(requestDTO.password());
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        UsersRequestDTO requestDTO = usersFactory.createUserRequest(1);
        Users userEntity = usersFactory.createUser(1);

        when(usersMapper.toEntity(requestDTO)).thenReturn(userEntity);
        when(usersRepository.findByEmail(userEntity.getEmail()))
                .thenReturn(Optional.of(userEntity));

        assertThatThrownBy(() -> userService.createUsers(requestDTO))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessage("This email is already registered.");

        verify(usersRepository, never()).save(any());
        verify(hashPassword, never()).hashEncode(any());
    }

    @Test
    void shouldReturnUserDetailsWhenUserExists() {
        String email = "test@example.com";
        UserDetails mockUser = org.springframework.security.core.userdetails.User
                .withUsername(email)
                .password("password")
                .roles("USER")
                .build();

        when(usersRepository.findByEmail(email)).thenReturn(Optional.of(mockUser));

        UserDetails result = userService.loadUserByUsername(email);

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo(email);
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        String email = "notfound@example.com";
        when(usersRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.loadUserByUsername(email))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User not found.");
    }

    @Test
    void shouldListUsersSuccessfully() {
        List<Users> user = usersFactory.createUserList(2);
        List<UsersResponseDTO> responseDTO = usersFactory.createUserListResponse(1);
        when(usersMapper.toListResponse(user)).thenReturn(responseDTO);
        when(usersRepository.findAll()).thenReturn(user);

        List<UsersResponseDTO> result = userService.listUsers();


        assertThat(result.getFirst().email()).isEqualTo(user.getFirst().getEmail());

        verify(usersRepository, times(1)).findAll();
        verify(usersMapper, times(1)).toListResponse(user);
    }

    @Test
    void shouldReturnEmptyListWhenNoUsersFound() {
        when(usersRepository.findAll()).thenReturn(Collections.emptyList());
        when(usersMapper.toListResponse(Collections.emptyList())).thenReturn(Collections.emptyList());

        List<UsersResponseDTO> result = userService.listUsers();

        assertTrue(result.isEmpty());

        verify(usersRepository, times(1)).findAll();
        verify(usersMapper, times(1)).toListResponse(Collections.emptyList());
    }

    @Test
    void shouldDeleteUserById() {
        Users user = usersFactory.createUser(1);
        doNothing().when(usersRepository).deleteById(user.getId());

        userService.deleteUserById(user.getId());

        verify(usersRepository, times(1)).deleteById(user.getId());

        verifyNoMoreInteractions(usersRepository, usersMapper, hashPassword, tokenService);
    }

    @Test
    void shouldReturnPointersWhenUserExists() {
        Users currentUser = new Users();
        currentUser.setId(UUID.randomUUID());

        Users userFromRepo = new Users();
        userFromRepo.setId(currentUser.getId());
        userFromRepo.setStreakData(LocalDate.now().minusDays(1));
        userFromRepo.setPointers(5);

        when(tokenService.getCurrentUser()).thenReturn(currentUser);
        when(usersRepository.findById(currentUser.getId())).thenReturn(Optional.of(userFromRepo));
        when(usersRepository.findPointersByUsersId(currentUser.getId())).thenReturn(userFromRepo.getPointers());
        when(usersRepository.save(userFromRepo)).thenReturn(userFromRepo);

        Integer result = userService.getSequenceByUser();

        assertThat(result).isEqualTo(userFromRepo.getPointers());
        verify(usersRepository, times(1)).findById(currentUser.getId());
        verify(usersRepository, times(1)).save(userFromRepo);
        verify(usersRepository, times(1)).findPointersByUsersId(currentUser.getId());
    }

    @Test
    void shouldResetPointersWhenStreakTooOld() {
        // Arrange
        Users currentUser = new Users();
        currentUser.setId(UUID.randomUUID());

        Users userFromRepo = new Users();
        userFromRepo.setId(currentUser.getId());
        userFromRepo.setStreakData(LocalDate.now().minusDays(5));
        userFromRepo.setPointers(10);

        when(tokenService.getCurrentUser()).thenReturn(currentUser);
        when(usersRepository.findById(currentUser.getId())).thenReturn(Optional.of(userFromRepo));
        when(usersRepository.findPointersByUsersId(currentUser.getId())).thenReturn(0);
        when(usersRepository.save(userFromRepo)).thenReturn(userFromRepo);

        // Act
        Integer result = userService.getSequenceByUser();

        // Assert
        assertThat(result).isEqualTo(0);
        assertThat(userFromRepo.getPointers()).isEqualTo(0);
        verify(usersRepository, times(1)).findById(currentUser.getId());
        verify(usersRepository, times(1)).save(userFromRepo);
        verify(usersRepository, times(1)).findPointersByUsersId(currentUser.getId());
    }

    @Test
    void shouldThrowExceptionWhenUserNotFoundInPoint() {
        // Arrange
        Users currentUser = new Users();
        currentUser.setId(UUID.randomUUID());

        when(tokenService.getCurrentUser()).thenReturn(currentUser);
        when(usersRepository.findById(currentUser.getId())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.getSequenceByUser())
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Users not fount");

        verify(usersRepository, times(1)).findById(currentUser.getId());
        verifyNoMoreInteractions(usersRepository);
    }



}
