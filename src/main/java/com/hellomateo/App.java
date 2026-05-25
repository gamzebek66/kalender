package com.hellomateo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.nio.file.Files;
import java.nio.file.Paths;

@SpringBootApplication
public class App {

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @Bean
    public CommandLineRunner init() {
        return args -> {

            try {
                Files.createDirectories(Paths.get("uploads"));
                System.out.println("Uploads-Ordner erstellt ✔");

            } catch (Exception e) {
                e.printStackTrace();
            }
        };
    }
}