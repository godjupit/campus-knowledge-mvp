package com.campus;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.campus.*.mapper")
@EnableScheduling
public class CampusKnowledgeMvpApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusKnowledgeMvpApplication.class, args);
    }
}
