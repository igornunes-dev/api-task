package com.example.apitask.services;

import com.example.apitask.dtos.categories.CategoriesResponseDTO;
import com.example.apitask.dtos.tasks.TasksRequestDTO;
import com.example.apitask.dtos.tasks.TasksResponseDTO;
import com.example.apitask.exceptions.Authorization;
import com.example.apitask.exceptions.ResourceNotFoundException;
import com.example.apitask.factories.CategoryFactory;
import com.example.apitask.factories.TaskFactory;
import com.example.apitask.factories.UserFactory;
import com.example.apitask.infra.security.TokenService;
import com.example.apitask.mappers.CategoriesMapper;
import com.example.apitask.mappers.TasksMapper;
import com.example.apitask.models.Categories;
import com.example.apitask.models.Tasks;
import com.example.apitask.models.Users;
import com.example.apitask.repositories.CategoriesRepository;
import com.example.apitask.repositories.TasksRepository;
import com.example.apitask.repositories.UsersRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {

    private TaskService taskService;

    @Mock
    private TasksRepository tasksRepository;

    @Mock
    private TasksMapper tasksMapper;

    @Mock
    private TokenService tokenService;

    @Mock
    private CategoriesRepository categoriesRepository;

    @Mock
    private UsersRepository usersRepository;

    @Mock
    private CategoriesMapper categoriesMapper;

    private TaskFactory taskFactory;
    private UserFactory usersFactory;
    private CategoryFactory categoryFactory;

    @BeforeEach
    void setUp() {
        //Procure nessa class(this) todos os campos que tem a nomenclatura de @Mock e inicialize ele!
        MockitoAnnotations.openMocks(this);
        taskService = new TaskService(tasksRepository, tasksMapper, tokenService, categoriesRepository, usersRepository, categoriesMapper);
        taskFactory = new TaskFactory();
        usersFactory = new UserFactory();
        categoryFactory = new CategoryFactory();
    }

    @Test
    void shouldCreateTaskSuccessfully() {
        // Arrange
        Users currentUser = usersFactory.createUser(1);
        Categories category = categoryFactory.createCategory(1);
        Set<UUID> categoryIds = Set.of(category.getId());

        TasksRequestDTO requestDTO = new TasksRequestDTO(
                "Task 1",
                "Description for task 1",
                LocalDate.now().plusDays(1),
                categoryIds
        );

        Tasks taskEntity = taskFactory.createTask(1);
        TasksResponseDTO responseDTO = taskFactory.createTaskResponse(1);

        when(tokenService.getCurrentUser()).thenReturn(currentUser);
        when(categoriesRepository.findById(category.getId())).thenReturn(Optional.of(category));
        when(tasksMapper.toEntity(requestDTO)).thenReturn(taskEntity);
        when(tasksMapper.toDTO(taskEntity)).thenReturn(responseDTO);

        // Act
        TasksResponseDTO result = taskService.createTask(requestDTO);

        // Assert
        assertThat(result).isEqualTo(responseDTO);
        assertThat(taskEntity.getUsers()).isEqualTo(currentUser);
        verify(tasksRepository, times(1)).save(taskEntity);
        verify(tasksMapper, times(1)).toEntity(requestDTO);
        verify(tasksMapper, times(1)).toDTO(taskEntity);
    }

    @Test
    void shouldThrowExceptionWhenCategoryNotFound() {
        Users currentUser = usersFactory.createUser(1);
        UUID nonExistentCategoryId = UUID.randomUUID();

        // Criar request manualmente
        TasksRequestDTO requestDTO = new TasksRequestDTO(
                "Task 1",
                "Description for task 1",
                LocalDate.now().plusDays(1),
                Set.of(nonExistentCategoryId)
        );

        when(tokenService.getCurrentUser()).thenReturn(currentUser);
        when(categoriesRepository.findById(nonExistentCategoryId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.createTask(requestDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("category not found");

        verify(categoriesRepository, times(1)).findById(nonExistentCategoryId);
        verifyNoMoreInteractions(tasksRepository, tasksMapper);
    }

    @Test
    void shouldFindAllTasksSuccessfully() {
        // Arrange
        List<Tasks> tasks = taskFactory.createTaskList(2);
        List<TasksResponseDTO> tasksResponse = List.of(
                taskFactory.createTaskResponse(0),
                taskFactory.createTaskResponse(1)
        );

        when(tasksRepository.findAll()).thenReturn(tasks);
        when(tasksMapper.toListResponse(tasks)).thenReturn(tasksResponse);

        // Act
        List<TasksResponseDTO> result = taskService.findAllTasks();

        // Assert
        assertThat(result).isEqualTo(tasksResponse);
        verify(tasksRepository, times(1)).findAll();
        verify(tasksMapper, times(1)).toListResponse(tasks);
    }

    @Test
    void shouldReturnEmptyListWhenNoTasksFound() {
        // Arrange
        when(tasksRepository.findAll()).thenReturn(Collections.emptyList());
        when(tasksMapper.toListResponse(Collections.emptyList())).thenReturn(Collections.emptyList());

        // Act
        List<TasksResponseDTO> result = taskService.findAllTasks();

        // Assert
        assertTrue(result.isEmpty());
        verify(tasksRepository, times(1)).findAll();
        verify(tasksMapper, times(1)).toListResponse(Collections.emptyList());
    }

    @Test
    void shouldFindTaskSuccessfully() {
        // Arrange
        Users currentUser = usersFactory.createUser(1);
        Tasks task = taskFactory.createTask(1);
        task.setUsers(currentUser); // garantir que o usuário é o dono
        UUID taskId = task.getId();
        TasksResponseDTO responseDTO = taskFactory.createTaskResponse(1);

        when(tokenService.getCurrentUser()).thenReturn(currentUser);
        when(tasksRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(tasksMapper.toDTO(task)).thenReturn(responseDTO);

        // Act
        TasksResponseDTO result = taskService.findTask(taskId);

        // Assert
        assertThat(result).isEqualTo(responseDTO);
        verify(tasksRepository, times(1)).findById(taskId);
        verify(tasksMapper, times(1)).toDTO(task);
    }

    @Test
    void shouldThrowExceptionWhenTaskNotFound() {
        // Arrange
        UUID taskId = UUID.randomUUID();
        when(tokenService.getCurrentUser()).thenReturn(usersFactory.createUser(1));
        when(tasksRepository.findById(taskId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> taskService.findTask(taskId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("task not found");

        verify(tasksRepository, times(1)).findById(taskId);
        verifyNoInteractions(tasksMapper);
    }

    @Test
    void shouldThrowExceptionWhenUserNotOwner() {
        // Arrange
        Users currentUser = usersFactory.createUser(1);
        Users anotherUser = usersFactory.createUser(2);
        Tasks task = taskFactory.createTask(1);
        task.setUsers(anotherUser);
        UUID taskId = task.getId();

        when(tokenService.getCurrentUser()).thenReturn(currentUser);
        when(tasksRepository.findById(taskId)).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> taskService.findTask(taskId))
                .isInstanceOf(Authorization.class)
                .hasMessage("you not have permission");

        verify(tasksRepository, times(1)).findById(taskId);
        verifyNoInteractions(tasksMapper);
    }

    @Test
    void shouldCompleteTaskSuccessfullyAndIncrementPointer() {
        // Arrange
        Users currentUser = usersFactory.createUser(1);
        currentUser.setStreakData(null);
        Tasks task = taskFactory.createTask(1);
        task.setUsers(currentUser);
        task.setCompleted(false);
        UUID taskId = task.getId();
        TasksResponseDTO responseDTO = taskFactory.createTaskResponse(1);

        when(tokenService.getCurrentUser()).thenReturn(currentUser);
        when(tasksRepository.findById(taskId)).thenReturn(Optional.of(task));
        lenient().doAnswer(invocation -> {
            Tasks t = invocation.getArgument(0);
            boolean firstTaskFlag = invocation.getArgument(1);
            return new TasksResponseDTO(
                    t.getId(),
                    t.getName(),
                    t.getDescription(),
                    t.getCompleted(),
                    t.getDateCreation(),
                    t.getDateConclusion(),
                    t.getDateExpiration(),
                    t.getCategories().stream()
                            .map(cat -> new CategoriesResponseDTO(cat.getId(), cat.getName()))
                            .collect(Collectors.toSet()),
                    firstTaskFlag
            );
        }).when(tasksMapper).toDTO(any());

        // Act
        TasksResponseDTO result = taskService.tasksCompleted(taskId);

        assertThat(task.getCompleted()).isTrue();
        assertThat(task.getDateConclusion()).isEqualTo(LocalDate.now());
        assertThat(currentUser.getPointers()).isEqualTo(11);
        assertThat(currentUser.getStreakData()).isEqualTo(LocalDate.now());
        verify(usersRepository, times(1)).save(currentUser);
        verify(tasksRepository, times(1)).save(task);
    }

    @Test
    void shouldThrowAuthorizationWhenUserNotOwner() {
        // Arrange
        Users currentUser = usersFactory.createUser(1);
        Users anotherUser = usersFactory.createUser(2);
        Tasks task = taskFactory.createTask(1);
        task.setUsers(anotherUser);
        UUID taskId = task.getId();

        when(tokenService.getCurrentUser()).thenReturn(currentUser);
        when(tasksRepository.findById(taskId)).thenReturn(Optional.of(task));

        // Act & Assert
        assertThatThrownBy(() -> taskService.tasksCompleted(taskId))
                .isInstanceOf(Authorization.class)
                .hasMessage("You do not have permission to modify this task.");

        verify(tasksRepository, times(1)).findById(taskId);
        verifyNoMoreInteractions(tasksRepository);
        verifyNoInteractions(usersRepository);
    }

    @Test
    void shouldNotIncrementPointerIfTaskAlreadyCompleted() {
        // Arrange
        Users currentUser = usersFactory.createUser(1);
        currentUser.setStreakData(LocalDate.now());
        currentUser.setPointers(5);

        Tasks task = taskFactory.createTask(1);
        task.setUsers(currentUser);
        task.setCompleted(true);
        task.setDateConclusion(LocalDate.now().minusDays(1));
        UUID taskId = task.getId();

        when(tokenService.getCurrentUser()).thenReturn(currentUser);
        when(tasksRepository.findById(taskId)).thenReturn(Optional.of(task));

        // Act
        TasksResponseDTO result = taskService.tasksCompleted(taskId);

        assertThat(task.getCompleted()).isTrue();
        assertThat(task.getDateConclusion()).isEqualTo(LocalDate.now());
        assertThat(currentUser.getPointers()).isEqualTo(5); // não incrementou
        verify(usersRepository, never()).save(currentUser); // não salva
        verify(tasksRepository, times(1)).save(task);
    }

    @Test
    void shouldDeleteTaskSuccessfully() {
        // Arrange
        Users currentUser = usersFactory.createUser(1);
        Tasks task = taskFactory.createTask(1);
        task.setUsers(currentUser);
        UUID taskId = task.getId();

        when(tokenService.getCurrentUser()).thenReturn(currentUser);
        when(tasksRepository.findById(taskId)).thenReturn(Optional.of(task));
        doNothing().when(tasksRepository).deleteById(taskId);

        // Act
        taskService.deleteTaskById(taskId);

        // Assert
        verify(tasksRepository, times(1)).findById(taskId);
        verify(tasksRepository, times(1)).deleteById(taskId);
        verifyNoMoreInteractions(tasksRepository);
        verifyNoInteractions(tasksMapper, categoriesRepository, usersRepository);
    }

    @Test
    void shouldThrowExceptionWhenUserNotAuthorized() {
        // Arrange
        Users currentUser = usersFactory.createUser(1);
        Users anotherUser = usersFactory.createUser(2);
        Tasks task = taskFactory.createTask(1);
        task.setUsers(anotherUser);
        UUID taskId = task.getId();

        when(tokenService.getCurrentUser()).thenReturn(currentUser);
        when(tasksRepository.findById(taskId)).thenReturn(Optional.of(task));

        // Act & Assert
        assertThatThrownBy(() -> taskService.deleteTaskById(taskId))
                .isInstanceOf(Authorization.class)
                .hasMessage("you not have permission");

        verify(tasksRepository, times(1)).findById(taskId);
        verify(tasksRepository, never()).deleteById(any());
    }

    @Test
    void shouldReturnTasksForTodaySuccessfully() {
        // Arrange
        Users currentUser = usersFactory.createUser(1);
        int page = 0;
        int size = 2;
        LocalDate today = LocalDate.now();

        Tasks task1 = taskFactory.createTask(1);
        task1.setUsers(currentUser);
        Tasks task2 = taskFactory.createTask(2);
        task2.setUsers(currentUser);

        List<Tasks> tasksList = List.of(task1, task2);
        Page<Tasks> tasksPage = new PageImpl<>(tasksList);

        when(tokenService.getCurrentUser()).thenReturn(currentUser);
        when(tasksRepository.findTasksDueUpToDate(currentUser.getId(), today, PageRequest.of(page, size, Sort.by("dateExpiration").descending())))
                .thenReturn(tasksPage);
        // Simulando o mapeamento da página
        when(tasksMapper.toDTO(task1)).thenReturn(taskFactory.createTaskResponse(1));
        when(tasksMapper.toDTO(task2)).thenReturn(taskFactory.createTaskResponse(2));

        // Act
        Page<TasksResponseDTO> result = taskService.hasTasksToday(page, size);

        assertThat(result.getContent().get(0).id()).isEqualTo(task1.getId());
        assertThat(result.getContent().get(1).id()).isEqualTo(task2.getId());

        verify(tokenService, times(1)).getCurrentUser();
        verify(tasksRepository, times(1))
                .findTasksDueUpToDate(currentUser.getId(), today, PageRequest.of(page, size, Sort.by("dateExpiration").descending()));
        verify(tasksMapper, times(1)).toDTO(task1);
        verify(tasksMapper, times(1)).toDTO(task2);
    }

    @Test
    void shouldReturnEmptyPageWhenNoTasksForToday() {
        // Arrange
        Users currentUser = usersFactory.createUser(1);
        int page = 0;
        int size = 2;
        LocalDate today = LocalDate.now();

        Page<Tasks> emptyTasksPage = new PageImpl<>(Collections.emptyList());

        when(tokenService.getCurrentUser()).thenReturn(currentUser);
        when(tasksRepository.findTasksDueUpToDate(currentUser.getId(), today, PageRequest.of(page, size, Sort.by("dateExpiration").descending())))
                .thenReturn(emptyTasksPage);

        // Act
        Page<TasksResponseDTO> result = taskService.hasTasksToday(page, size);

        verify(tokenService, times(1)).getCurrentUser();
        verify(tasksRepository, times(1))
                .findTasksDueUpToDate(currentUser.getId(), today, PageRequest.of(page, size, Sort.by("dateExpiration").descending()));
        verifyNoInteractions(tasksMapper);
    }

    @Test
    void shouldReturnPendingTasksSuccessfully() {
        // Arrange
        Users currentUser = usersFactory.createUser(1);
        int page = 0;
        int size = 2;

        Tasks task1 = taskFactory.createTask(1);
        task1.setUsers(currentUser);
        task1.setCompleted(false);

        Tasks task2 = taskFactory.createTask(2);
        task2.setUsers(currentUser);
        task2.setCompleted(false);

        List<Tasks> tasksList = List.of(task1, task2);
        Page<Tasks> tasksPage = new PageImpl<>(tasksList);

        when(tokenService.getCurrentUser()).thenReturn(currentUser);
        when(tasksRepository.findByUsersIdAndCompletedFalse(
                currentUser.getId(),
                PageRequest.of(page, size, Sort.by("dateExpiration").descending())
        )).thenReturn(tasksPage);

        when(tasksMapper.toDTO(task1)).thenReturn(taskFactory.createTaskResponse(1));
        when(tasksMapper.toDTO(task2)).thenReturn(taskFactory.createTaskResponse(2));

        // Act
        Page<TasksResponseDTO> result = taskService.existsTasksPending(page, size);


        assertThat(result.getContent().get(0).id()).isEqualTo(task1.getId());
        assertThat(result.getContent().get(1).id()).isEqualTo(task2.getId());

        verify(tokenService, times(1)).getCurrentUser();
        verify(tasksRepository, times(1))
                .findByUsersIdAndCompletedFalse(currentUser.getId(), PageRequest.of(page, size, Sort.by("dateExpiration").descending()));
        verify(tasksMapper, times(1)).toDTO(task1);
        verify(tasksMapper, times(1)).toDTO(task2);
    }

    @Test
    void shouldReturnEmptyPageWhenNoPendingTasks() {
        // Arrange
        Users currentUser = usersFactory.createUser(1);
        int page = 0;
        int size = 2;

        Page<Tasks> emptyTasksPage = new PageImpl<>(Collections.emptyList());

        when(tokenService.getCurrentUser()).thenReturn(currentUser);
        when(tasksRepository.findByUsersIdAndCompletedFalse(
                currentUser.getId(),
                PageRequest.of(page, size, Sort.by("dateExpiration").descending())
        )).thenReturn(emptyTasksPage);

        // Act
        Page<TasksResponseDTO> result = taskService.existsTasksPending(page, size);


        verify(tokenService, times(1)).getCurrentUser();
        verify(tasksRepository, times(1))
                .findByUsersIdAndCompletedFalse(currentUser.getId(), PageRequest.of(page, size, Sort.by("dateExpiration").descending()));
        verifyNoInteractions(tasksMapper);
    }

    @Test
    void shouldReturnCompletedTasksSuccessfully() {
        // Arrange
        Users currentUser = usersFactory.createUser(1);
        int page = 0;
        int size = 2;

        Tasks task1 = taskFactory.createTask(1);
        task1.setUsers(currentUser);
        task1.setCompleted(true);

        Tasks task2 = taskFactory.createTask(2);
        task2.setUsers(currentUser);
        task2.setCompleted(true);

        List<Tasks> tasksList = List.of(task1, task2);
        Page<Tasks> tasksPage = new PageImpl<>(tasksList);

        when(tokenService.getCurrentUser()).thenReturn(currentUser);
        when(tasksRepository.findByUsersIdAndCompletedTrue(
                currentUser.getId(),
                PageRequest.of(page, size, Sort.by("dateExpiration").descending())
        )).thenReturn(tasksPage);

        when(tasksMapper.toDTO(task1)).thenReturn(taskFactory.createTaskResponse(1));
        when(tasksMapper.toDTO(task2)).thenReturn(taskFactory.createTaskResponse(2));

        // Act
        Page<TasksResponseDTO> result = taskService.existsTasksCompleted(page, size);

        assertThat(result.getContent().get(0).id()).isEqualTo(task1.getId());
        assertThat(result.getContent().get(1).id()).isEqualTo(task2.getId());

        verify(tokenService, times(1)).getCurrentUser();
        verify(tasksRepository, times(1))
                .findByUsersIdAndCompletedTrue(currentUser.getId(), PageRequest.of(page, size, Sort.by("dateExpiration").descending()));
        verify(tasksMapper, times(1)).toDTO(task1);
        verify(tasksMapper, times(1)).toDTO(task2);
    }

    @Test
    void shouldReturnEmptyPageWhenNoCompletedTasks() {
        // Arrange
        Users currentUser = usersFactory.createUser(1);
        int page = 0;
        int size = 2;

        Page<Tasks> emptyTasksPage = new PageImpl<>(Collections.emptyList());

        when(tokenService.getCurrentUser()).thenReturn(currentUser);
        when(tasksRepository.findByUsersIdAndCompletedTrue(
                currentUser.getId(),
                PageRequest.of(page, size, Sort.by("dateExpiration").descending())
        )).thenReturn(emptyTasksPage);

        // Act
        Page<TasksResponseDTO> result = taskService.existsTasksCompleted(page, size);


        verify(tokenService, times(1)).getCurrentUser();
        verify(tasksRepository, times(1))
                .findByUsersIdAndCompletedTrue(currentUser.getId(), PageRequest.of(page, size, Sort.by("dateExpiration").descending()));
        verifyNoInteractions(tasksMapper);
    }

    @Test
    void shouldReturnTasksByUserSuccessfully() {
        // Arrange
        Users currentUser = usersFactory.createUser(1);
        int page = 0;
        int size = 2;

        Tasks task1 = taskFactory.createTask(1);
        task1.setUsers(currentUser);

        Tasks task2 = taskFactory.createTask(2);
        task2.setUsers(currentUser);

        List<Tasks> tasksList = List.of(task1, task2);
        Page<Tasks> tasksPage = new PageImpl<>(tasksList);

        when(tokenService.getCurrentUser()).thenReturn(currentUser);
        when(tasksRepository.findAllByUsersId(
                currentUser.getId(),
                PageRequest.of(page, size, Sort.by("dateExpiration").descending())
        )).thenReturn(tasksPage);

        when(tasksMapper.toDTO(task1)).thenReturn(taskFactory.createTaskResponse(1));
        when(tasksMapper.toDTO(task2)).thenReturn(taskFactory.createTaskResponse(2));

        // Act
        Page<TasksResponseDTO> result = taskService.getTasksByUser(page, size);

        assertThat(result.getContent().get(0).id()).isEqualTo(task1.getId());
        assertThat(result.getContent().get(1).id()).isEqualTo(task2.getId());

        verify(tokenService, times(1)).getCurrentUser();
        verify(tasksRepository, times(1))
                .findAllByUsersId(currentUser.getId(), PageRequest.of(page, size, Sort.by("dateExpiration").descending()));
        verify(tasksMapper, times(1)).toDTO(task1);
        verify(tasksMapper, times(1)).toDTO(task2);
    }

    @Test
    void shouldReturnEmptyPageWhenUserHasNoTasks() {
        // Arrange
        Users currentUser = usersFactory.createUser(1);
        int page = 0;
        int size = 2;

        Page<Tasks> emptyTasksPage = new PageImpl<>(Collections.emptyList());

        when(tokenService.getCurrentUser()).thenReturn(currentUser);
        when(tasksRepository.findAllByUsersId(
                currentUser.getId(),
                PageRequest.of(page, size, Sort.by("dateExpiration").descending())
        )).thenReturn(emptyTasksPage);

        // Act
        Page<TasksResponseDTO> result = taskService.getTasksByUser(page, size);


        verify(tokenService, times(1)).getCurrentUser();
        verify(tasksRepository, times(1))
                .findAllByUsersId(currentUser.getId(), PageRequest.of(page, size, Sort.by("dateExpiration").descending()));
        verifyNoInteractions(tasksMapper);
    }

    @Test
    void shouldMapTaskWithFirstTaskFlagSuccessfully() {
        // Arrange
        Tasks task = taskFactory.createTask(1);
        boolean firstTaskToday = true;

        Set<CategoriesResponseDTO> categoryResponses = task.getCategories().stream()
                .map(cat -> new CategoriesResponseDTO(cat.getId(), cat.getName()))
                .collect(Collectors.toSet());

        when(categoriesMapper.toSetResponse(task.getCategories())).thenReturn(categoryResponses);

        TasksResponseDTO result = taskService.mapWithFirstTaskFlag(task, firstTaskToday);

        assertThat(result.id()).isEqualTo(task.getId());
        assertThat(result.name()).isEqualTo(task.getName());
        assertThat(result.description()).isEqualTo(task.getDescription());
        assertThat(result.completed()).isEqualTo(task.getCompleted());
        assertThat(result.dateCreation()).isEqualTo(task.getDateCreation());
        assertThat(result.dateConclusion()).isEqualTo(task.getDateConclusion());
        assertThat(result.dateExpiration()).isEqualTo(task.getDateExpiration());
        assertThat(result.categories()).isEqualTo(categoryResponses);
        assertThat(result.firstTaskToday()).isEqualTo(firstTaskToday);

        verify(categoriesMapper, times(1)).toSetResponse(task.getCategories());
    }

    @Test
    void shouldDeleteCompletedTasksBeforeSuccessfully() {
        Users currentUser = usersFactory.createUser(1);
        when(tokenService.getCurrentUser()).thenReturn(currentUser);

        LocalDate limitDate = LocalDate.now().minusDays(30);
        int deletedCount = 5;

        when(tasksRepository.deleteOldCompleted(currentUser.getId(), limitDate)).thenReturn(deletedCount);

        int result = taskService.deleteCompletedTasksBefore();

        assertThat(result).isEqualTo(deletedCount);
        verify(tokenService, times(1)).getCurrentUser();
        verify(tasksRepository, times(1)).deleteOldCompleted(currentUser.getId(), limitDate);
    }


}
