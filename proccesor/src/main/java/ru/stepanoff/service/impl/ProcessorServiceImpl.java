package ru.stepanoff.service.impl;

import io.micrometer.core.annotation.Timed;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.validation.Valid;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import ru.stepanoff.component.EnrichmentComponent;
import ru.stepanoff.component.StateComponent;
import ru.stepanoff.component.TriggerSenderComponent;
import ru.stepanoff.entity.CallEntity;
import ru.stepanoff.entity.GeoEntity;
import ru.stepanoff.entity.LinkEntity;
import ru.stepanoff.model.DTO.ProcessorEntityDTO;
import ru.stepanoff.model.request.Call;
import ru.stepanoff.model.request.Geo;
import ru.stepanoff.model.request.Link;
import ru.stepanoff.service.ProcessorService;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Builder
class GeoRule {
    double latitudeMin;
    double latitudeMax;
    double longitudeMin;
    double longitudeMax;
    int radiusMeters;
    long geoIntervalInSeconds;
}

@Builder
class LinkRule {
    int clickNumber;
    Set<String> links;
}

@Builder
class CallRule {
    String phoneB;
}


@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessorServiceImpl implements ProcessorService {
    private enum Topic {
        GEO, CALL, SITE;
    }

    private final Flux<ProcessorEntityDTO> processorEntityDTOFlux;

    private final EnrichmentComponent enrichmentComponent;
    private final StateComponent stateComponent;
    private final TriggerSenderComponent triggerSenderComponent;
    private final ModelMapper mapper;

    private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
    private final ConcurrentHashMap<Topic, Predicate<Long>> topicAndItsPredicate = new ConcurrentHashMap<>();

    private volatile int clickNumber;
    private volatile long triggerIntervalInMilliSeconds;

    private final ConcurrentHashMap<Topic, Optional<Object>> topicAndItsRule = new ConcurrentHashMap<>();

    @PostConstruct
    public void startListening() {
        processorEntityDTOFlux.subscribe(this::tryToProcessProcessorRuleUpdate);
    }

    @PreDestroy
    private void clearDB() {
        stateComponent.clear();
    }

    private void tryToProcessProcessorRuleUpdate(ProcessorEntityDTO processorEntityDTO) {
        try {
            processProcessorEvent(processorEntityDTO);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    private void processProcessorEvent(@Valid ProcessorEntityDTO processorEntityDTO) {
        log.debug(processorEntityDTO.toString());

        triggerIntervalInMilliSeconds = processorEntityDTO.getTriggerIntervalInSeconds() * 1000;

        topicAndItsPredicate.clear();
        stateComponent.clear();

        triggerSenderComponent.setTriggerInterval(processorEntityDTO.getTriggerIntervalInSeconds());

        executorService.shutdown();
        executorService = Executors.newScheduledThreadPool(1);
        executorService.scheduleAtFixedRate(() -> stateComponent.clear(triggerIntervalInMilliSeconds), triggerIntervalInMilliSeconds, triggerIntervalInMilliSeconds, TimeUnit.MILLISECONDS);

        saveGeoRule(processorEntityDTO);
        saveCallRule(processorEntityDTO);
        saveSiteRule(processorEntityDTO);
    }

    private void saveGeoRule(ProcessorEntityDTO processorEntityDTO) {
        topicAndItsRule.put(Topic.GEO, Optional.empty());

        Double latitude = processorEntityDTO.getLatitude();
        Double longitude = processorEntityDTO.getLongitude();
        Integer radiusMeters = processorEntityDTO.getRadiusMeters();
        Long geoIntervalInSeconds = processorEntityDTO.getGeoIntervalInSeconds();

        if (latitude == null && longitude == null && radiusMeters == null && geoIntervalInSeconds == null) {
            log.debug("No geo rules");
            topicAndItsPredicate.put(Topic.GEO, userId -> true);
            return;
        }
        if (!(latitude != null && longitude != null && radiusMeters != null && geoIntervalInSeconds != null)) {
            throw new IllegalArgumentException("Wrong geo rule");
        }

        double radiusInKm = radiusMeters / 1000.0;
        double longitudeMin = longitude - radiusInKm / Math.abs(Math.cos(Math.toRadians(latitude)) * 111);
        double longitudeMax = longitude + radiusInKm / Math.abs(Math.cos(Math.toRadians(latitude)) * 111);
        double latitudeMin = latitude - radiusInKm / 111;
        double latitudeMax = latitude + radiusInKm / 111;

        GeoRule geoRule = GeoRule.builder()
                .geoIntervalInSeconds(geoIntervalInSeconds)
                .radiusMeters(radiusMeters)
                .latitudeMin(latitudeMin)
                .latitudeMax(latitudeMax)
                .longitudeMin(longitudeMin)
                .longitudeMax(longitudeMax)
                .build();
        topicAndItsRule.put(Topic.GEO, Optional.of(geoRule));

        topicAndItsPredicate.put(Topic.GEO, userId -> {
            PriorityBlockingQueue<GeoEntity> geoPriorityBlockingQueue = stateComponent.getCoordinates(userId, geoIntervalInSeconds);
            GeoEntity oldestGeoEntity = geoPriorityBlockingQueue.peek();
            long minTime = System.currentTimeMillis() - geoIntervalInSeconds * 1000;

            return oldestGeoEntity != null && minTime < oldestGeoEntity.getTime().getTime()
                    && geoPriorityBlockingQueue.size() >= 2;
        });
        log.debug("Save geo rules");
    }

    private void saveCallRule(ProcessorEntityDTO processorEntityDTO) {
        topicAndItsRule.put(Topic.CALL, Optional.empty());

        String phoneB = processorEntityDTO.getPhoneB();
        if (phoneB == null) {
            log.debug("No call rules");
            topicAndItsPredicate.put(Topic.CALL, userId -> true);
            return;
        }

        CallRule callRule = CallRule.builder()
                .phoneB(phoneB)
                .build();
        topicAndItsRule.put(Topic.CALL, Optional.of(callRule));

        topicAndItsPredicate.put(Topic.CALL, userId -> {
            Optional<CallEntity> callEntity = stateComponent.getCall(userId, triggerIntervalInMilliSeconds);
            return callEntity.isPresent() && timeIsValid(callEntity.get().getTime());
        });
        log.debug("Save call rules");
    }

    private void saveSiteRule(ProcessorEntityDTO processorEntityDTO) {
        topicAndItsRule.put(Topic.SITE, Optional.empty());

        Integer clickNumberFromDTO = processorEntityDTO.getClickNumber();
        if (clickNumberFromDTO == null) {
            throw new IllegalArgumentException("Wrong site rule");
        }
        if (clickNumberFromDTO == 0) {
            log.debug("No site rules");
            topicAndItsPredicate.put(Topic.SITE, userId -> true);
            return;
        }
        String links = processorEntityDTO.getLinks();
        if (links == null) {
            throw new IllegalArgumentException("Wrong site rule");
        }

        this.clickNumber = clickNumberFromDTO;

        Set<String> linkSet = Arrays.stream(links.split(",")).collect(Collectors.toSet());
        LinkRule linkRule = LinkRule.builder()
                .links(linkSet)
                .clickNumber(clickNumberFromDTO)
                .build();
        topicAndItsRule.put(Topic.SITE, Optional.of(linkRule));

        topicAndItsPredicate.put(Topic.SITE, userId -> {
            PriorityBlockingQueue<LinkEntity> linkPriorityBlockingQueue = stateComponent.getLinks(
                    userId,
                    triggerIntervalInMilliSeconds,
                    clickNumberFromDTO
            );
            LinkEntity oldestLinkEntity = linkPriorityBlockingQueue.peek();

            return oldestLinkEntity != null && timeIsValid(oldestLinkEntity.getTime())
                    && linkPriorityBlockingQueue.size() >= clickNumberFromDTO;
        });
        log.debug("Save site rules");
    }

    @Override
    @Timed("geoServiceProcessing")
    public void processGeo(@Valid Geo geo) {
        log.debug(geo.toString());
        if (!geoIsValid(geo)) {
            return;
        }

        enrichmentComponent.enrich(geo);

        GeoEntity geoEntity = mapper.map(geo, GeoEntity.class);
        geoEntity.setTime(new Timestamp(geo.getTime()));
        stateComponent.save(geoEntity);

        boolean canCreateTrigger = topicAndItsPredicate.values()
                .stream()
                .allMatch(predicate -> predicate.test(geo.getUserId()));
        if (canCreateTrigger) {
            triggerSenderComponent.send(geo.getUserId());
        }
    }

    private boolean geoIsValid(Geo geo) {
        Optional<Object> optionalRule = topicAndItsRule.get(Topic.GEO);
        if (!topicAndItsRule.containsKey(Topic.GEO) || optionalRule.isEmpty()) {
            return false;
        }

        GeoRule geoRule = (GeoRule) (optionalRule.get());

        double longitude = geo.getLongitude();
        double latitude = geo.getLatitude();
        Long time = geo.getTime();

        return timeIsValid(time) &&
                (geoRule.latitudeMin < latitude && latitude < geoRule.latitudeMax) &&
                (geoRule.longitudeMin < longitude && longitude < geoRule.longitudeMax);
    }

    @Override
    @Timed("linkServiceProcessing")
    public void processLink(@Valid Link link) {
        log.debug(link.toString());
        if (!linkIsValid(link)) {
            return;
        }

        enrichmentComponent.enrich(link);

        LinkEntity linkEntity = mapper.map(link, LinkEntity.class);
        linkEntity.setTime(new Timestamp(link.getTime()));
        stateComponent.save(linkEntity, clickNumber);

        boolean canCreateTrigger = topicAndItsPredicate.values()
                .stream()
                .allMatch(predicate -> predicate.test(link.getUserId()));
        if (canCreateTrigger) {
            triggerSenderComponent.send(link.getUserId());
        }
    }

    private boolean linkIsValid(Link link) {
        Optional<Object> optionalRule = topicAndItsRule.get(Topic.SITE);
        if (!topicAndItsRule.containsKey(Topic.SITE) || optionalRule.isEmpty()) {
            return false;
        }

        LinkRule linkRule = (LinkRule) (optionalRule.get());
        String url = link.getUrl();
        Long time = link.getTime();

        return linkRule.links.contains(url) && timeIsValid(time);
    }

    @Override
    @Timed("callServiceProcessing")
    public void processCall(@Valid Call call) {
        log.debug(call.toString());
        if (!callIsValid(call)) {
            return;
        }

        enrichmentComponent.enrich(call);

        CallEntity callEntity = mapper.map(call, CallEntity.class);
        callEntity.setTime(new Timestamp(call.getTime()));
        stateComponent.save(callEntity);

        boolean canCreateTrigger = topicAndItsPredicate.values()
                .stream()
                .allMatch(predicate -> predicate.test(call.getUserId()));
        if (canCreateTrigger) {
            triggerSenderComponent.send(call.getUserId());
        }
    }

    private boolean callIsValid(Call call) {
        Optional<Object> optionalRule = topicAndItsRule.get(Topic.CALL);
        if (!topicAndItsRule.containsKey(Topic.CALL) || optionalRule.isEmpty()) {
            return false;
        }

        CallRule callRule = (CallRule) (optionalRule.get());
        String phoneB = call.getPhoneB();
        Long time = call.getTime();

        return phoneB.equals(callRule.phoneB) && timeIsValid(time);
    }

    private boolean timeIsValid(Timestamp time) {
        return !Objects.isNull(time) && timeIsValid(time.getTime());
    }

    private boolean timeIsValid(long time) {
        long minTime = System.currentTimeMillis() - triggerIntervalInMilliSeconds;
        return minTime < time && time < System.currentTimeMillis();
    }
}
