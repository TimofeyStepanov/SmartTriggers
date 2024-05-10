package ru.stepanoff.config;

import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.modelmapper.TypeMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.stepanoff.entity.CallEntity;
import ru.stepanoff.entity.GeoEntity;
import ru.stepanoff.entity.LinkEntity;
import ru.stepanoff.model.request.Call;
import ru.stepanoff.model.request.Geo;
import ru.stepanoff.model.request.Link;

import java.util.UUID;

@Configuration
public class MapperConfig {
    @Bean
    public ModelMapper createMapper() {
        ModelMapper mapper = new ModelMapper();

        TypeMap<Geo, GeoEntity> geoPropertyMapper = mapper.createTypeMap(Geo.class, GeoEntity.class);
        geoPropertyMapper.addMappings(
                m -> m.map(src -> UUID.randomUUID(), GeoEntity::setId)
        );

        PropertyMap<Link, LinkEntity> skipLinkFieldsMap = new PropertyMap<>() {
            protected void configure() {
                skip().setId(null);
            }
        };
        mapper.addMappings(skipLinkFieldsMap);


        PropertyMap<Call, CallEntity> skipCallFieldsMap = new PropertyMap<>() {
            protected void configure() {
                skip().setId(null);
            }
        };
        mapper.addMappings(skipCallFieldsMap);
        return mapper;
    }
}
