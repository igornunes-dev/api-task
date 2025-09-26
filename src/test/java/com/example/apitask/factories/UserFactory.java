package com.example.apitask.factories;

import com.example.apitask.dtos.users.UsersRequestDTO;
import com.example.apitask.dtos.users.UsersResponseDTO;
import com.example.apitask.enums.UsersRole;
import com.example.apitask.models.Users;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserFactory {
    public Users createUser() {
        return createUser(0);
    }

    public Users createUser(int number) {
        Users user = new Users();
        String uuidSeed = "user-" + number;
        user.setId(UUID.nameUUIDFromBytes(uuidSeed.getBytes()));
        user.setEmail("user" + number + "@gmail.com");
        user.setPassword("password" + number);
        user.setPointers(number * 10);
        user.setStreakData(LocalDate.now().minusDays(number));
        user.setRole(UsersRole.USER);
        user.setTasks(new ArrayList<>());
        return user;
    }

    public List<Users> createUserList(int size) {
        List<Users> users = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            users.add(createUser(i));
        }
        return users;
    }

    public List<UsersResponseDTO> createUserListResponse(int size) {
        List<UsersResponseDTO> users = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            users.add(createUserResponse(i));
        }
        return users;
    }

    public UsersRequestDTO createUserRequest(int number) {
        return new UsersRequestDTO(
                "user" + number + "@gmail.com",
                "password" + number
        );
    }

    public UsersResponseDTO createUserResponse(int number) {
        String uuidSeed = "user-" + number;
        UUID id = UUID.nameUUIDFromBytes(uuidSeed.getBytes());
        return new UsersResponseDTO(
                id,
                "user" + number + "@gmail.com",
                UsersRole.USER
        );
    }
}
