package com.example.apitask.services;

import com.example.apitask.dtos.users.UsersRequestDTO;
import com.example.apitask.dtos.users.UsersResponseDTO;
import com.example.apitask.email.EmailPublisher;
import com.example.apitask.enums.UsersRole;
import com.example.apitask.exceptions.EmailAlreadyExistsException;
import com.example.apitask.exceptions.ResourceNotFoundException;
import com.example.apitask.helpers.HashPassword;
import com.example.apitask.infra.security.TokenService;
import com.example.apitask.mappers.UsersMapper;
import com.example.apitask.models.Users;
import com.example.apitask.repositories.UsersRepository;
import jakarta.validation.Valid;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService implements UserDetailsService {

    private final UsersRepository usersRepository;
    private final UsersMapper usersMapper;
    private final HashPassword hashPassword;
    private final TokenService tokenService;
    private final EmailPublisher emailPublisher;

    public UserService(UsersRepository usersRepository, UsersMapper usersMapper, HashPassword hashPassword, TokenService tokenService, EmailPublisher emailPublisher) {
        this.usersRepository = usersRepository;
        this.usersMapper = usersMapper;
        this.hashPassword = hashPassword;
        this.tokenService = tokenService;
        this.emailPublisher = emailPublisher;
    }


    public UsersResponseDTO createUsers(@Valid UsersRequestDTO usersRequestDTO) {
        Users users = usersMapper.toEntity(usersRequestDTO);
        Optional<UserDetails> userDetails = usersRepository.findByEmail(users.getEmail());
        if(userDetails.isPresent()) {
            throw new EmailAlreadyExistsException("This email is already registered.");
        }
        users.setPassword(hashPassword.hashEncode(usersRequestDTO.password()));
        users.setRole(UsersRole.USER);
        usersRepository.save(users);
        emailPublisher.sendWelcomeEmail(users);
        return usersMapper.toDTO(users);
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<UserDetails> users = usersRepository.findByEmail(username);
        if(users.isPresent()) {
            return users.get();
        } else {
            throw new UsernameNotFoundException("User not found.");
        }
    }

    public List<UsersResponseDTO> listUsers() {
        List<Users> users = usersRepository.findAll();
        return usersMapper.toListResponse(users);
    }

    public void deleteUserById(UUID id) {
        usersRepository.deleteById(id);
    }

    public Integer getSequenceByUser() {
        Users user = tokenService.getCurrentUser();
        LocalDate today = LocalDate.now();
        Users users = usersRepository.findById(user.getId()).orElseThrow(() -> new ResourceNotFoundException("Users not fount"));
        LocalDate streakDate = users.getStreakData();
        if(streakDate == null) {
            users.setPointers(0);
        } else {
            long daysSinceLast = ChronoUnit.DAYS.between(streakDate, today);
            if(daysSinceLast >= 2) {
                users.setPointers(0);
            }
        }
        usersRepository.save(users);
        return usersRepository.findPointersByUsersId(user.getId());
    }
}
