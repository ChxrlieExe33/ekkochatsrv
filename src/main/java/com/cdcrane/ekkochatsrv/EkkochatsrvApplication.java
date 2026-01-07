package com.cdcrane.ekkochatsrv;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class EkkochatsrvApplication {

    public static void main(String[] args) {
        SpringApplication.run(EkkochatsrvApplication.class, args);
    }


    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {
            System.out.println("\n--------------------------------------------------------------------");
            System.out.println("EkkoChat server was started successfully!");
            System.out.println("--------------------------------------------------------------------\n");
        };
    }
}
