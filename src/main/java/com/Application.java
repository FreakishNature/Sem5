package com;

import com.constants.Constants;
import com.database.ProjectRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
//@EnableJpaRepositories(basePackageClasses = ProjectRepository.class)
public class Application {
    public static Constants constants = new Constants();

    public static void main(String[] args) {
        SpringApplication.run(Application.class,args);
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**").allowedMethods("GET", "POST", "PUT", "DELETE","PATCH")
                        .allowedOrigins("*")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }
}
