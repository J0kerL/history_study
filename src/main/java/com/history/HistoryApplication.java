package com.history;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Diamond
 */
@SpringBootApplication
@MapperScan("com.history.mapper")
public class HistoryApplication {

    public static void main(String[] args) {
        SpringApplication.run(HistoryApplication.class, args);
    }

}
