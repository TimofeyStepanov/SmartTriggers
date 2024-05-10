package ru.stepanoff.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClickHouseConfig {
    @Bean
    public HikariDataSource getHikariDataSource(@Value("${clickHouse.url}") String url,
                                                @Value("${clickHouse.userName}") String userName,
                                                @Value("${clickHouse.driverName}") String driverName) {
        HikariConfig dataBaseConfig = new HikariConfig();
        dataBaseConfig.setJdbcUrl(url);
        dataBaseConfig.setUsername(userName);
        dataBaseConfig.setDriverClassName(driverName);
        return new HikariDataSource(dataBaseConfig);
    }
}
