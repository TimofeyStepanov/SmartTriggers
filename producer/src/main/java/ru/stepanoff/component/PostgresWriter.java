package ru.stepanoff.component;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import ru.stepanoff.model.DTO.UserDTO;

import java.io.Closeable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Builder
public class PostgresWriter implements Closeable {
    private final String url;
    private final String userName;
    private final String password;
    private final String tableName;
    private final String driverClassName;

    private HikariDataSource hikariDataSource;

    public synchronized void write(UserDTO userDTO) {
        hikariDataSource = Optional.ofNullable(hikariDataSource).orElseGet(this::getDataSource);
        try (Connection connection = hikariDataSource.getConnection()) {
            DSLContext dslContext = DSL.using(connection, SQLDialect.POSTGRES);
            String query = String.format("insert into %s values ('%s', %s)", tableName, UUID.randomUUID(), userDTO.getUserId());
            dslContext.execute(query);
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
    }

    private HikariDataSource getDataSource() {
        log.debug("Get data source");
        HikariConfig dataBaseConfig = new HikariConfig();
        dataBaseConfig.setJdbcUrl(url);
        dataBaseConfig.setUsername(userName);
        dataBaseConfig.setPassword(password);
        dataBaseConfig.setDriverClassName("org.postgresql.Driver");
        return new HikariDataSource(dataBaseConfig);
    }

    @Override
    public void close() {
        if (hikariDataSource == null) {
            return;
        }
        hikariDataSource.close();
        hikariDataSource = null;
    }
}
