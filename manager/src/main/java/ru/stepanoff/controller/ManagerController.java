package ru.stepanoff.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.stepanoff.model.consumer.ConsumerDTO;
import ru.stepanoff.model.processor.ProcessorDTO;
import ru.stepanoff.model.producer.ProducerDTO;
import ru.stepanoff.service.ManagerService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("manager")
public class ManagerController {
    private final ManagerService managerService;

    @PostMapping("/update/input")
    @Operation(summary = "Обновить правила потребителя")
    public ResponseEntity<String> updateConsumerParams(@RequestBody ConsumerDTO consumerDTO) {
        try {
            managerService.updateConsumerParams(consumerDTO);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>("Update consumer rule", HttpStatus.OK);
    }

    @PostMapping("/update/processor")
    @Operation(summary = "Обновить правила процессора")
    public ResponseEntity<String> updateProcessorRules(@RequestBody ProcessorDTO processorDTO) {
        try {
            managerService.updateProcessorRules(processorDTO);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>("Update processor rule", HttpStatus.OK);
    }

    @PostMapping("/update/output")
    @Operation(summary = "Обновить правила продюсера")
    public ResponseEntity<String> updateProducerParams(@RequestBody ProducerDTO producerDTO) {
        try {
            managerService.updateProducerParams(producerDTO);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>("Update producer rule", HttpStatus.OK);
    }

    @GetMapping("/get/input")
    @Operation(summary = "Получить правила потребителя")
    public ConsumerDTO getConsumerParams() {
        return managerService.getConsumerParams();
    }

    @GetMapping("/get/processor")
    @Operation(summary = "Получить правила процессора")
    public ProcessorDTO getProcessorParams() {
        return managerService.getProcessorParams();
    }

    @GetMapping("/get/output")
    @Operation(summary = "Получить правила продюсера")
    public ProducerDTO getProducerParams() {
        return managerService.getProducerParams();
    }
}
