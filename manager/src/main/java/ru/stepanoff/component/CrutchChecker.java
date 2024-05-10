package ru.stepanoff.component;

import jakarta.validation.Valid;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import ru.stepanoff.model.processor.CallDTO;
import ru.stepanoff.model.processor.GeoDTO;
import ru.stepanoff.model.processor.SiteDTO;
import ru.stepanoff.model.producer.KafkaProducerDTO;
import ru.stepanoff.model.producer.PostgresDTO;

@Component
@Validated
public class CrutchChecker {
    public void checkKafka(@Valid KafkaProducerDTO kafkaParamsDTO) {
    }
    public void checkPostgres(@Valid PostgresDTO postgresDTO) {
    }
    public void checkSiteDTO(@Valid SiteDTO siteDTO) {
    }
    public void checkGeo(@Valid GeoDTO geoInfo) {
    }
    public void checkCall(@Valid CallDTO callInfo) {
    }
}
