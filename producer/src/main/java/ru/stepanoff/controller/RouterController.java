package ru.stepanoff.controller;

import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

public interface RouterController {
    Mono<ServerResponse> handleUser(ServerRequest serverRequest);
}
