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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.stepanoff.model.ConsumerDTO;
import ru.stepanoff.model.ConsumerEntityDTO;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ConnectionFactoryConfig {
    private final ModelMapper mapper;

    @Bean
    public PostgresqlConnection getConnectionFactory(@Value("${postgres.host}") String host,
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
    public Flux<ConsumerDTO> getConsumerDTOFlux(PostgresqlConnection postgresqlConnection,
                                                @Value("${postgres.channelName}") String channelName) {
        postgresqlConnection.createStatement("listen " + channelName)
                .execute()
                .flatMap(PostgresqlResult::getRowsUpdated)
                .subscribe();

        return postgresqlConnection
                .getNotifications()
                .mapNotNull(Notification::getParameter)
                .mapNotNull(this::getConsumerEntityFromJSON)
                .mapNotNull(consumerEntity -> mapper.map(consumerEntity, ConsumerDTO.class));
    }

    private ConsumerEntityDTO getConsumerEntityFromJSON(String consumerEntityJson) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);

        ConsumerEntityDTO consumer = null;
        try {
            consumer = objectMapper.readValue(consumerEntityJson, ConsumerEntityDTO.class);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
        }
        return consumer;
    }
}