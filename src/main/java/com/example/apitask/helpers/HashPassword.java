package com.example.apitask.helpers;

import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class HashPassword {
   private final PasswordEncoder encoder;

    public HashPassword(PasswordEncoder encoder) {
        this.encoder = encoder;
    }

    public String hashEncode(String password) {
        return encoder.encode(password);
    }
}
