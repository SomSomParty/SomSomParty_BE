package com.acc.somsomparty;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class SomsompartyApplication {

	public static void main(String[] args) {
		SpringApplication.run(SomsompartyApplication.class, args);
	}

}
