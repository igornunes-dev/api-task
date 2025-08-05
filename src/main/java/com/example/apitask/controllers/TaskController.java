package com.example.apitask.controllers;

import com.example.apitask.dtos.tasks.TasksRequestDTO;
import com.example.apitask.dtos.tasks.TasksResponseDTO;
import com.example.apitask.dtos.users.UsersResponseDTO;
import com.example.apitask.infra.security.TokenService;
import com.example.apitask.models.Users;
import com.example.apitask.services.TaskService;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/task")
@PreAuthorize("hasRole('USER')")
public class TaskController {
    private final TaskService taskService;
    private final TokenService tokenService;

    public TaskController(TaskService taskService, TokenService tokenService) {
        this.taskService = taskService;
        this.tokenService = tokenService;
    }

    @PostMapping("/create")
    public ResponseEntity<TasksResponseDTO> createTasks(@RequestBody TasksRequestDTO tasksRequestDTO) {
        TasksResponseDTO tasksResponseDTO = taskService.createTask(tasksRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(tasksResponseDTO);
    }

    @GetMapping("/findAll")
    public ResponseEntity<List<TasksResponseDTO>> findTaskAll() {
        List<TasksResponseDTO> tasksResponseDTOS = taskService.findAllTasks();
        return ResponseEntity.status(HttpStatus.OK).body(tasksResponseDTOS);
    }

    @GetMapping("/findTask/{id}")
    public ResponseEntity<TasksResponseDTO> findTaskById(@PathVariable("id") UUID id) {
        TasksResponseDTO tasksResponseDTO = taskService.findTask(id);
        return ResponseEntity.status(HttpStatus.OK).body(tasksResponseDTO);
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<TasksResponseDTO> taskCompleted(@PathVariable("id") UUID id) {
        TasksResponseDTO tasksResponseDTO = taskService.tasksCompleted(id);
        return ResponseEntity.status(HttpStatus.OK).body(tasksResponseDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTaskById(@PathVariable("id") UUID id) {
        taskService.deleteTaskById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/forToday")
    public ResponseEntity<Page<TasksResponseDTO>> hasTasksToday(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<TasksResponseDTO> exists = taskService.hasTasksToday(page, size);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/hasTaskForPending")
    public ResponseEntity<Page<TasksResponseDTO>> hasTasksPending(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<TasksResponseDTO> exists = taskService.existsTasksPending(page, size);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/hasTaskForCompleted")
    public ResponseEntity<Page<TasksResponseDTO>> hasTasksCompleted(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<TasksResponseDTO> exists = taskService.existsTasksCompleted(page, size);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/findAllTasksByUsers")
    public ResponseEntity<Page<TasksResponseDTO>> getAllTaskByUser(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<TasksResponseDTO> tasksResponseDTOS = taskService.getTasksByUser(page, size);
        return ResponseEntity.ok(tasksResponseDTOS);
    }

    @DeleteMapping("/clearOldCompleted")
    public ResponseEntity<String> clearOldCompleted() {
        int count = taskService.deleteCompletedTasksBefore();
        if (count == 0) {
            return ResponseEntity.ok("No old completed tasks to delete.");
        }
        return ResponseEntity.ok(count + " old completed tasks deleted.");
    }


}
