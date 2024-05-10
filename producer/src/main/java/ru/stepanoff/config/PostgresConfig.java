package ru.stepanoff.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import io.r2dbc.postgresql.api.Notification;
import io.r2dbc.postgresql.api.PostgresqlConnection;
import io.r2dbc.postgresql.api.PostgresqlResult;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.stepanoff.model.DTO.ProducerEntityDTO;

@Slf4j
@Configuration
public class PostgresConfig {
    @Bean
    public PostgresqlConnection getPostgresqlConnection(@Value("${postgres.host}") String host,
                                                        @Value("${postgres.port}") int port,
                                                        @Value("${postgres.database}") String database,
                                                        @Value("${postgres.user}") String user,
                                                        @Value("${postgres.password}") String password) {
        ConnectionFactoryOptions connectionFactoryOptions = ConnectionFactoryOptions.builder()
                .option(ConnectionFactoryOptions.DRIVER, "postgres")
                .option(ConnectionFactoryOptions.HOST, host)
                .option(ConnectionFactoryOptions.PORT, port)
                .option(ConnectionFactoryOptions.DATABASE, database)
                .option(ConnectionFactoryOptions.USER, user)
                .option(ConnectionFactoryOptions.PASSWORD, password)
                .build();

        ConnectionFactory connectionFactory = ConnectionFactories.get(connectionFactoryOptions);
        return Mono.from(connectionFactory.create()).cast(PostgresqlConnection.class).block();
    }

    @Bean
    public Flux<ProducerEntityDTO> getProducerEntityDTOFlux(PostgresqlConnection postgresqlConnection,
                                                            @Value("${postgres.channelName}") String channelName) {
        postgresqlConnection.createStatement("listen " + channelName)
                .execute()
                .flatMap(PostgresqlResult::getRowsUpdated)
                .subscribe();

        return postgresqlConnection
                .getNotifications()
                .mapNotNull(Notification::getParameter)
                .mapNotNull(this::getDTOFromJSON);
    }

    private ProducerEntityDTO getDTOFromJSON(String processorEntityJson) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);

        ProducerEntityDTO producerEntityDTO = null;
        try {
            producerEntityDTO = objectMapper.readValue(processorEntityJson, ProducerEntityDTO.class);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
        }
        return producerEntityDTO;
    }
}
