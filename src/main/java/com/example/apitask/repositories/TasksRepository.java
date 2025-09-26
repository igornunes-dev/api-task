package com.example.apitask.repositories;

import com.example.apitask.models.Tasks;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.scheduling.config.Task;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface TasksRepository extends JpaRepository<Tasks, UUID> {
    @Query("select t from Tasks t where t.users.id = :userId and t.dateExpiration <= :date and t.completed = false order by t.dateExpiration desc")
    Page<Tasks> findTasksDueUpToDate(@Param("userId") UUID userId, @Param("date") LocalDate date, Pageable pageable);

    Page<Tasks> findByUsersIdAndCompletedFalse(@Param("userId") UUID userId, Pageable pageable);

    Page<Tasks> findByUsersIdAndCompletedTrue(UUID userId, Pageable pageable);

    Page<Tasks> findAllByUsersId(UUID userId, Pageable pageable);

    @Transactional
    @Modifying
    @Query("delete from Tasks t where t.users.id = :userId and t.completed = true and t.dateConclusion < :limitDate")
    int deleteOldCompleted(@Param("userId") UUID userId, @Param("limitDate") LocalDate limitDate);

}