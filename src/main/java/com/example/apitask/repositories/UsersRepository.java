package com.example.apitask.repositories;

import com.example.apitask.models.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsersRepository extends JpaRepository<Users, UUID> {
    Optional<UserDetails> findByEmail(String email);
    @Query("SELECT u.pointers FROM Users u WHERE u.id = :id")
    Integer findPointersByUsersId(@Param("id") UUID id);
}
