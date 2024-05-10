package ru.stepanoff.component.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.stepanoff.component.EnrichmentComponent;
import ru.stepanoff.exception.WrongKeyException;
import ru.stepanoff.model.request.Call;
import ru.stepanoff.model.request.Geo;
import ru.stepanoff.model.request.Link;
import ru.stepanoff.repository.KeyValueRepository;

@Slf4j
@Component
public class EnrichmentComponentImpl implements EnrichmentComponent {
    private final Cache<Long, String> cache;
    private final KeyValueRepository<Long, String> keyValueRepository;

    public EnrichmentComponentImpl(KeyValueRepository<Long, String> keyValueRepository, @Value("${application.cacheSize}") int cacheSize) {
        this.keyValueRepository = keyValueRepository;
        this.cache = Caffeine.newBuilder().maximumSize(cacheSize).build();
    }

    @Override
    public void enrich(Geo geo) {
        try {
            tryToEnrich(geo);
        } catch (WrongKeyException e) {
            log.error(e.getMessage());
        }
    }

    private void tryToEnrich(Geo geo) throws WrongKeyException {
        String enrichedPhone = getEnrichedPhone(geo.getUserId(), geo.getPhone());
        geo.setPhone(enrichedPhone);
    }

    @Override
    public void enrich(Link link) {
        try {
            tryToEnrich(link);
        } catch (WrongKeyException e) {
            log.error(e.getMessage());
        }
    }

    private void tryToEnrich(Link link) throws WrongKeyException {
        String enrichedPhone = getEnrichedPhone(link.getUserId(), link.getPhone());
        link.setPhone(enrichedPhone);
    }

    @Override
    public void enrich(Call call) {
        try {
            tryToEnrich(call);
        } catch (WrongKeyException e) {
            log.error(e.getMessage());
        }
    }

    private void tryToEnrich(Call call) throws WrongKeyException {
        String enrichedPhone = getEnrichedPhone(call.getUserId(), call.getPhoneA());
        call.setPhoneA(enrichedPhone);
    }

    private String getEnrichedPhone(long userId, String phoneToEnrich) throws WrongKeyException {
        if (!phoneToEnrich.isBlank()) {
            if (cache.getIfPresent(userId) == null) {
                keyValueRepository.save(userId, phoneToEnrich);
                cache.put(userId, phoneToEnrich);
            }
            return phoneToEnrich;
        }

        String enrichedPhone = cache.getIfPresent(userId);
        if (enrichedPhone == null) {
            enrichedPhone = keyValueRepository.get(userId);
            cache.put(userId, enrichedPhone);
        }
        return enrichedPhone;
    }
}
