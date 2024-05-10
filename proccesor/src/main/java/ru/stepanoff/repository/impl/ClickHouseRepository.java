package ru.stepanoff.repository.impl;

import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.stepanoff.entity.CallEntity;
import ru.stepanoff.entity.GeoEntity;
import ru.stepanoff.entity.LinkEntity;
import ru.stepanoff.repository.StateRepository;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClickHouseRepository implements StateRepository {
    private final HikariDataSource hikariDataSource;

    @Value("${clickHouse.geoTableName}")
    private String geoTableName;

    @Value("${clickHouse.linkTableName}")
    private String linkTableName;

    @Value("${clickHouse.callTableName}")
    private String callTableName;

    @Override
    public Collection<GeoEntity> getCoordinates(long userId, long maxLivingTimeInMilliSeconds) {
        Collection<GeoEntity> geoEntities = new ArrayList<>();
        try (Connection connection = hikariDataSource.getConnection()) {
            DSLContext dsl = DSL.using(connection);

            long minCreationTime = System.currentTimeMillis() - maxLivingTimeInMilliSeconds;
            String query = String.format("select * from `default`.%s g " +
                    "where  g.user_id = %s and g.`time` >= %s " +
                    "order by g.`time`desc " +
                    "limit 2", geoTableName, userId, minCreationTime);

            geoEntities = dsl.fetch(query).into(GeoEntity.class).stream().toList();
            log.debug("Get from db geo entities {}", geoEntities);
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        return geoEntities;
    }

    @Override
    public Collection<LinkEntity> getLinks(long userId, long maxLivingTimeInMilliSeconds, int maxLinkNumber) {
        Collection<LinkEntity> linkEntities = new ArrayList<>();
        try (Connection connection = hikariDataSource.getConnection()) {
            DSLContext dsl = DSL.using(connection);

            long minCreationTime = System.currentTimeMillis() - maxLivingTimeInMilliSeconds;
            String query = String.format("select * from `default`.%s l " +
                    "where  l.user_id = %s and l.`time` >= %s " +
                    "order by l.`time`desc " +
                    "limit %s", linkTableName, userId, minCreationTime, maxLinkNumber);

            linkEntities = dsl.fetch(query).into(LinkEntity.class).stream().toList();
            log.debug("Get from db link entities {}", linkEntities);
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        return linkEntities;
    }

    @Override
    public Optional<CallEntity> getCall(long userId, long maxLivingTimeInMilliSeconds) {
        Optional<CallEntity> callEntityOptional = Optional.empty();
        try (Connection connection = hikariDataSource.getConnection()) {
            DSLContext dsl = DSL.using(connection);

            long minCreationTime = System.currentTimeMillis() - maxLivingTimeInMilliSeconds;
            String query = String.format("select * from `default`.%s c " +
                    "where  c.user_id = %s and c.`time` >= %s " +
                    "order by c.`time`desc " +
                    "limit 1", callTableName, userId, minCreationTime);

            List<CallEntity> callEntityList = dsl.fetch(query).into(CallEntity.class).stream().toList();
            log.debug("Get from db call entity {}", callEntityList);
            if (!callEntityList.isEmpty()) {
                callEntityOptional = Optional.of(callEntityList.get(0));
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        return callEntityOptional;
    }

    @Override
    public void saveCall(CallEntity callEntity) {
        log.debug("save call {}", callEntity);

        try (Connection connection = hikariDataSource.getConnection()) {
            DSLContext dsl = DSL.using(connection);

            String query = String.format("insert into %s values ('%s', %s, '%s', '%s', %s)",
                    callTableName,
                    callEntity.getId(), callEntity.getUserId(), callEntity.getPhoneA(), callEntity.getPhoneB(), callEntity.getTime().getTime());
            dsl.execute(query);
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
    }

    @Override
    public void saveGeos(Collection<GeoEntity> geoEntities) {
        log.debug("save geos {}", geoEntities);

        try (Connection connection = hikariDataSource.getConnection()) {
            DSLContext dsl = DSL.using(connection);

            StringBuilder stringBuilder = new StringBuilder();
            geoEntities.forEach(geoEntity -> {
                if (!stringBuilder.isEmpty()) {
                    stringBuilder.append(',');
                }
                stringBuilder.append(
                        String.format("('%s', %s, '%s', %s, %s, %s)",
                                geoEntity.getId(), geoEntity.getUserId(),
                                geoEntity.getPhone(), geoEntity.getLatitude(),
                                geoEntity.getLongitude(), geoEntity.getTime().getTime())
                );
            });
            String query = String.format("insert into %s values %s", geoTableName, stringBuilder);
            dsl.execute(query);
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
    }

    @Override
    public void saveLinks(Collection<LinkEntity> linkEntities) {
        log.debug("save links {}", linkEntities);

        try (Connection connection = hikariDataSource.getConnection()) {
            DSLContext dsl = DSL.using(connection);

            StringBuilder stringBuilder = new StringBuilder();
            linkEntities.forEach(geoEntity -> {
                if (!stringBuilder.isEmpty()) {
                    stringBuilder.append(',');
                }
                stringBuilder.append(
                        String.format("('%s', %s, '%s', '%s', %s)",
                                geoEntity.getId(), geoEntity.getUserId(),
                                geoEntity.getPhone(), geoEntity.getUrl(),
                                geoEntity.getTime().getTime())
                );
            });
            String query = String.format("insert into %s values %s", linkTableName, stringBuilder);
            dsl.execute(query);
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
    }

    @Override
    public void clear(long maxLivingTimeInSeconds) {
        log.debug("clear overdue data");

        String queryTemplate = "delete from `default`.%s where `time` < %s";
        try (Connection connection = hikariDataSource.getConnection()) {
            DSLContext dsl = DSL.using(connection);

            long minCreationTime = System.currentTimeMillis() - maxLivingTimeInSeconds;

            String query = String.format(queryTemplate, callTableName, minCreationTime);
            dsl.execute(query);

            query = String.format(queryTemplate, linkTableName, minCreationTime);
            dsl.execute(query);

            query = String.format(queryTemplate, geoTableName, minCreationTime);
            dsl.execute(query);
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
    }

    @Override
    public void clear() {
        log.debug("clear db");

        String queryTemplate = "TRUNCATE TABLE `default`.%s";
        try (Connection connection = hikariDataSource.getConnection()) {
            DSLContext dsl = DSL.using(connection);

            String query = String.format(queryTemplate, callTableName);
            dsl.execute(query);

            query = String.format(queryTemplate, linkTableName);
            dsl.execute(query);

            query = String.format(queryTemplate, geoTableName);
            dsl.execute(query);
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
    }
}
