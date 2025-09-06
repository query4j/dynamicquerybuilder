package com.github.query4j.examples;

import org.springframework.boot.SpringApplication;
import org.testcontainers.utility.TestcontainersConfiguration;

public class TestEcommerceApplication {
	
	public static void main(String[] args) {
		SpringApplication.from(EcommerceApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
