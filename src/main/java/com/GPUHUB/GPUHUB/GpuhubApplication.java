package com.GPUHUB.GPUHUB;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.GPUHUB.GPUHUB.repositorio")
public class GpuhubApplication {

	public static void main(String[] args) {
		SpringApplication.run(GpuhubApplication.class, args);
	}

}
