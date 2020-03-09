package com.dailymission.api.springboot;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching // 캐시 활성화
public class SpringbootApplication {

    public static final String APPLICATION_LOCATIONS = "spring.config.location="
            + "classpath:application.yml,"
            + "classpath:aws.yml";

    public static void main(String[] args) {
        new SpringApplicationBuilder(SpringbootApplication.class)
                .properties(APPLICATION_LOCATIONS)
                .run(args);
    }
}