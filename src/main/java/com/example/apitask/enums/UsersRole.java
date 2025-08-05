package com.example.apitask.enums;

import jakarta.persistence.Enumerated;

public enum UsersRole {
    USER("user");

    private String role;

    UsersRole(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }


}
