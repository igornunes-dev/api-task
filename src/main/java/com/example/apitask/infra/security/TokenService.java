package com.example.apitask.infra.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.apitask.models.Users;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class TokenService {
    @Value("${jwt.secret}")
    String secret;

    public String generateToken(Users users) {
        try {

            Algorithm algorithm = Algorithm.HMAC256(secret);
            List<String> authorities = List.of("ROLE_" + users.getRole().name());

            return JWT.create()
                    .withIssuer("apitask")
                    .withSubject(users.getEmail())
                    .withClaim("id", users.getId().toString())
                    .withClaim("role", users.getRole().name())
                    .withClaim("authorities", authorities)
                    .withExpiresAt(genExpirationDate())
                    .sign(algorithm);
        }catch (JWTCreationException e) {
            throw new RuntimeException("Error while generating token", e);
        }
    }

    public String validateToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.require(algorithm)
                    .withIssuer("apitask")
                    .build()
                    .verify(token)
                    .getSubject();
        } catch (JWTVerificationException e) {
            return null;
        }
    }

    public Users getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authentication");
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof Users)) {
            throw new RuntimeException("main invalid");
        }

        return (Users) principal;
    }

    private Instant genExpirationDate(){
        return LocalDateTime.now().plusHours(2).toInstant(ZoneOffset.of("-03:00"));
    }

    public DecodedJWT decodeToken(String token) {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        return JWT.require(algorithm)
                .withIssuer("apitask")
                .build()
                .verify(token);
    }
}
