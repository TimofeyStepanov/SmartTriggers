package ru.stepanoff.controller.impl;

import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import ru.stepanoff.controller.RouterController;
import ru.stepanoff.model.request.Call;
import ru.stepanoff.model.request.Geo;
import ru.stepanoff.model.request.Link;
import ru.stepanoff.service.ProcessorService;

@Slf4j
@Component
@RequiredArgsConstructor
public class RouterControllerImpl implements RouterController {
    private final ProcessorService processorService;

    @Override
    @Timed("geoControllerHandling")
    public Mono<ServerResponse> handleGeo(ServerRequest serverRequest) {
        Mono<Geo> geoMono = serverRequest.bodyToMono(Geo.class);
        return geoMono.flatMap(geo -> {
            processorService.processGeo(geo);
            return ServerResponse.ok().build();
        });
    }

    @Override
    @Timed("linkControllerHandling")
    public Mono<ServerResponse> handleLink(ServerRequest serverRequest) {
        Mono<Link> linkMono = serverRequest.bodyToMono(Link.class);
        return linkMono.flatMap(link -> {
            processorService.processLink(link);
            return ServerResponse.ok().build();
        });
    }

    @Override
    @Timed("callControllerHandling")
    public Mono<ServerResponse> handleCall(ServerRequest serverRequest) {
        Mono<Call> callMono = serverRequest.bodyToMono(Call.class);
        return callMono.flatMap(call -> {
           processorService.processCall(call);
           return ServerResponse.ok().build();
        });
    }
}
