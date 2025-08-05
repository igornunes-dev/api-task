package com.example.apitask.controllers;

import com.example.apitask.dtos.users.UsersLoginDTO;
import com.example.apitask.dtos.users.UsersRequestDTO;
import com.example.apitask.dtos.users.UsersResponseDTO;
import com.example.apitask.exceptions.ResourceNotFoundException;
import com.example.apitask.infra.security.TokenService;
import com.example.apitask.models.Users;
import com.example.apitask.repositories.UsersRepository;
import com.example.apitask.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
public class UserController {
    private final UserService usersService;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final UsersRepository usersRepository;

    public UserController(UserService usersService, AuthenticationManager authenticationManager, TokenService tokenService, UsersRepository usersRepository) {
        this.usersService = usersService;
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
        this.usersRepository = usersRepository;
    }


    @PostMapping("/auth/create")
    public ResponseEntity<UsersResponseDTO> createUser(@RequestBody UsersRequestDTO usersRequestDTO) {
        UsersResponseDTO user = usersService.createUsers(usersRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @PostMapping("/auth/login")
    public ResponseEntity login(@RequestBody UsersRequestDTO usersRequestDTO) {
        var emailPassword = new UsernamePasswordAuthenticationToken(usersRequestDTO.email(), usersRequestDTO.password());
        var auth = this.authenticationManager.authenticate(emailPassword);
        var token = this.tokenService.generateToken((Users) auth.getPrincipal());
        String username = usersRequestDTO.email().split("@")[0];

        Optional<UserDetails> userDetailsOpt = usersRepository.findByEmail(usersRequestDTO.email());

        Users userFromDb = userDetailsOpt
                .filter(u -> u instanceof Users)
                .map(u -> (Users) u)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        LocalDate streakDate = userFromDb.getStreakData();
        System.out.println("StreakDate from DB: " + streakDate);

        return ResponseEntity.status(HttpStatus.OK).body(new UsersLoginDTO(token, username, streakDate));
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UsersResponseDTO>> listUsers() {
        List<UsersResponseDTO> usersResponseDTOS = usersService.listUsers();
        return ResponseEntity.status(HttpStatus.OK).body(usersResponseDTOS);
    }


    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUserById(@PathVariable("id") UUID id) {
        usersService.deleteUserById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/users/sequence")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Integer> getSequenceByUser() {
        Integer sequence = usersService.getSequenceByUser();
        return ResponseEntity.status(HttpStatus.OK).body(sequence);
    }
}
