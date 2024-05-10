package ru.stepanoff.config;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CassandraConfig {
    @Bean
    public Session getCassandraSession(@Value("${cassandra.node}") String node, @Value("${cassandra.port}") int port) {
        Cluster.Builder b = Cluster.builder().addContactPoint(node);
        b.withPort(port);

        Cluster cluster = b.build();
        return cluster.connect();
    }

    @Bean
    public CommandLineRunner getInitScript(@Autowired Session session,
                                           @Value("${cassandra.keyspaceName}") String keyspaceName,
                                           @Value("${cassandra.strategy}") String replicationStrategy,
                                           @Value("${cassandra.replicationFactor}") int replicationFactor,
                                           @Value("${cassandra.tableName}") String tableName) {
        return args -> {
            String query = "CREATE KEYSPACE IF NOT EXISTS " +
                    keyspaceName + " WITH replication = {" +
                    "'class':'" + replicationStrategy +
                    "','replication_factor':" + replicationFactor +
                    "};";
            session.execute(query);

            query = "CREATE TABLE IF NOT EXISTS " +
                    keyspaceName + '.' + tableName +
                    "(id bigint PRIMARY KEY, phone text);";
            session.execute(query);
        };
    }
}
