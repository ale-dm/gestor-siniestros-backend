package com.portfolio.siniestros;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHashGeneratorTest {

    @Test
    void printHash() {
        String hash = new BCryptPasswordEncoder().encode("password123");
        System.out.println("=== HASH PARA password123 ===");
        System.out.println(hash);
        System.out.println("=============================");
    }
}
