package ru.stepanoff.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.stepanoff.service.ConsumerService;

@Configuration
@RequiredArgsConstructor
public class ConsumerCommandLineRunner {
    private final ConsumerService consumerService;

    @Bean
    public CommandLineRunner createKafkaGenerator() {
        return args -> consumerService.start();
    }
}
