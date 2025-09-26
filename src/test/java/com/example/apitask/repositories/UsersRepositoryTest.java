package com.example.apitask.repositories;

import com.example.apitask.enums.UsersRole;
import com.example.apitask.models.Users;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
public class UsersRepositoryTest {

    @Test
    void contextLoads() {}

    @Autowired
    private UsersRepository usersRepository;

    @BeforeEach
    void setup() {
        usersRepository.deleteAll();
    }

    @Test
    void shouldReturnPointersByUserId() {
        Users users = new Users();
        users.setRole(UsersRole.USER);
        users.setPointers(0);
        users.setStreakData(null);
        users.setTasks(new ArrayList<>());
        users.setEmail("teste@gmail.com");
        users.setPassword("12345678eu");

        Users savedUser = usersRepository.saveAndFlush(users);

        Integer pointers = usersRepository.findPointersByUsersId(savedUser.getId());

        assertEquals(0, pointers);
    }

    @Test
    void shouldReturnEmptyOptionalWhenEmailNotFound() {
        Optional<UserDetails> result = usersRepository.findByEmail("inexistente@gmail.com");
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnNullWhenUserNotFound() {
        Integer pointers = usersRepository.findPointersByUsersId(UUID.randomUUID());
        assertNull(pointers);
    }

    @Test
    void shouldFindUserByEmail() {
        Users users = new Users();
        users.setRole(UsersRole.USER);
        users.setPointers(0);
        users.setStreakData(null);
        users.setTasks(new ArrayList<>());
        users.setEmail("email@gmail.com");
        users.setPassword("12345678eu");
        usersRepository.saveAndFlush(users);

        Optional<UserDetails> found = usersRepository.findByEmail("email@gmail.com");
        assertTrue(found.isPresent());
        assertEquals("email@gmail.com", found.get().getUsername());
    }

    @Test
    void shouldReturnEmptyWhenEmailNotFound() {
        Optional<UserDetails> found = usersRepository.findByEmail("nonee@gmail.com");
        assertTrue(found.isEmpty());
    }

}
