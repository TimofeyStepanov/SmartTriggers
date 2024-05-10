package ru.stepanoff.component;

import ru.stepanoff.entity.CallEntity;
import ru.stepanoff.entity.GeoEntity;
import ru.stepanoff.entity.LinkEntity;

import java.util.Optional;
import java.util.concurrent.PriorityBlockingQueue;

public interface StateComponent {
    // не нужно больше двух в радиусе
    PriorityBlockingQueue<GeoEntity> getCoordinates(long userId, long maxLivingTimeInMilliSeconds);

    // не нужно больше чем clickNumber
    PriorityBlockingQueue<LinkEntity> getLinks(long userId, long maxLivingTimeInMilliSeconds, int maxLinkNumber);

    // возвращает последний верный Call.
    Optional<CallEntity> getCall(long userId, long maxLivingTimeInMilliSeconds);

    void save(CallEntity call); // также обновляет cache (для запросов и для выгрузки)
    void save(GeoEntity geoEntity); // также обновляет cache
    void save(LinkEntity link, int maxLinkNumber); // также обновляет cache

    void clear(long maxLivingTimeInMilliSeconds); // очищает просроченные данные (кеш не чистить)
    void clear();
}
