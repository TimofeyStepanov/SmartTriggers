package ru.stepanoff.service.impl;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.stepanoff.component.CrutchChecker;
import ru.stepanoff.entity.consumer.ConsumerEntity;
import ru.stepanoff.entity.processor.*;
import ru.stepanoff.entity.producer.KafkaProducerEntity;
import ru.stepanoff.entity.producer.PostgresEntity;
import ru.stepanoff.entity.producer.ProducerEntity;
import ru.stepanoff.model.consumer.ConsumerDTO;
import ru.stepanoff.model.processor.*;
import ru.stepanoff.model.producer.KafkaProducerDTO;
import ru.stepanoff.model.producer.PostgresDTO;
import ru.stepanoff.model.producer.ProducerDTO;
import ru.stepanoff.repository.consumer.ConsumerRepository;
import ru.stepanoff.repository.processor.*;
import ru.stepanoff.repository.producer.KafkaProducerRepository;
import ru.stepanoff.repository.producer.PostgresRepository;
import ru.stepanoff.repository.producer.ProducerRepository;
import ru.stepanoff.service.ManagerService;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Validated
@RequiredArgsConstructor
public class ManagerServiceImpl implements ManagerService {
    private final ModelMapper mapper;

    private final CrutchChecker crutchChecker;
    private final ConsumerRepository consumerRepository;
    private final ProducerRepository producerRepository;
    private final ProcessorRepository processorRepository;
    private final SiteRepository siteRepository;
    private final CallRepository callRepository;
    private final LinkRepository linkRepository;
    private final GeoRepository geoRepository;
    private final PostgresRepository postgresRepository;
    private final KafkaProducerRepository kafkaProducerRepository;

    private ConsumerDTO consumerDTO;
    private ProcessorDTO processorDTO;
    private ProducerDTO producerDTO;

    @Override
    public void updateConsumerParams(@Valid ConsumerDTO consumerDTO) {
        if (consumerDTOHasNoTopic(consumerDTO)) {
            throw new IllegalArgumentException("No valid topic. Must be at least one topic");
        }
        ConsumerEntity consumerEntity = mapper.map(consumerDTO, ConsumerEntity.class);
        consumerRepository.save(consumerEntity);

        this.consumerDTO = consumerDTO;
    }

    private boolean consumerDTOHasNoTopic(ConsumerDTO consumerDTO) {
        String geoTopicName = consumerDTO.getGeoTopicName();
        String callTopicName = consumerDTO.getCallTopicName();
        String urlTopicName = consumerDTO.getUrlTopicName();

        return (geoTopicName == null || geoTopicName.isBlank())
                && (callTopicName == null || callTopicName.isBlank())
                && (urlTopicName == null || urlTopicName.isBlank());
    }


    @Override
    public void updateProcessorRules(@Valid ProcessorDTO processorDTO) {
        if (processorDTO.getSiteDTO() == null && processorDTO.getCallInfo() == null && processorDTO.getGeoInfo() == null) {
            throw new IllegalArgumentException("No rules!");
        }

        SiteEntity siteEntity = saveSite(processorDTO);
        CallEntity callEntity = saveCall(processorDTO);
        GeoEntity geoEntity = saveGeo(processorDTO);

        ProcessorEntity processorEntity = mapper.map(processorDTO, ProcessorEntity.class);
        processorEntity.setSiteEntity(siteEntity);
        processorEntity.setCallEntity(callEntity);
        processorEntity.setGeoEntity(geoEntity);
        processorEntity.setIntervalInSeconds(processorDTO.getIntervalInSeconds());
        processorRepository.save(processorEntity);

        this.processorDTO = processorDTO;
    }


    private SiteEntity saveSite(ProcessorDTO processorDTO) {
        SiteDTO siteDTO = processorDTO.getSiteDTO();
        if (siteDTO == null) {
            siteDTO = new SiteDTO();
            siteDTO.setLinks(new HashSet<>());
            siteDTO.setClickNumber(0);
        }
        crutchChecker.checkSiteDTO(siteDTO);

        boolean anyLinkUrlContainsComma = siteDTO.getLinks().stream().map(LinkDTO::getUrl).anyMatch(url -> url.contains(","));
        if (anyLinkUrlContainsComma) {
            throw new IllegalArgumentException("Comma is not allowed in url");
        }
        SiteEntity siteEntity = mapper.map(siteDTO, SiteEntity.class);
        siteRepository.save(siteEntity);

        Set<LinkEntity> linkEntitySet = siteEntity.getLinks()
                .stream()
                .map(linkDTO -> {
                    LinkEntity linkEntity = mapper.map(linkDTO, LinkEntity.class);
                    linkEntity.setSite(siteEntity);
                    linkRepository.save(linkEntity);
                    return linkEntity;
                })
                .collect(Collectors.toSet());
        siteEntity.setLinks(linkEntitySet);
        siteRepository.save(siteEntity);

        return siteEntity;
    }

    private CallEntity saveCall(ProcessorDTO processorDTO) {
        CallDTO callDTO = processorDTO.getCallInfo();
        if (callDTO != null) {
            crutchChecker.checkCall(processorDTO.getCallInfo());
        } else {
            callDTO = new CallDTO();
        }
        CallEntity callEntity = mapper.map(callDTO, CallEntity.class);
        callRepository.save(callEntity);
        return callEntity;
    }

    private GeoEntity saveGeo(ProcessorDTO processorDTO) {
        GeoDTO geoDTO = processorDTO.getGeoInfo();
        if (geoDTO != null) {
            crutchChecker.checkGeo(processorDTO.getGeoInfo());
        } else {
            geoDTO = new GeoDTO();
        }
        GeoEntity geoEntity = mapper.map(geoDTO, GeoEntity.class);
        geoRepository.save(geoEntity);
        return geoEntity;
    }

    @Override
    public void updateProducerParams(@Valid ProducerDTO producerDTO) {
        KafkaProducerEntity kafkaProducerEntity = saveKafka(producerDTO);
        PostgresEntity postgresEntity = savePostgres(producerDTO);

        ProducerEntity producerEntity = mapper.map(producerDTO, ProducerEntity.class);
        producerEntity.setKafkaProducer(kafkaProducerEntity);
        producerEntity.setPostgresEntity(postgresEntity);
        producerEntity.setWriteInFile(producerDTO.getWriteInFile());
        producerRepository.save(producerEntity);

        this.producerDTO = producerDTO;
    }

    private KafkaProducerEntity saveKafka(ProducerDTO producerDTO) {
        KafkaProducerDTO kafkaProducerDTO = producerDTO.getKafkaParamsDTO();
        if (kafkaProducerDTO != null) {
            crutchChecker.checkKafka(kafkaProducerDTO);
        } else {
            kafkaProducerDTO = new KafkaProducerDTO();
        }
        KafkaProducerEntity kafkaProducerEntity = mapper.map(kafkaProducerDTO, KafkaProducerEntity.class);
        kafkaProducerRepository.save(kafkaProducerEntity);
        return kafkaProducerEntity;
    }

    private PostgresEntity savePostgres(ProducerDTO producerDTO) {
        PostgresDTO postgresDTO = producerDTO.getPostgresDTO();
        if (postgresDTO != null) {
            crutchChecker.checkPostgres(postgresDTO);
        } else {
            postgresDTO = new PostgresDTO();
        }
        PostgresEntity postgresEntity = mapper.map(postgresDTO, PostgresEntity.class);
        postgresRepository.save(postgresEntity);
        return postgresEntity;
    }


    @Override
    public ConsumerDTO getConsumerParams() {
        return consumerDTO;
    }

    @Override
    public ProcessorDTO getProcessorParams() {
        return processorDTO;
    }

    @Override
    public ProducerDTO getProducerParams() {
        return producerDTO;
    }

}
