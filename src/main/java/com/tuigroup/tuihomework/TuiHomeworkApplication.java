package com.tuigroup.tuihomework;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class TuiHomeworkApplication {

    public static void main(String[] args) {
        SpringApplication.run(TuiHomeworkApplication.class, args);
    }

    @Bean
    public OpenAPI customOpenAPI(@Value("${application-title}") String appTitle,
                                 @Value("${application-description}") String appDescription,
                                 @Value("${application-version}") String appVersion) {
        return new OpenAPI().info(new Info().title(appTitle).version(appVersion).description(appDescription));
    }

}
