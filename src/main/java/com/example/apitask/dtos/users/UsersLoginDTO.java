package com.example.apitask.dtos.users;

import java.time.LocalDate;

public record UsersLoginDTO(String token, String username, LocalDate streakDate) {
}
