package ru.stepanoff.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class ConsumerEntityDTO {
    private long id;

    private String bootstrapServers;
    private Integer requestTimoutMs;
    private String kafkaAutoOffsetReset;

    private String urlTopicName;
    private String callTopicName;
    private String geoTopicName;

    private int consumerNumber;
}


