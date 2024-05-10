package ru.stepanoff.repository;

import ru.stepanoff.entity.CallEntity;
import ru.stepanoff.entity.GeoEntity;
import ru.stepanoff.entity.LinkEntity;

import java.util.Collection;
import java.util.Optional;

public interface StateRepository {
    // не нужно больше двух в радиусе
    Collection<GeoEntity> getCoordinates(long userId, long maxLivingTimeInMilliSeconds);

    // не нужно больше чем clickNumber
    Collection<LinkEntity> getLinks(long userId, long maxLivingTimeInMilliSeconds, int maxLinkNumber);

    // возвращает последний верный Call.
    Optional<CallEntity> getCall(long userId, long maxLivingTimeInMilliSeconds);

    void saveCall(CallEntity call);
    void saveGeos(Collection<GeoEntity> geoEntities);
    void saveLinks(Collection<LinkEntity> links);

    void clear(long maxLivingTimeInSeconds); // очищает просроченные данные
    void clear();
}
