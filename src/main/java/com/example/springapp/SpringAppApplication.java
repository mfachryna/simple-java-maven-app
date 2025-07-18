package com.example.springapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication // Marks this as a Spring Boot application
@RestController      // Marks this class as a REST Controller
public class SpringAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringAppApplication.class, args);
    }

    @GetMapping("/hello") // Maps HTTP GET requests to /hello
    public String hello() {
        return "Hello from Spring Boot deployed by Jenkins!";
    }

    @GetMapping("/") // Maps HTTP GET requests to the root path
    public String rootHello() {
        return "Welcome to Spring Boot App!";
    }
}