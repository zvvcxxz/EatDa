package com.sp.app;

import org.springframework.ai.vectorstore.pgvector.autoconfigure.PgVectorStoreAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication(exclude = { PgVectorStoreAutoConfiguration.class })
public class EatDaApplication {

	public static void main(String[] args) {
		SpringApplication.run(EatDaApplication.class, args);
	}

}
