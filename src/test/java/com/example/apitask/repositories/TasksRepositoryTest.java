package com.example.apitask.repositories;

import com.example.apitask.enums.UsersRole;
import com.example.apitask.models.Tasks;
import com.example.apitask.models.Users;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("test")
public class TasksRepositoryTest {
    @Test
    void contextLoads() {}

    @Autowired
    private TasksRepository tasksRepository;

    @Autowired
    private UsersRepository usersRepository;

    private Users user;

    @BeforeEach
    void setup() {
        user = new Users();
        user.setEmail("test@example.com");
        user.setPassword("123456");
        user.setRole(UsersRole.USER);
        usersRepository.saveAndFlush(user);

        Tasks task1 = new Tasks();
        task1.setName("Task 1");
        task1.setDescription("Descrição 1");
        task1.setCompleted(false);
        task1.setDateCreation(LocalDate.now().minusDays(5));
        task1.setDateExpiration(LocalDate.now().minusDays(1));
        task1.setUsers(user);

        Tasks task2 = new Tasks();
        task2.setName("Task 2");
        task2.setDescription("Descrição 2");
        task2.setCompleted(false);
        task2.setDateCreation(LocalDate.now().minusDays(3));
        task2.setDateExpiration(LocalDate.now());
        task2.setUsers(user);

        Tasks task3 = new Tasks();
        task3.setName("Task 3");
        task3.setDescription("Descrição 3");
        task3.setCompleted(true); // já concluída
        task3.setDateCreation(LocalDate.now().minusDays(2));
        task3.setDateExpiration(LocalDate.now());
        task3.setUsers(user);

        tasksRepository.saveAll(List.of(task1, task2, task3));
    }

    @Test
    void shouldReturnTasksDueUpToTodayForUser() {
        Pageable pageable = PageRequest.of(0, 10);
        LocalDate today = LocalDate.now();

        Page<Tasks> tasksPage = tasksRepository.findTasksDueUpToDate(user.getId(), today, pageable);

        assertEquals(2, tasksPage.getTotalElements());
        assertTrue(tasksPage.getContent().stream().allMatch(t -> !t.getCompleted()));
        assertTrue(tasksPage.getContent().stream().allMatch(t -> !t.getDateExpiration().isAfter(today)));

        assertTrue(tasksPage.getContent().get(0).getDateExpiration().isAfter(
                tasksPage.getContent().get(1).getDateExpiration()) ||
                tasksPage.getContent().get(0).getDateExpiration().isEqual(
                        tasksPage.getContent().get(1).getDateExpiration()));
    }

    @Test
    void shouldReturnEmptyWhenUserHasNoTasksDue() {
        Users newUser = new Users();
        newUser.setEmail("empty@example.com");
        newUser.setPassword("123456");
        newUser.setRole(UsersRole.USER);
        usersRepository.saveAndFlush(newUser);

        Pageable pageable = PageRequest.of(0, 10);
        LocalDate today = LocalDate.now();

        Page<Tasks> tasksPage = tasksRepository.findTasksDueUpToDate(newUser.getId(), today, pageable);

        assertTrue(tasksPage.isEmpty());
    }

    @Test
    void shouldReturnOnlyIncompleteTasksForUser() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<Tasks> tasksPage = tasksRepository.findByUsersIdAndCompletedFalse(user.getId(), pageable);

        assertEquals(2, tasksPage.getTotalElements());
        assertTrue(tasksPage.getContent().stream().allMatch(t -> !t.getCompleted()));
        assertTrue(tasksPage.getContent().stream().allMatch(t -> t.getUsers().getId().equals(user.getId())));
    }

    @Test
    void shouldReturnEmptyWhenUserHasNoTasks() {
        Users newUser = new Users();
        newUser.setEmail("empty@example.com");
        newUser.setPassword("123456");
        newUser.setRole(UsersRole.USER);
        usersRepository.saveAndFlush(newUser);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Tasks> tasksPage = tasksRepository.findByUsersIdAndCompletedFalse(newUser.getId(), pageable);

        assertTrue(tasksPage.isEmpty());
    }

    @Test
    void shouldReturnCompletedTasksForUser() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<Tasks> tasksPage = tasksRepository.findByUsersIdAndCompletedTrue(user.getId(), pageable);

        assertEquals(1, tasksPage.getTotalElements());
        assertTrue(tasksPage.getContent().stream().allMatch(Tasks::getCompleted));
    }


    @Test
    void shouldReturnEmptyWhenUserHasNoCompletedTasks() {
        UUID fakeUserId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);

        Page<Tasks> tasksPage = tasksRepository.findByUsersIdAndCompletedTrue(fakeUserId, pageable);

        assertTrue(tasksPage.isEmpty());
    }

    @Test
    void shouldReturnAllTasksForUser() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<Tasks> tasksPage = tasksRepository.findAllByUsersId(user.getId(), pageable);

        assertEquals(3, tasksPage.getTotalElements());
        assertTrue(tasksPage.getContent().stream().allMatch(t -> t.getUsers().getId().equals(user.getId())));
    }

    @Test
    void shouldReturnEmptyWhenUserByIdHasNoTasks() {
        UUID fakeUserId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);

        Page<Tasks> tasksPage = tasksRepository.findAllByUsersId(fakeUserId, pageable);

        assertTrue(tasksPage.isEmpty());
    }

    @Test
    void shouldDeleteOldCompletedTasksForUser() {
        LocalDate limitDate = LocalDate.now().minusDays(1);
        Tasks oldCompletedTask = new Tasks();
        oldCompletedTask.setName("Old Completed Task");
        oldCompletedTask.setDescription("Old completed description");
        oldCompletedTask.setCompleted(true);
        oldCompletedTask.setDateCreation(LocalDate.now().minusDays(10));
        oldCompletedTask.setDateExpiration(LocalDate.now().minusDays(9));
        oldCompletedTask.setDateConclusion(LocalDate.now().minusDays(5));
        oldCompletedTask.setUsers(user);
        tasksRepository.saveAndFlush(oldCompletedTask);

        int deletedCount = tasksRepository.deleteOldCompleted(user.getId(), limitDate);

        assertEquals(1, deletedCount);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Tasks> tasksPage = tasksRepository.findByUsersIdAndCompletedTrue(user.getId(), pageable);
        assertTrue(tasksPage.getContent().stream()
                .noneMatch(t -> t.getId().equals(oldCompletedTask.getId())));
    }

    @Test
    void shouldReturnZeroWhenNoOldCompletedTasksExist() {
        LocalDate limitDate = LocalDate.now().minusDays(100);
        int deletedCount = tasksRepository.deleteOldCompleted(user.getId(), limitDate);

        assertEquals(0, deletedCount);
    }




}
