package com.example.apitask.controllers;

import com.example.apitask.dtos.tasks.TasksRequestDTO;
import com.example.apitask.dtos.tasks.TasksResponseDTO;
import com.example.apitask.factories.CategoryFactory;
import com.example.apitask.factories.TaskFactory;
import com.example.apitask.infra.security.TokenService;
import com.example.apitask.repositories.UsersRepository;
import com.example.apitask.services.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
@AutoConfigureMockMvc(addFilters = false)
public class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsersRepository usersRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TaskService taskService;

    @MockitoBean
    private TokenService tokenService;

    private final TaskFactory taskFactory = new TaskFactory();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldCreateTaskSuccessfully() throws Exception {
        TasksRequestDTO requestDTO = taskFactory.createTaskRequest(1);
        TasksResponseDTO responseDTO = taskFactory.createTaskResponse(1);

        when(taskService.createTask(any(TasksRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/task/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(responseDTO.id().toString()))
                .andExpect(jsonPath("$.description").value(responseDTO.description()));

        verify(taskService, times(1)).createTask(any(TasksRequestDTO.class));
    }

    @Test
    void shouldReturnAllTasksSuccessfully() throws Exception {
        List<TasksResponseDTO> tasksList = List.of(
                taskFactory.createTaskResponse(1),
                taskFactory.createTaskResponse(2)
        );

        when(taskService.findAllTasks()).thenReturn(tasksList);

        mockMvc.perform(get("/task/findAll")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(tasksList.get(0).id().toString()))
                .andExpect(jsonPath("$[0].description").value(tasksList.get(0).description()))
                .andExpect(jsonPath("$[1].id").value(tasksList.get(1).id().toString()))
                .andExpect(jsonPath("$[1].description").value(tasksList.get(1).description()));

        verify(taskService, times(1)).findAllTasks();
    }

    @Test
    void shouldReturnTaskByIdSuccessfully() throws Exception {
        TasksResponseDTO responseDTO = taskFactory.createTaskResponse(1);
        UUID taskId = responseDTO.id();

        when(taskService.findTask(taskId)).thenReturn(responseDTO);

        mockMvc.perform(get("/task/findTask/{id}", taskId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(responseDTO.id().toString()))
                .andExpect(jsonPath("$.description").value(responseDTO.description()));

        verify(taskService, times(1)).findTask(taskId);
    }

    @Test
    void shouldToggleTaskCompletedSuccessfully() throws Exception {
        TasksResponseDTO responseDTO = taskFactory.createTaskResponse(1);
        UUID taskId = responseDTO.id();

        when(taskService.tasksCompleted(taskId)).thenReturn(responseDTO);

        mockMvc.perform(patch("/task/{id}/toggle", taskId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(responseDTO.id().toString()))
                .andExpect(jsonPath("$.description").value(responseDTO.description()));

        verify(taskService, times(1)).tasksCompleted(taskId);
    }

    @Test
    void shouldDeleteTaskSuccessfully() throws Exception {
        UUID taskId = UUID.randomUUID();

        doNothing().when(taskService).deleteTaskById(taskId);

        mockMvc.perform(delete("/task/{id}", taskId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(taskService, times(1)).deleteTaskById(taskId);
    }

    @Test
    void shouldReturnTasksForToday() throws Exception {
        List<TasksResponseDTO> tasksList = IntStream.range(0, 3)
                .mapToObj(taskFactory::createTaskResponse)
                .toList();

        Page<TasksResponseDTO> page = new PageImpl<>(tasksList);

        when(taskService.hasTasksToday(0, 10)).thenReturn(page);

        mockMvc.perform(get("/task/forToday")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(tasksList.size()))
                .andExpect(jsonPath("$.content[0].id").value(tasksList.get(0).id().toString()));

        verify(taskService, times(1)).hasTasksToday(0, 10);
    }

    @Test
    void shouldReturnPendingTasks() throws Exception {
        List<TasksResponseDTO> tasksList = IntStream.range(0, 3)
                .mapToObj(taskFactory::createTaskResponse)
                .toList();

        Page<TasksResponseDTO> page = new PageImpl<>(tasksList);

        when(taskService.existsTasksPending(0, 10)).thenReturn(page);

        mockMvc.perform(get("/task/hasTaskForPending")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(tasksList.size()))
                .andExpect(jsonPath("$.content[0].id").value(tasksList.get(0).id().toString()));

        verify(taskService, times(1)).existsTasksPending(0, 10);
    }

    @Test
    void shouldReturnCompletedTasks() throws Exception {
        List<TasksResponseDTO> tasksList = IntStream.range(0, 3)
                .mapToObj(taskFactory::createTaskResponse)
                .toList();

        Page<TasksResponseDTO> page = new PageImpl<>(tasksList);

        when(taskService.existsTasksCompleted(0, 10)).thenReturn(page);

        mockMvc.perform(get("/task/hasTaskForCompleted")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(tasksList.size()))
                .andExpect(jsonPath("$.content[0].id").value(tasksList.get(0).id().toString()));

        verify(taskService, times(1)).existsTasksCompleted(0, 10);
    }

    @Test
    void shouldReturnTasksByUser() throws Exception {
        // Cria uma lista de TasksResponseDTO simulando tasks do usuário
        List<TasksResponseDTO> tasksList = IntStream.range(0, 3)
                .mapToObj(taskFactory::createTaskResponse)
                .toList();

        // Cria uma página simulada
        Page<TasksResponseDTO> page = new PageImpl<>(tasksList);

        // Mocka o service
        when(taskService.getTasksByUser(0, 10)).thenReturn(page);

        mockMvc.perform(get("/task/findAllTasksByUsers")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(tasksList.size()))
                .andExpect(jsonPath("$.content[0].id").value(tasksList.get(0).id().toString()));

        verify(taskService, times(1)).getTasksByUser(0, 10);
    }

    @Test
    void shouldClearOldCompletedTasks() throws Exception {
        when(taskService.deleteCompletedTasksBefore()).thenReturn(3);

        mockMvc.perform(delete("/task/clearOldCompleted")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("3 old completed tasks deleted."));

        verify(taskService, times(1)).deleteCompletedTasksBefore();
    }

    @Test
    void shouldReturnMessageWhenNoOldTasks() throws Exception {
        when(taskService.deleteCompletedTasksBefore()).thenReturn(0);

        mockMvc.perform(delete("/task/clearOldCompleted")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("No old completed tasks to delete."));

        verify(taskService, times(1)).deleteCompletedTasksBefore();
    }

}
