package com.example.apitask.services;

import com.example.apitask.dtos.tasks.TasksRequestDTO;
import com.example.apitask.dtos.tasks.TasksResponseDTO;
import com.example.apitask.exceptions.Authorization;
import com.example.apitask.exceptions.ResourceNotFoundException;
import com.example.apitask.infra.security.TokenService;
import com.example.apitask.mappers.CategoriesMapper;
import com.example.apitask.mappers.PageUtils;
import com.example.apitask.mappers.TasksMapper;
import com.example.apitask.models.Categories;
import com.example.apitask.models.Tasks;
import com.example.apitask.models.Users;
import com.example.apitask.repositories.CategoriesRepository;
import com.example.apitask.repositories.TasksRepository;
import com.example.apitask.repositories.UsersRepository;
import jakarta.validation.Valid;
import org.hibernate.Hibernate;
import org.springframework.cglib.core.Local;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.invoke.CallSite;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TaskService {
    private final TasksRepository tasksRepository;
    private final TasksMapper tasksMapper;
    private final TokenService tokenService;
    private final CategoriesRepository categoriesRepository;
    private final UsersRepository usersRepository;
    private final CategoriesMapper categoriesMapper;

    public TaskService(TasksRepository tasksRepository, TasksMapper tasksMapper, TokenService tokenService, CategoriesRepository categoriesRepository, UsersRepository usersRepository, CategoriesMapper categoriesMapper) {
        this.tasksRepository = tasksRepository;
        this.tasksMapper = tasksMapper;
        this.tokenService = tokenService;
        this.categoriesRepository = categoriesRepository;
        this.usersRepository = usersRepository;
        this.categoriesMapper = categoriesMapper;
    }

    public TasksResponseDTO createTask(@Valid TasksRequestDTO tasksRequestDTO) {
        Users currentUser = tokenService.getCurrentUser();
        Set<Categories> categories = tasksRequestDTO.categoryIds().stream()
                .map(id -> categoriesRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("category not found"))).collect(Collectors.toSet());

        Tasks tasks = tasksMapper.toEntity(tasksRequestDTO);
        tasks.setUsers(currentUser);
        tasks.setCategories(categories);
        tasksRepository.save(tasks);
        return tasksMapper.toDTO(tasks);
    }

    public List<TasksResponseDTO> findAllTasks() {
        List<Tasks> tasks = tasksRepository.findAll();
        return tasksMapper.toListResponse(tasks);
    }

    public TasksResponseDTO findTask(UUID id) {
        Users users = tokenService.getCurrentUser();
        Tasks tasks = tasksRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("task not found"));

        if(!tasks.getUsers().getId().equals(users.getId())) {
            throw new Authorization("you not have permission");
        }
        return tasksMapper.toDTO(tasks);
    }

    public TasksResponseDTO tasksCompleted(UUID id) {
        Users users = tokenService.getCurrentUser();
        Tasks tasks = tasksRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("task not found"));

        if(!tasks.getUsers().getId().equals(users.getId())) {
            throw new Authorization("You do not have permission to modify this task.");
        }
        boolean firstTaskToday = false;
        LocalDate today = LocalDate.now();
        if(!tasks.getCompleted()) {
            if (users.getStreakData() == null || !users.getStreakData().isEqual(today)) {
                users.setPointers(users.getPointers() + 1);
                users.setStreakData(today);
                usersRepository.save(users);
                firstTaskToday = true;
            }
        }

        tasks.setCompleted(true);
        tasks.setDateConclusion(today);
        tasksRepository.save(tasks);
        return mapWithFirstTaskFlag(tasks, firstTaskToday);
    }

    public void deleteTaskById(UUID id) {
        Users users = tokenService.getCurrentUser();
        Tasks tasks = tasksRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("task not found"));

        if(!tasks.getUsers().getId().equals(users.getId())) {
            throw new Authorization("you not have permission");
        }

        tasksRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Page<TasksResponseDTO> hasTasksToday(int page, int size) {
        Users users = tokenService.getCurrentUser();
        LocalDate today = LocalDate.now();
        Pageable pageable = PageRequest.of(page, size, Sort.by("dateExpiration").descending());
        Page<Tasks> tasks = tasksRepository.findTasksDueUpToDate(users.getId(), today, pageable);
        return PageUtils.mapPage(tasks, tasksMapper::toDTO);
    }

    public Page<TasksResponseDTO> existsTasksPending(int page, int size) {
        Users users = tokenService.getCurrentUser();
        Pageable pageable = PageRequest.of(page, size, Sort.by("dateExpiration").descending());
        Page<Tasks> tasks = tasksRepository.findByUsersIdAndCompletedFalse(users.getId(), pageable);
        return PageUtils.mapPage(tasks, tasksMapper::toDTO);
    }

    public Page<TasksResponseDTO> existsTasksCompleted(int page, int size) {
        Users users = tokenService.getCurrentUser();
        Pageable pageable = PageRequest.of(page, size, Sort.by("dateExpiration").descending());
        Page<Tasks> tasks = tasksRepository.findByUsersIdAndCompletedTrue(users.getId(), pageable);
        return PageUtils.mapPage(tasks, tasksMapper::toDTO);
    }

    public Page<TasksResponseDTO> getTasksByUser(int page, int size) {
        Users users = tokenService.getCurrentUser();
        Pageable pageable = PageRequest.of(page, size, Sort.by("dateExpiration").descending());
        Page<Tasks> tasks =  tasksRepository.findAllByUsersId(users.getId(), pageable);
        return PageUtils.mapPage(tasks, tasksMapper::toDTO);
    }

    public TasksResponseDTO mapWithFirstTaskFlag(Tasks task, boolean firstTaskToday) {
        return new TasksResponseDTO(
                task.getId(),
                task.getName(),
                task.getDescription(),
                task.getCompleted(),
                task.getDateCreation(),
                task.getDateConclusion(),
                task.getDateExpiration(),
                categoriesMapper.toSetResponse(task.getCategories()),
                firstTaskToday
        );
    }

    public int deleteCompletedTasksBefore() {
        Users user = tokenService.getCurrentUser();
        LocalDate limitDate = LocalDate.now().minusDays(30);
        return tasksRepository.deleteOldCompleted(user.getId(), limitDate);
    }



}
