package ru.stepanoff.controller.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import ru.stepanoff.controller.RouterController;
import ru.stepanoff.model.DTO.UserDTO;
import ru.stepanoff.service.ProducerService;

@Slf4j
@Component
@RequiredArgsConstructor
public class RouterControllerImpl implements RouterController {
    private final ProducerService producerService;

    @Override
    public Mono<ServerResponse> handleUser(ServerRequest serverRequest) {
        Mono<UserDTO> userMono = serverRequest.bodyToMono(UserDTO.class);
        return userMono.flatMap(userDTO -> {
            producerService.processUser(userDTO);
            return ServerResponse.ok().build();
        });
    }
}
