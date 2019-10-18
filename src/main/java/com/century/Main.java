package com.century;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;

@Slf4j
@SpringBootApplication
@PropertySource({"classpath:common.properties", "classpath:extra_charge/extra_charge.properties"})
@ComponentScan(basePackages={"com.century.web", "com.century.report"})
public class Main extends SpringBootServletInitializer {
    public static void main(String[] args) {
        try {
            SpringApplication.run(Main.class, args);
        } catch (Exception e) {
            log.error("Error in main:", e);
        }
    }
}
