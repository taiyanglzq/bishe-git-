package com.campus.assistant;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ????????????????????????
 */
@MapperScan("com.campus.assistant.mapper")
@SpringBootApplication
public class CampusAssistantApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusAssistantApplication.class, args);
    }
}
