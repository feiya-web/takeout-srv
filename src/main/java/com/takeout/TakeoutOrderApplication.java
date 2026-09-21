package com.takeout;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.takeout.mapper")
public class TakeoutOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(TakeoutOrderApplication.class, args);
    }
}
