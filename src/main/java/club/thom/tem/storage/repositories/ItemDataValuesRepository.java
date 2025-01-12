package club.thom.tem.storage.repositories;

import club.thom.tem.models.export.StoredItemDataValue;
import club.thom.tem.storage.LocalDatabase;

import java.sql.*;

public class ItemDataValuesRepository implements TEMRepository {
    LocalDatabase localDatabase;
    public ItemDataValuesRepository(LocalDatabase localDatabase) {
        this.localDatabase = localDatabase;
    }

    @Override
    public String getTableName() {
        return "ITEM_DATA_VALUES";
    }

    @Override
    public void createTable() {
        try (Connection connection = localDatabase.getConnection();
             Statement creationStatement = connection.createStatement();
             Statement indexStatement = connection.createStatement()) {
            creationStatement.executeUpdate("CREATE TABLE \"" + getTableName() + "\" (id INT AUTO_INCREMENT PRIMARY KEY, data_value VARCHAR(255), data_type VARCHAR(16), CONSTRAINT item_data_values_unique UNIQUE (data_value, data_type))");
            indexStatement.executeUpdate("CREATE INDEX item_data_values_data_index ON \"" + getTableName() + "\" (data_value, data_type)");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getSchemaVersion() {
        return 1;
    }

    public int insertValueOrGetId(Connection connection, String value, String type) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("INSERT INTO \"" + getTableName() + "\" (data_value, data_type) VALUES (?, ?)", PreparedStatement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, value);
            statement.setString(2, type);
            statement.executeUpdate();
            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (!generatedKeys.next()) {
                throw new RuntimeException("Failed to insert value " + value + " of type " + type);
            }
            return generatedKeys.getInt(1);
        } catch (SQLIntegrityConstraintViolationException e) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT id FROM \"" + getTableName() + "\" WHERE data_value = ? AND data_type = ?")) {
                statement.setString(1, value);
                statement.setString(2, type);
                ResultSet resultSet = statement.executeQuery();
                if (!resultSet.next()) {
                    throw new RuntimeException("Failed to get id for value " + value + " of type " + type);
                }
                return resultSet.getInt("id");
            }
        }
    }

    public StoredItemDataValue getValueFromId(Connection connection, int id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT data_key FROM \"" + getTableName() + "\" WHERE id = ?")) {
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();
            if (!resultSet.next()) {
                return null;
            }
            return new StoredItemDataValue(resultSet.getInt("id"), resultSet.getString("data_value"), resultSet.getString("data_type"));
        }
    }
}
