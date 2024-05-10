package ru.stepanoff.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import ru.stepanoff.controller.RouterController;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
@RequiredArgsConstructor
public class RouterConfig {
    private final RouterController routerHandler;

    @Bean
    public RouterFunction<ServerResponse> routerFunction() {
        return route()
                .POST("/user", accept(APPLICATION_JSON), routerHandler::handleUser)
                .build();
    }
}
