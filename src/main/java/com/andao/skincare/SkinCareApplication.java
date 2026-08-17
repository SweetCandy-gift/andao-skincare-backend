package com.andao.skincare;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.andao.skincare.module.*.mapper")
@SpringBootApplication
public class SkinCareApplication {

    public static void main(String[] args) {
        SpringApplication.run(SkinCareApplication.class, args);
    }
}
