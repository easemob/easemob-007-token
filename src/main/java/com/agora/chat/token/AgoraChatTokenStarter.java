package com.agora.chat.token;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.agora")
public class AgoraChatTokenStarter {
    public static void main(String[] args) {
        SpringApplication.run(AgoraChatTokenStarter.class, args);
    }
}
