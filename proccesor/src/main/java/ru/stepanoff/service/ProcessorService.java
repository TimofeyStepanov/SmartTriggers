package ru.stepanoff.service;

import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import ru.stepanoff.model.request.Call;
import ru.stepanoff.model.request.Geo;
import ru.stepanoff.model.request.Link;

@Validated
public interface ProcessorService {
    void processGeo(@Valid Geo geo);
    void processLink(@Valid Link link);
    void processCall(@Valid Call call);
}
