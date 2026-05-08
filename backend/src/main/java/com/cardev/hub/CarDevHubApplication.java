package com.cardev.hub;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 汽车研发知识共享平台启动类
 */
@SpringBootApplication
@MapperScan("com.cardev.hub.mapper")
public class CarDevHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(CarDevHubApplication.class, args);
    }
}
