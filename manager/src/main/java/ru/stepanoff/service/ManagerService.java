package ru.stepanoff.service;

import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import ru.stepanoff.model.consumer.ConsumerDTO;
import ru.stepanoff.model.processor.ProcessorDTO;
import ru.stepanoff.model.producer.ProducerDTO;

@Validated
public interface ManagerService {
    void updateConsumerParams(@Valid ConsumerDTO consumerDTO);
    void updateProcessorRules(@Valid ProcessorDTO processorDTO);
    void updateProducerParams(@Valid ProducerDTO producerDTO);

    ConsumerDTO getConsumerParams();
    ProcessorDTO getProcessorParams();
    ProducerDTO getProducerParams();
}
