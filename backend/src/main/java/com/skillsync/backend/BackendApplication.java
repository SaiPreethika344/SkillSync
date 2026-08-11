package com.skillsync.backend;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.mail.javamail.JavaMailSender;

@SpringBootApplication
@EnableScheduling
public class BackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

    @Bean
    public CommandLineRunner checkMailBean(ApplicationContext ctx) {
        return args -> {
            // TEMP DIAGNOSTIC - remove after debugging
            String[] beans = ctx.getBeanNamesForType(JavaMailSender.class);
            System.out.println("### JavaMailSender beans found: " + beans.length);
            for (String beanName : beans) {
                System.out.println("### Bean name: " + beanName);
            }
        };
    }
}