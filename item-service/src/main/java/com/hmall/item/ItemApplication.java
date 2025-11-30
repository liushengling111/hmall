package com.hmall.item;

import com.hmall.config.DefeatClientConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@MapperScan("com.hmall.item.mapper")
@SpringBootApplication
@EnableFeignClients(basePackages = "com.hmall.client", defaultConfiguration = DefeatClientConfiguration.class)
public class ItemApplication {
    public static void main(String[] args) {
        SpringApplication.run(ItemApplication.class, args);
    }
}