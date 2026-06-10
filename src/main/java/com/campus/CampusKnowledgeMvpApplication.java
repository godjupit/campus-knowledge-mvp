package com.campus;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan({
        "com.campus.modules.auth.mapper",
        "com.campus.modules.knowledge.mapper",
        "com.campus.modules.interaction.mapper",
        "com.campus.modules.notification.mapper",
        "com.campus.infrastructure.event.mapper"
})
@EnableScheduling
public class CampusKnowledgeMvpApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusKnowledgeMvpApplication.class, args);
    }
}
