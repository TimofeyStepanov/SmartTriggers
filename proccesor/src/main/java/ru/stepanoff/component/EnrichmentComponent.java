package ru.stepanoff.component;

import ru.stepanoff.model.request.Call;
import ru.stepanoff.model.request.Geo;
import ru.stepanoff.model.request.Link;

public interface EnrichmentComponent {
    void enrich(Geo geo);
    void enrich(Link link);
    void enrich(Call call);
}
