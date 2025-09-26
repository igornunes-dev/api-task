package com.example.apitask.dtos.users;

import jakarta.validation.constraints.NotBlank;

public record UsersRequestDTO(@NotBlank String email, @NotBlank String password) {
}
