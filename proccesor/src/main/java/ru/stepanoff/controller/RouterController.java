package ru.stepanoff.controller;

import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

public interface RouterController {
    Mono<ServerResponse> handleGeo(ServerRequest serverRequest);
    Mono<ServerResponse> handleLink(ServerRequest serverRequest);
    Mono<ServerResponse> handleCall(ServerRequest serverRequest);
}
