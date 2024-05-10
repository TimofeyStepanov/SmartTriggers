package ru.stepanoff.repository.impl;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.stepanoff.exception.WrongKeyException;
import ru.stepanoff.repository.KeyValueRepository;

@Component
@RequiredArgsConstructor
public class CassandraKeyValueRepository implements KeyValueRepository<Long, String> {
    @Value("${cassandra.keyspaceName}")
    private String keyspaceName;

    @Value("${cassandra.tableName}")
    private String tableName;

    private final Session session;

    @Override
    public String get(Long userId) throws WrongKeyException {
        String query = "select phone from " + keyspaceName + "." + tableName + " where id = " + userId + ";";
        ResultSet result = session.execute(query);

        Row row = result.one();
        if (row == null ) {
            throw new WrongKeyException();
        }
        return row.getString(0);
    }

    @Override
    public void save(Long userId, String phone) {
        String query = "Insert into " + keyspaceName + "." + tableName + "(id, phone) values (" + userId + ", '" + phone + "');";
        session.execute(query);
    }

    @Override
    public void clear() {
        String query = "TRUNCATE " + keyspaceName + "." + tableName + ';';
        session.execute(query);
    }
}
