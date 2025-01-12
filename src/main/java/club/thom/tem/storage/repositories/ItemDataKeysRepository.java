package club.thom.tem.storage.repositories;

import club.thom.tem.storage.LocalDatabase;

import javax.xml.transform.Result;
import java.sql.*;

public class ItemDataKeysRepository implements TEMRepository {
    LocalDatabase localDatabase;
    public ItemDataKeysRepository(LocalDatabase localDatabase) {
        this.localDatabase = localDatabase;
    }

    @Override
    public String getTableName() {
        return "ITEM_DATA_KEYS";
    }

    @Override
    public void createTable() {
        try (Connection connection = localDatabase.getConnection();
             Statement creationStatement = connection.createStatement();
             Statement indexStatement = connection.createStatement()) {
            creationStatement.executeUpdate("CREATE TABLE \"" + getTableName() + "\" (id INT AUTO_INCREMENT PRIMARY KEY, data_key VARCHAR(255) UNIQUE)");
            indexStatement.executeUpdate("CREATE INDEX item_data_keys_key_index ON \"" + getTableName() + "\" (data_key)");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getSchemaVersion() {
        return 1;
    }

    public int insertKeyOrGetId(Connection connection, String key) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("INSERT INTO \"" + getTableName() + "\" (data_key) VALUES (?)", PreparedStatement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, key);
            statement.executeUpdate();
            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (!generatedKeys.next()) {
                throw new RuntimeException("Failed to insert key " + key);
            }
            return generatedKeys.getInt(1);
        } catch (SQLIntegrityConstraintViolationException e) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT id FROM \"" + getTableName() + "\" WHERE data_key = ?")) {
                statement.setString(1, key);
                ResultSet resultSet = statement.executeQuery();
                if (!resultSet.next()) {
                    throw new RuntimeException("Failed to get id for key " + key);
                }
                return resultSet.getInt("id");
            }
        }
    }

    public String getKeyFromId(Connection connection, int id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT data_key FROM \"" + getTableName() + "\" WHERE id = ?")) {
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("data_key");
            } else {
                return null;
            }
        }
    }

    public Integer getIdFromKey(Connection connection, String key) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT id FROM \"" + getTableName() + "\" WHERE data_key = ?")) {
            statement.setString(1, key);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("id");
            } else {
                return null;
            }
        }
    }
}
