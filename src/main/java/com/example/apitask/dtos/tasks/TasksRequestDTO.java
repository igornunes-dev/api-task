package com.example.apitask.dtos.tasks;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public record TasksRequestDTO(String name, String description, LocalDate dateExpiration, Set<UUID> categoryIds) {
}
