package ru.stepanoff.component.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.stepanoff.component.StateComponent;
import ru.stepanoff.entity.CallEntity;
import ru.stepanoff.entity.GeoEntity;
import ru.stepanoff.entity.LinkEntity;
import ru.stepanoff.repository.StateRepository;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Optional;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.function.BiFunction;


class PriorityBlockingQueueWithEviction<E> extends PriorityBlockingQueue<E> {
    private final int maxSize;

    public PriorityBlockingQueueWithEviction(int maxSize, Comparator<? super E> comparator) {
        super(maxSize, comparator);
        this.maxSize = maxSize;
    }

    public PriorityBlockingQueueWithEviction(int maxSize, Comparator<? super E> comparator, E e) {
        super(maxSize, comparator);
        super.add(e);
        this.maxSize = maxSize;
    }

    public synchronized void addingWithEviction(E e) {
        super.add(e);
        if (super.size() > maxSize) {
            super.remove();
        }
    }

    public synchronized PriorityBlockingQueueWithEviction<E> addingWithEviction(Collection<? extends E> c) {
        c.forEach(this::addingWithEviction);
        return this;
    }
}

@Component
public class StateComponentImpl implements StateComponent {
    private final StateRepository stateRepository;

    private final Comparator<GeoEntity> geosComparator = Comparator.comparing(GeoEntity::getTime);
    private final Comparator<LinkEntity> linksComparator = Comparator.comparing(LinkEntity::getTime);

    // cache for geos
    private final Cache<Long, PriorityBlockingQueueWithEviction<GeoEntity>> cacheForAddingCoordinates;
    private final Cache<Long, PriorityBlockingQueueWithEviction<GeoEntity>> cacheForGettingCoordinates;

    // cache for links
    private final Cache<Long, PriorityBlockingQueueWithEviction<LinkEntity>> cacheForAddingLinks;
    private final Cache<Long, PriorityBlockingQueueWithEviction<LinkEntity>> cacheForGettingLinks;

    // cache for calls
    private final Cache<Long, Optional<CallEntity>> cacheForAddingCalls;
    private final Cache<Long, Optional<CallEntity>> cacheForGettingCalls;

    public StateComponentImpl(StateRepository stateRepository, @Value("${application.cacheSize}") int cacheSize) {
        this.stateRepository = stateRepository;

        // cache for geos
        cacheForAddingCoordinates = Caffeine.newBuilder()
                .maximumSize(cacheSize)
                .evictionListener((Long userId, PriorityBlockingQueueWithEviction<GeoEntity> geos, RemovalCause cause) -> {
                    stateRepository.saveGeos(geos);
                })
                .build();

        cacheForGettingCoordinates = Caffeine.newBuilder()
                .maximumSize(cacheSize)
                .build();

        // cache for links
        cacheForAddingLinks = Caffeine.newBuilder()
                .maximumSize(cacheSize)
                .evictionListener((Long userId,
                                   PriorityBlockingQueueWithEviction<LinkEntity> links,
                                   RemovalCause cause) -> {
                    stateRepository.saveLinks(links);
                })
                .build();

        cacheForGettingLinks = Caffeine.newBuilder()
                .maximumSize(cacheSize)
                .build();

        // cache for calls
        cacheForAddingCalls = Caffeine.newBuilder()
                .maximumSize(cacheSize)
                .evictionListener((Long userId, Optional<CallEntity> optionalCallEntity, RemovalCause cause) -> {
                    optionalCallEntity.ifPresent(stateRepository::saveCall);
                })
                .build();

        cacheForGettingCalls = Caffeine.newBuilder()
                .maximumSize(cacheSize)
                .build();
    }


    @Override
    public PriorityBlockingQueue<GeoEntity> getCoordinates(long userId, long maxLivingTimeInMilliSeconds) {
        return cacheForGettingCoordinates.get(userId, id -> {
            var priorityBlockingQueueWithEviction = new PriorityBlockingQueueWithEviction<>(2, geosComparator);

            Collection<GeoEntity> geosFromDB = stateRepository.getCoordinates(id, maxLivingTimeInMilliSeconds);
            priorityBlockingQueueWithEviction.addAll(geosFromDB);

            var geosFromCacheForAdding = cacheForAddingCoordinates.getIfPresent(id);
            if (geosFromCacheForAdding != null) {
                priorityBlockingQueueWithEviction.addAll(geosFromCacheForAdding);
            }
            return priorityBlockingQueueWithEviction;
        });
    }

    @Override
    public PriorityBlockingQueue<LinkEntity> getLinks(
            long userId,
            long maxLivingTimeInMilliSeconds,
            int maxLinkNumber) {

        return cacheForGettingLinks.get(userId, id -> {
            var priorityBlockingQueueWithEviction = new PriorityBlockingQueueWithEviction<>(
                    maxLinkNumber,
                    linksComparator
            );

            Collection<LinkEntity> linksFromDB = stateRepository.getLinks(
                    id,
                    maxLivingTimeInMilliSeconds,
                    maxLinkNumber
            );
            priorityBlockingQueueWithEviction.addAll(linksFromDB);

            var linksFromCacheForAdding = cacheForAddingLinks.getIfPresent(id);
            if (linksFromCacheForAdding != null) {
                priorityBlockingQueueWithEviction.addAll(linksFromCacheForAdding);
            }
            return priorityBlockingQueueWithEviction;
        });
    }

    @Override
    public Optional<CallEntity> getCall(long userId, long maxLivingTimeInMilliSeconds) {
        return cacheForGettingCalls.get(userId, id -> {
            Optional<CallEntity> callFromCacheForAdding = cacheForAddingCalls.getIfPresent(id);
            Optional<CallEntity> callFromDB = stateRepository.getCall(id, maxLivingTimeInMilliSeconds);

            if (callFromCacheForAdding == null || callFromCacheForAdding.isEmpty()) {
                return callFromDB;
            }
            if (callFromDB.isEmpty()) {
                return callFromCacheForAdding;
            }

            Timestamp timeOfCallFromCache = callFromCacheForAdding.get().getTime();
            Timestamp timeOfCallFromDB = callFromDB.get().getTime();
            return timeOfCallFromCache.after(timeOfCallFromDB) ? callFromCacheForAdding : callFromDB;
        });
    }

    @Override
    public void save(CallEntity call) {
        var callCacheMapForRequest = cacheForGettingCalls.asMap();
        var callCacheMapForAdding = cacheForAddingCalls.asMap();

        BiFunction<Optional<CallEntity>, Optional<CallEntity>, Optional<CallEntity>> remappingFunction = (optionalPrevCall, optionalNewCall) -> {
            if (optionalPrevCall.isEmpty()) {
                return optionalNewCall;
            }

            CallEntity prevCall = optionalPrevCall.get();
            CallEntity newCall = optionalNewCall.get(); // не может быть empty. Проверяю в service

            Timestamp prevTime = prevCall.getTime();
            Timestamp newTime = newCall.getTime();

            return prevTime.compareTo(newTime) < 0 ? optionalNewCall : optionalPrevCall;
        };

        callCacheMapForRequest.merge(call.getUserId(), Optional.of(call), remappingFunction);
        callCacheMapForAdding.merge(call.getUserId(), Optional.of(call), remappingFunction);
    }

    @Override
    public void save(GeoEntity geoEntity) {
        long userId = geoEntity.getUserId();

        var geoCacheMapForRequest = cacheForGettingCoordinates.asMap();
        geoCacheMapForRequest.computeIfPresent(userId, (id, geos) -> geos.addingWithEviction(Collections.singletonList(geoEntity)));

        var geoCacheMapForAdding = cacheForAddingCoordinates.asMap();
        geoCacheMapForAdding.merge(
                userId,
                new PriorityBlockingQueueWithEviction<>(2, geosComparator, geoEntity),
                PriorityBlockingQueueWithEviction::addingWithEviction
        );
    }

    @Override
    public void save(LinkEntity linkEntity, int maxLinkNumber) {
        long userId = linkEntity.getUserId();

        var linkCacheMapForRequest = cacheForGettingLinks.asMap();
        linkCacheMapForRequest.computeIfPresent(
                userId,
                (id, links) -> links.addingWithEviction(Collections.singletonList(linkEntity))
        );

        var linkCacheMapForAdding = cacheForAddingLinks.asMap();
        linkCacheMapForAdding.merge(
                userId,
                new PriorityBlockingQueueWithEviction<>(maxLinkNumber, linksComparator, linkEntity),
                PriorityBlockingQueueWithEviction::addingWithEviction
        );
    }

    @Override
    public void clear(long maxLivingTimeInMilliSeconds) {
        stateRepository.clear(maxLivingTimeInMilliSeconds);
    }

    @Override
    public void clear() {
        cacheForAddingCoordinates.cleanUp();
        cacheForGettingCoordinates.cleanUp();

        cacheForAddingLinks.cleanUp();
        cacheForGettingLinks.cleanUp();

        cacheForGettingCalls.cleanUp();
        cacheForAddingCalls.cleanUp();

        stateRepository.clear();
    }
}
