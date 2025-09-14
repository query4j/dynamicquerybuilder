package com.github.query4j.examples.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Spring Boot test application for Query4j integration testing.
 * This minimal application provides the Spring context needed for
 * integration tests with embedded H2 database and JPA support.
 * 
 * @author query4j team
 * @version 1.0.0
 * @since 1.0.0
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.github.query4j.examples")
@EnableJpaRepositories(basePackages = "com.github.query4j.examples.repository")
@EntityScan(basePackages = "com.github.query4j.examples.entity")
@EnableTransactionManagement
public class TestApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }
}