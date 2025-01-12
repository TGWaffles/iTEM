package club.thom.tem.storage.repositories;

import club.thom.tem.models.export.StoredItemData;
import club.thom.tem.models.export.StoredItemDataKey;
import club.thom.tem.models.export.StoredItemDataValue;
import club.thom.tem.storage.LocalDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ItemDataRepository implements TEMRepository {
    LocalDatabase localDatabase;
    public ItemDataRepository(LocalDatabase localDatabase) {
        this.localDatabase = localDatabase;
    }

    @Override
    public String getTableName() {
        return "ITEM_DATA";
    }

    @Override
    public void createTable() {
        try (Connection connection = localDatabase.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE \"" + getTableName() + "\" (id INT NOT NULL auto_increment, parent INT, data_key INT, data_value INT, PRIMARY KEY (id), CONSTRAINT item_data_parent_unique UNIQUE (parent, data_key, data_value))");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getSchemaVersion() {
        return 1;
    }

    public void postCreation() {
        try (Connection connection = localDatabase.getConnection();
             Statement firstStatement = connection.createStatement();
             Statement secondStatement = connection.createStatement();
             Statement thirdStatement = connection.createStatement();
             Statement fourthStatement = connection.createStatement()) {
            firstStatement.executeUpdate("ALTER TABLE \"" + getTableName() + "\" ADD CONSTRAINT IF NOT EXISTS item_data_parent_fk FOREIGN KEY (parent) REFERENCES \"" + getTableName() + "\"(id) ON DELETE CASCADE");
            secondStatement.executeUpdate("ALTER TABLE \"" + getTableName() + "\" ADD CONSTRAINT IF NOT EXISTS item_data_key_fk FOREIGN KEY (data_key) REFERENCES ITEM_DATA_KEYS(id)");
            thirdStatement.executeUpdate("ALTER TABLE \"" + getTableName() + "\" ADD CONSTRAINT IF NOT EXISTS item_data_value_fk FOREIGN KEY (data_value) REFERENCES ITEM_DATA_VALUES(id)");
            fourthStatement.executeUpdate("MERGE INTO \"" + getTableName() + "\" (id, parent, data_key, data_value) KEY(id) VALUES (-1, NULL, NULL, NULL)");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int insertData(Connection connection, Integer parentId, Integer keyId, Integer valueId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("INSERT INTO \"" + getTableName() + "\" (parent, data_key, data_value) VALUES (?, ?, ?)", PreparedStatement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, parentId);
            statement.setInt(2, keyId);
            statement.setInt(3, valueId);
            statement.executeUpdate();
            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (!generatedKeys.next()) {
                throw new RuntimeException("Failed to insert data");
            }
            return generatedKeys.getInt(1);
        } catch (SQLIntegrityConstraintViolationException e) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT id FROM \"" + getTableName() + "\" WHERE parent = ? AND data_key = ? AND data_value = ?")) {
                statement.setInt(1, parentId);
                statement.setInt(2, keyId);
                statement.setInt(3, valueId);
                ResultSet resultSet = statement.executeQuery();
                if (!resultSet.next()) {
                    throw new RuntimeException("Failed to get id for data");
                }
                return resultSet.getInt("id");
            }
        }
    }

    public StoredItemData getDataFromKey(Connection connection, int keyId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT a.parent, a.id AS data_id, k.id AS key_id, k.data_key, v.id AS value_id, v.data_value, v.data_type FROM \"" + getTableName() + "\" a INNER JOIN ITEM_DATA_KEYS k ON a.data_key = ? AND a.data_key = k.id INNER JOIN ITEM_DATA_VALUES v ON a.data_value = v.id")) {
            statement.setInt(1, keyId);
            ResultSet resultSet = statement.executeQuery();
            if (!resultSet.next()) {
                return null;
            }
            int parentId = resultSet.getInt("parent");
            StoredItemData parentReference = parentId == -1 ? null : getDataFromKey(connection, parentId);
            StoredItemDataKey keyReference = new StoredItemDataKey(resultSet.getInt("key_id"), resultSet.getString("data_key"));
            StoredItemDataValue valueReference = new StoredItemDataValue(resultSet.getInt("value_id"), resultSet.getString("data_value"), resultSet.getString("data_type"));
            return new StoredItemData(resultSet.getInt("data_id"), resultSet.getInt("parent"), parentReference, resultSet.getInt("key_id"), keyReference, resultSet.getInt("value_id"), valueReference);
        }
    }

    public StoredItemData getDataFromId(Connection connection, int id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT a.parent, a.id AS data_id, k.id AS key_id, k.data_key, v.id AS value_id, v.data_value, v.data_type FROM \"" + getTableName() + "\" a INNER JOIN ITEM_DATA_KEYS k ON a.id = ? AND a.data_key = k.id INNER JOIN ITEM_DATA_VALUES v ON a.data_value = v.id")) {
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();
            if (!resultSet.next()) {
                return null;
            }
            int parentId = resultSet.getInt("parent");
            StoredItemData parentReference = parentId == -1 ? null : getDataFromKey(connection, parentId);
            StoredItemDataKey keyReference = new StoredItemDataKey(resultSet.getInt("key_id"), resultSet.getString("data_key"));
            StoredItemDataValue valueReference = new StoredItemDataValue(resultSet.getInt("value_id"), resultSet.getString("data_value"), resultSet.getString("data_type"));
            return new StoredItemData(resultSet.getInt("data_id"), resultSet.getInt("parent"), parentReference, resultSet.getInt("key_id"), keyReference, resultSet.getInt("value_id"), valueReference);
        }
    }

    public List<StoredItemData> getChildren(Connection connection, StoredItemData parent) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT a.parent, a.id AS data_id, k.id AS key_id, k.data_key, v.id AS value_id, v.data_value, v.data_type FROM \"" + getTableName() + "\" a INNER JOIN ITEM_DATA_KEYS k ON a.parent = ? AND a.data_key = k.id INNER JOIN ITEM_DATA_VALUES v ON a.data_value = v.id")) {
            statement.setInt(1, parent.id);
            ResultSet resultSet = statement.executeQuery();
            List<StoredItemData> children = new ArrayList<>();
            while (resultSet.next()) {
                StoredItemDataKey keyReference = new StoredItemDataKey(resultSet.getInt("key_id"), resultSet.getString("data_key"));
                StoredItemDataValue valueReference = new StoredItemDataValue(resultSet.getInt("value_id"), resultSet.getString("data_value"), resultSet.getString("data_type"));
                children.add(new StoredItemData(resultSet.getInt("data_id"), resultSet.getInt("parent"), parent, resultSet.getInt("key_id"), keyReference, resultSet.getInt("value_id"), valueReference));
            }
            return children;
        }
    }
}
