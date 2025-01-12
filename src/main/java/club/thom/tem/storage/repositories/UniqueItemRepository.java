package club.thom.tem.storage.repositories;

import club.thom.tem.models.export.StoredUniqueItem;
import club.thom.tem.storage.LocalDatabase;

import java.sql.*;

public class UniqueItemRepository implements TEMRepository {
    LocalDatabase localDatabase;
    public UniqueItemRepository(LocalDatabase localDatabase) {
        this.localDatabase = localDatabase;
    }

    @Override
    public String getTableName() {
        return "UNIQUE_ITEMS";
    }

    @Override
    public void createTable() {
        try (Connection connection = localDatabase.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE \"" + getTableName() + "\" (uuid VARCHAR(36) NOT NULL, item_id VARCHAR(255) NOT NULL, rarity INT NOT NULL, reforge VARCHAR(64), hexcode INT, creation_timestamp BIGINT, PRIMARY KEY (uuid))");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getSchemaVersion() {
        return 1;
    }

    @Override
    public void migrate() {
        try (Connection connection = localDatabase.getConnection()) {
            Statement statement = connection.createStatement();
            statement.executeUpdate("ALTER TABLE \"" + getTableName() + "\" RENAME TO \"" + getTableName() + "_old\"");
            statement.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        createTable();
        try (Connection connection = localDatabase.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet rst = statement.executeQuery("SELECT * FROM \"" + getTableName() + "_old\"");
            while (rst.next()) {
                StoredUniqueItem item = parseItem(rst);
                upsertItem(connection, item);
            }
            statement.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private StoredUniqueItem parseItem(ResultSet result) throws SQLException {
        return new StoredUniqueItem(result.getString("uuid"), result.getString("item_id"), result.getInt("rarity"), result.getString("reforge"), result.getInt("hexcode"), result.getLong("creation_timestamp"));
    }

    public void upsertItem(Connection connection, StoredUniqueItem item) {
        try (PreparedStatement statement = connection.prepareStatement("MERGE INTO \"" + getTableName() + "\" (uuid, item_id, rarity, reforge, hexcode, creation_timestamp) KEY(uuid) VALUES (?, ?, ?, ?, ?, ?)")) {
            statement.setString(1, item.uuid);
            statement.setString(2, item.itemId);
            statement.setInt(3, item.rarity.getNumber());
            statement.setString(4, item.reforge);
            statement.setInt(5, item.hexCode);
            statement.setLong(6, item.creationTimestamp);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public StoredUniqueItem getItemFromUUID(Connection connection, String uuid) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM \"" + getTableName() + "\" WHERE uuid = ?")) {
            statement.setString(1, uuid);
            ResultSet result = statement.executeQuery();
            if (!result.first()) {
                return null;
            }
            return parseItem(result);
        }
    }
}
