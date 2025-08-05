package com.example.apitask.dtos.tasks;

import com.example.apitask.dtos.categories.CategoriesResponseDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public record TasksResponseDTO(
        UUID id,
        String name,
        String description,
        boolean completed,
        LocalDate dateCreation,
        LocalDate dateConclusion,
        LocalDate dateExpiration,
        Set<CategoriesResponseDTO> categories,
        boolean firstTaskToday
) {
}
