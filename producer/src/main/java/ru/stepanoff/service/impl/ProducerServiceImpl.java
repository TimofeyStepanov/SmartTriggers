package ru.stepanoff.service.impl;

import io.micrometer.core.instrument.MeterRegistry;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Histogram;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import ru.stepanoff.component.KafkaWriter;
import ru.stepanoff.component.PostgresWriter;
import ru.stepanoff.model.DTO.ProducerEntityDTO;
import ru.stepanoff.model.DTO.UserDTO;
import ru.stepanoff.service.ProducerService;

import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
public class ProducerServiceImpl implements ProducerService {
    private final Flux<ProducerEntityDTO> processorEntityDTOFlux;

    private volatile boolean needToWriteInFile = false;
    private volatile boolean needToWritePostgres = false;
    private volatile boolean needToWriteKafka = false;

    private final Object fileMock = new Object();
    private final Object postgresMock = new Object();
    private final Object kafkaMock = new Object();

    private final AtomicLong numberOfReceivedMessages = new AtomicLong();

    private KafkaWriter kafkaWriter;
    private PostgresWriter postgresWriter;


    public ProducerServiceImpl(Flux<ProducerEntityDTO> processorEntityDTOFlux, MeterRegistry meterRegistry, CollectorRegistry collectorRegistry) {
        this.processorEntityDTOFlux = processorEntityDTOFlux;
        meterRegistry.gauge("numberOfReceivedMessages", numberOfReceivedMessages);
        
    }

    @PostConstruct
    public void startListening() {
        processorEntityDTOFlux.subscribe(this::processProcessorRuleUpdate);
    }

    private void processProcessorRuleUpdate(ProducerEntityDTO processorEntityDTO) {
        log.debug("Rule update");
        try {
            tryToProcessProducerEvent(processorEntityDTO);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    private void tryToProcessProducerEvent(@Valid ProducerEntityDTO producerEntityDTO) {
        log.debug("Get rule {}", producerEntityDTO);
        synchronized (kafkaMock) {
            saveKafkaParams(producerEntityDTO);
        }
        synchronized (postgresMock) {
            savePostgresParams(producerEntityDTO);
        }
        synchronized (fileMock) {
            saveFileParams(producerEntityDTO);
        }
    }

    private void saveKafkaParams(ProducerEntityDTO producerEntityDTO) {
        needToWriteKafka = false;

        String kafkaBootstrap = producerEntityDTO.getBootstrapServers();
        String kafkaTopic = producerEntityDTO.getTopic();
        Integer batchSize = producerEntityDTO.getBatchSize();
        Integer requestTimoutMs = producerEntityDTO.getRequestTimoutMs();
        Integer bufferMemory = producerEntityDTO.getBufferMemory();
        Long linearMs = producerEntityDTO.getLingerMs();
        String acks = producerEntityDTO.getAcks();

        if (kafkaTopic == null && kafkaBootstrap == null && batchSize == null
                && requestTimoutMs == null && bufferMemory == null && linearMs == null && acks == null) {
            log.debug("No kafka params");
            return;
        }
        if (!(kafkaTopic != null && kafkaBootstrap != null && batchSize != null
                && requestTimoutMs != null && bufferMemory != null && linearMs != null && acks != null)) {
            throw new IllegalArgumentException("Wrong kafka params");
        }

        if (kafkaWriter != null) {
            kafkaWriter.close();
        }
        kafkaWriter = KafkaWriter.builder()
                .bootstrapServers(kafkaBootstrap)
                .topic(kafkaTopic)
                .batchSize(batchSize)
                .requestTimoutMs(requestTimoutMs)
                .bufferMemory(bufferMemory)
                .lingerMs(linearMs)
                .acks(acks)
                .build();
        needToWriteKafka = true;
        log.debug("Save kafka params");
    }

    private void savePostgresParams(ProducerEntityDTO producerEntityDTO) {
        needToWritePostgres = false;

        String url = producerEntityDTO.getPostgresUrl();
        String user = producerEntityDTO.getNickname();
        String password = producerEntityDTO.getPassword();
        String tableName = producerEntityDTO.getTableName();

        if (url == null && user == null && password == null && tableName == null) {
            log.debug("No postgres params");
            return;
        }
        if (!(url != null && user != null && password != null && tableName != null)) {
            throw new IllegalArgumentException("Wrong postgres params");
        }

        if (postgresWriter != null) {
            postgresWriter.close();
        }
        postgresWriter = PostgresWriter.builder()
                .url(url)
                .userName(user)
                .password(password)
                .tableName(tableName)
                .driverClassName(tableName)
                .build();
        needToWritePostgres = true;
        log.debug("Save postgres params");
    }

    private void saveFileParams(ProducerEntityDTO producerEntityDTO) {
        log.debug("Write in file:" + producerEntityDTO.getWriteInFile());
        needToWriteInFile = producerEntityDTO.getWriteInFile();
    }

    @Override
    public void processUser(@Valid UserDTO userDTO) {
        log.debug("Get userDTO {}", userDTO);
        numberOfReceivedMessages.incrementAndGet();

        synchronized (kafkaMock) {
            writeInKafka(userDTO);
        }
        synchronized (postgresMock) {
            writeInPostgres(userDTO);
        }
        synchronized (fileMock) {
            writeInFile(userDTO);
        }
    }

    private void writeInKafka(UserDTO userDTO) {
        if (!needToWriteKafka) {
            return;
        }
        log.debug("Write in Kafka");
        kafkaWriter.write(userDTO.getUserId());
    }

    private void writeInPostgres(UserDTO userDTO) {
        if (!needToWritePostgres) {
            return;
        }
        log.debug("Write in Postgres");
        postgresWriter.write(userDTO);
    }

    private void writeInFile(UserDTO userDTO) {
        if (!needToWriteInFile) {
            return;
        }
        log.debug("Write in File");
        log.info(userDTO.toString());
    }
}
