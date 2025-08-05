package com.example.apitask.dtos.tasks;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public record TasksUpdateDTO(
        String description,
        LocalDate dateExpiration,
        Boolean completed,
        Set<UUID> categoryIds
) {
}
