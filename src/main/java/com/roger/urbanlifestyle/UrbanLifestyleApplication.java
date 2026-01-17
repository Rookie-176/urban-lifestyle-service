package com.roger.urbanlifestyle;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@EnableAspectJAutoProxy(exposeProxy = true)
@MapperScan("com.roger.urbanlifestyle.mapper")
@SpringBootApplication
public class UrbanLifestyleApplication {

    public static void main(String[] args) {
        SpringApplication.run(UrbanLifestyleApplication.class, args);
    }

}
