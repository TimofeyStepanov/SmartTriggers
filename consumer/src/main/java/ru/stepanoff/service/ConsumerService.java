package ru.stepanoff.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.micrometer.core.instrument.MeterRegistry;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Histogram;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.stepanoff.model.CallDTO;
import ru.stepanoff.model.ConsumerDTO;
import ru.stepanoff.model.GeoDTO;
import ru.stepanoff.model.LinkDTO;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

@Slf4j
@Service
@Validated
public class ConsumerService {
    private static final String GROUP_ID_CONFIG = "tc-" + UUID.randomUUID();

    private final CopyOnWriteArrayList<KafkaConsumer<String, String>> kafkaConsumers = new CopyOnWriteArrayList<>();
    private ExecutorService executorForConsumers = Executors.newFixedThreadPool(1);
    private volatile boolean needToConsume = true;

    private final Map<String, String> topicValueFromDBAndItsName = new HashMap<>();
    private final Map<String, Class<?>> topicValueFromDBAndItsClass = new HashMap<>();

    private final AtomicInteger numberOfWorkingConsumerThreads = new AtomicInteger();
    private final Lock lock = new ReentrantLock();
    private final Condition needToWaitStopOfAllWorkingConsumerThreads = lock.newCondition();

    private boolean alreadyStarted = false;

    private final AtomicLong numberOfSendMessages = new AtomicLong();
    private final Histogram kafkaMessageHistogram;

    private final WebClient webClient;
    private final Flux<ConsumerDTO> consumerDTOFlux;

    public ConsumerService(Flux<ConsumerDTO> consumerDTOFlux,
                           MeterRegistry meterRegistry,
                           CollectorRegistry collectorRegistry,
                           @Value("${application.serverUrl}") String processorUrl) {

        this.consumerDTOFlux = consumerDTOFlux;
        this.webClient = WebClient.builder().baseUrl(processorUrl).build();

        meterRegistry.gauge("numberOfSendMessages", numberOfSendMessages);

        // sum by (le) (rate(kafkaMessageHistogram_Millis_bucket[1m]))
        kafkaMessageHistogram = Histogram.build()
                .name("kafkaMessageHistogram")
                .help("Time of message to be read.")
                .buckets(1, 2.5, 5, 10, 25, 50, 100, 250, 500, 750, 1000)
                .unit("Millis")
                .register(collectorRegistry);
    }

    public void start() {
        if (alreadyStarted) {
            return;
        }
        alreadyStarted = true;
        consumerDTOFlux.subscribe(this::processConsumerDTOFromDB);
    }

    private void processConsumerDTOFromDB(@Valid ConsumerDTO consumerDTO) {
        log.debug("Get from db:" + consumerDTO);
        try {
            stopConsumers();
            updateKafkaParameters(consumerDTO);
            startConsumers(consumerDTO.getConsumerNumber());
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    private void stopConsumers() throws InterruptedException {
        log.debug("Stop consumers");

        lock.lock();
        needToConsume = false;
        while (numberOfWorkingConsumerThreads.get() != 0) {
            needToWaitStopOfAllWorkingConsumerThreads.await();
        }
        lock.unlock();

        executorForConsumers.shutdown();
        needToConsume = true;
    }

    private void startConsumers(int consumerNumber) {
        log.debug("Start consumers");

        executorForConsumers = Executors.newFixedThreadPool(consumerNumber);
        for (int i = 0; i < consumerNumber; i++) {
            int finalI = i;
            executorForConsumers.submit(() -> startThread(finalI));
        }
        numberOfWorkingConsumerThreads.set(consumerNumber);
    }

    private void startThread(int threadIndex) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        while (needToConsume) {
            KafkaConsumer<String, String> kafkaConsumer = kafkaConsumers.get(threadIndex);
            ConsumerRecords<String, String> consumerRecords = kafkaConsumer.poll(Duration.ofMillis(100));
            for (ConsumerRecord<String, String> consumerRecord : consumerRecords) {
                kafkaMessageHistogram.observe(Math.max(0.0, (double)System.currentTimeMillis() - consumerRecord.timestamp()));
                log.debug(String.valueOf(Math.max(0.0, System.currentTimeMillis() - consumerRecord.timestamp())));

                String topic = consumerRecord.topic();
                String value = consumerRecord.value();
                log.debug("Message from Kafka topic {} : {}. Sent to {}", topic, value, topicValueFromDBAndItsName.get(topic));

                String urlToSend = topicValueFromDBAndItsName.get(topic);
                Class<?> dtoClass = topicValueFromDBAndItsClass.get(topic);

                try {
                    webClient.post()
                            .uri(urlToSend)
                            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                            .body(Mono.just(objectMapper.readValue(value, dtoClass)), dtoClass)
                            .retrieve()
                            .bodyToMono(String.class)
                            .onErrorResume(e -> Mono.empty())
                            .subscribe(log::debug);

                    numberOfSendMessages.incrementAndGet();
                } catch (JsonProcessingException e) {
                    log.error(e.getMessage());
                }
            }
        }
        kafkaConsumers.get(threadIndex).close();
        log.debug("Stop consume");

        lock.lock();
        numberOfWorkingConsumerThreads.decrementAndGet();
        if (numberOfWorkingConsumerThreads.get() == 0) {
            needToWaitStopOfAllWorkingConsumerThreads.signal();
        }
        lock.unlock();
    }

    private void updateKafkaParameters(ConsumerDTO newConsumerDTO) {
        reconnectedKafkaConsumers(newConsumerDTO);
        reconnectKafkaConsumersToTopics(getKafkaTopicsFromConsumerDTO(newConsumerDTO));
        updateTopicValueFromDBAndItsName(newConsumerDTO);
    }

    private void reconnectedKafkaConsumers(ConsumerDTO consumerDTO) {
        kafkaConsumers.clear();

        int consumerNumber = consumerDTO.getConsumerNumber();
        String newBootstrapServers = consumerDTO.getBootstrapServers();
        String kafkaAutoOffsetReset = consumerDTO.getKafkaAutoOffsetReset();
        Integer requestTimoutMs = consumerDTO.getRequestTimoutMs();

        for (int i = 0; i < consumerNumber; i++) {
            KafkaConsumer<String, String> kafkaConsumer = new KafkaConsumer<>(
                    Map.of(
                            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, newBootstrapServers,
                            ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID_CONFIG,
                            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, kafkaAutoOffsetReset.toLowerCase(Locale.ROOT),

                            ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, requestTimoutMs
                    ),
                    new StringDeserializer(),
                    new StringDeserializer()
            );
            kafkaConsumers.add(i, kafkaConsumer);
        }
    }

    private List<String> getKafkaTopicsFromConsumerDTO(ConsumerDTO consumerDTO) {
        String callTopicName = consumerDTO.getCallTopicName();
        String geoTopicName = consumerDTO.getGeoTopicName();
        String urlTopicName = consumerDTO.getUrlTopicName();

        return Stream.of(callTopicName, geoTopicName, urlTopicName)
                .filter(Objects::nonNull)
                .toList();
    }

    private void reconnectKafkaConsumersToTopics(List<String> kafkaTopics) {
        kafkaConsumers.forEach(kafkaConsumer -> {
            log.debug("Subscribe to topics " + kafkaTopics);
            kafkaConsumer.subscribe(kafkaTopics);
        });
    }

    private void updateTopicValueFromDBAndItsName(ConsumerDTO consumerDTO) {
        String geoTopicName = consumerDTO.getGeoTopicName();
        String callTopicName = consumerDTO.getCallTopicName();
        String siteTopicName = consumerDTO.getUrlTopicName();

        if (geoTopicName != null) {
            topicValueFromDBAndItsName.put(geoTopicName, "geo");
            topicValueFromDBAndItsClass.put(geoTopicName, GeoDTO.class);
        }
        if (callTopicName != null) {
            topicValueFromDBAndItsName.put(callTopicName, "call");
            topicValueFromDBAndItsClass.put(callTopicName, CallDTO.class);
        }
        if (siteTopicName != null) {
            topicValueFromDBAndItsName.put(siteTopicName, "link");
            topicValueFromDBAndItsClass.put(siteTopicName, LinkDTO.class);
        }
    }
}
