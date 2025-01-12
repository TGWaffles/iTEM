package club.thom.tem.storage;

import club.thom.tem.TEM;
import club.thom.tem.storage.repositories.*;
import club.thom.tem.storage.services.UniqueItemService;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reflections.Reflections;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocalDatabase {
    private static final Logger logger = LogManager.getLogger(LocalDatabase.class);
    private Map<String, TEMRepository> repositories = null;
    private TEM tem;
    private String filePath = "/item/datastore";
    private UniqueItemService uniqueItemService;

    public LocalDatabase(TEM tem) {
        this.tem = tem;
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            logger.error("Failed to load H2 driver", e);
            // this should never be thrown
            throw new RuntimeException(e);
        }
    }

    private Map<String, TEMRepository> buildRepositoryList() {
        Map<String, TEMRepository> repositories = new HashMap<>();
        Reflections reflections = new Reflections("club.thom.tem.storage.repositories");
        for (Class<? extends TEMRepository> extensionClass : reflections.getSubTypesOf(TEMRepository.class)) {
            TEMRepository repository;
            try {
                repository = extensionClass.getConstructor(LocalDatabase.class).newInstance(this);
                repositories.put(repository.getTableName(), repository);
            } catch (Exception e) {
                logger.error("Failed to instantiate repository", e);
            }
        }

        return repositories;
    }

    public void initialize(FMLPreInitializationEvent event) {
        logger.info("Initializing local database");
        setFilePath(event.getModConfigurationDirectory() + filePath);
        repositories = buildRepositoryList();
        createTables();
        uniqueItemService = new UniqueItemService(this);
    }

    private List<String> getTableNames() {
        List<String> tableNames = new ArrayList<>();
        try (Connection connection = getConnection()) {
            ResultSet rst = connection.getMetaData().getTables(null, null, null, new String[]{"TABLE"});
            while (rst.next()) {
                tableNames.add(rst.getString("TABLE_NAME"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return tableNames;
    }

    private void createSchemaVersionsTable() {
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE \"TABLE_SCHEMA_VERSIONS\" (table_name VARCHAR(255), version INT, PRIMARY KEY (table_name))");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private int getVersionForTable(String tableName) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT version FROM \"TABLE_SCHEMA_VERSIONS\" WHERE table_name = ?")) {
            statement.setString(1, tableName);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("version");
            } else {
                return -1;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void upsertVersionForTable(String tableName, int version) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement("MERGE INTO \"TABLE_SCHEMA_VERSIONS\" (table_name, version) KEY(table_name) VALUES (?, ?)")) {
            statement.setString(1, tableName);
            statement.setInt(2, version);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void createTables() {
        List<String> tableNames = getTableNames();
        if (!tableNames.contains("TABLE_SCHEMA_VERSIONS")) {
            createSchemaVersionsTable();
        }

        for (TEMRepository repository : repositories.values()) {
            int currentVersion = getVersionForTable(repository.getTableName());
            if (currentVersion == repository.getSchemaVersion()) {
                continue;
            }
            if (currentVersion < 0) {
                logger.info("Creating table {}", repository.getTableName());
                repository.createTable();
                upsertVersionForTable(repository.getTableName(), repository.getSchemaVersion());
                continue;
            }
            logger.info("Migrating table {}", repository.getTableName());
            repository.migrate();
            upsertVersionForTable(repository.getTableName(), repository.getSchemaVersion());
        }

        for (TEMRepository repository : repositories.values()) {
            repository.postCreation();
        }
    }

    public Connection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(getUrl());
        if (connection == null) {
            throw new SQLException("Failed to establish connection to database (unknown)");
        }
        return connection;
    }

    private String getUrl() {
        return "jdbc:h2:file:" + getFilePath();
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public TEM getTEM() {
        return tem;
    }

    public UniqueItemRepository getUniqueItemRepository() {
        return (UniqueItemRepository) repositories.get("UNIQUE_ITEMS");
    }

    public ItemDataRepository getItemDataRepository() {
        return (ItemDataRepository) repositories.get("ITEM_DATA");
    }

    public ItemDataKeysRepository getItemDataKeysRepository() {
        return (ItemDataKeysRepository) repositories.get("ITEM_DATA_KEYS");
    }

    public ItemDataValuesRepository getItemDataValuesRepository() {
        return (ItemDataValuesRepository) repositories.get("ITEM_DATA_VALUES");
    }

    public UniqueItemService getUniqueItemService() {
        return uniqueItemService;
    }
}
