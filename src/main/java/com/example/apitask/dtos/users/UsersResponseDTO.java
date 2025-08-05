package com.example.apitask.dtos.users;

import com.example.apitask.enums.UsersRole;

import java.util.UUID;

public record UsersResponseDTO(UUID id, String email, UsersRole role) {
}
