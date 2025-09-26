package com.example.apitask.factories;

import com.example.apitask.dtos.categories.CategoriesResponseDTO;
import com.example.apitask.dtos.tasks.TasksRequestDTO;
import com.example.apitask.dtos.tasks.TasksResponseDTO;
import com.example.apitask.models.Tasks;

import java.time.LocalDate;
import java.util.*;

public class TaskFactory {
    private final UserFactory usersFactory = new UserFactory();
    private final CategoryFactory categoriesFactory = new CategoryFactory();

    public Tasks createTask() {
        return createTask(0);
    }

    public Tasks createTask(int number) {
        Tasks task = new Tasks();
        String uuidSeed = "task-" + number;
        task.setId(UUID.nameUUIDFromBytes(uuidSeed.getBytes()));
        task.setName("Task " + number);
        task.setDescription("Description for task " + number);
        task.setCompleted(number % 2 == 0);
        task.setDateCreation(LocalDate.now().minusDays(number));
        task.setDateConclusion(number % 2 == 0 ? LocalDate.now() : null);
        task.setDateExpiration(LocalDate.now().plusDays(number));
        task.setUsers(usersFactory.createUser(number));
        task.setCategories(new HashSet<>(categoriesFactory.createCategoryList(2)));
        return task;
    }

    public List<Tasks> createTaskList(int size) {
        List<Tasks> tasks = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            tasks.add(createTask(i));
        }
        return tasks;
    }

    public TasksRequestDTO createTaskRequest(int number) {
        Set<UUID> categoryIds = new HashSet<>();
        categoriesFactory.createCategoryList(2)
                .forEach(cat -> categoryIds.add(cat.getId()));

        return new TasksRequestDTO(
                "Task " + number,
                "Description for task " + number,
                LocalDate.now().plusDays(number),
                categoryIds
        );
    }

    public TasksResponseDTO createTaskResponse(int number) {
        Set<CategoriesResponseDTO> categoryResponses = new HashSet<>(
                categoriesFactory.createCategoryResponseList(2)
        );

        String uuidSeed = "task-" + number;
        UUID id = UUID.nameUUIDFromBytes(uuidSeed.getBytes());

        return new TasksResponseDTO(
                id,
                "Task " + number,
                "Description for task " + number,
                number % 2 == 0,
                LocalDate.now().minusDays(number),
                number % 2 == 0 ? LocalDate.now() : null,
                LocalDate.now().plusDays(number),
                categoryResponses,
                number == 0
        );
    }
}
