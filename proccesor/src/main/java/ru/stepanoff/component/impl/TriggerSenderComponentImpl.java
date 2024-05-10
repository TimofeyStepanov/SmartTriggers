package ru.stepanoff.component.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.stepanoff.component.TriggerSenderComponent;
import ru.stepanoff.model.DTO.UserDTO;
import ru.stepanoff.repository.SetRepository;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class TriggerSenderComponentImpl implements TriggerSenderComponent {
    private final SetRepository<Long> triggerRepository;

    private final WebClient webClient;
    private long triggerIntervalInSeconds;

    public TriggerSenderComponentImpl(SetRepository<Long> triggerRepository, @Value("${application.producerUrl}") String serverUrl) {
        this.triggerRepository = triggerRepository;
        webClient = WebClient.builder().baseUrl(serverUrl).build();
    }

    @Override
    public void send(long userId) {
        if (triggerRepository.contains(userId)) {
            log.debug("Trigger for {} already exists", userId);
            return;
        }

        log.debug("Create trigger for {}", userId);
        webClient.post()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(Mono.just(new UserDTO(userId)), UserDTO.class)
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(e -> Mono.empty())
                .subscribe(log::debug);

        CompletableFuture.runAsync(() ->  triggerRepository.add(userId, triggerIntervalInSeconds));
    }

    @Override
    public void setTriggerInterval(long triggerIntervalInSeconds) {
        CompletableFuture.runAsync(triggerRepository::clear);
        this.triggerIntervalInSeconds = triggerIntervalInSeconds;
    }
}
