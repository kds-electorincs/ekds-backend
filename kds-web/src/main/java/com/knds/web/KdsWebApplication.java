package com.knds.web;

import com.knds.service.security.InvitationProperties;
import com.knds.service.security.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.knds")
@EnableJpaRepositories(basePackages = "com.knds.repository")
@EntityScan(basePackages = "com.knds.entities")
@EnableConfigurationProperties({JwtProperties.class, InvitationProperties.class})
public class KdsWebApplication {

	public static void main(String[] args) {
		SpringApplication.run(KdsWebApplication.class, args);
	}
}
