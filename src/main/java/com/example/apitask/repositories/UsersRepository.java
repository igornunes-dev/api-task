package com.example.apitask.repositories;

import com.example.apitask.models.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsersRepository extends JpaRepository<Users, UUID> {
    Optional<UserDetails> findByEmail(String email);
    @Query("SELECT u.pointers FROM Users u WHERE u.id = :id")
    Integer findPointersByUsersId(@Param("id") UUID id);

    @Query("SELECT DISTINCT u FROM Users u LEFT JOIN FETCH u.tasks t WHERE t.completed = false AND t.dateExpiration <= :dateLimit")
    List<Users> findUserWithPendingTasks(@Param("dateLimit") LocalDate dateLimit);
}
