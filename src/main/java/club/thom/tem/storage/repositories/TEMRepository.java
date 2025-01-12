package club.thom.tem.storage.repositories;

import java.util.List;
import java.util.Map;

public interface TEMRepository {
    String getTableName();
    void createTable();
    int getSchemaVersion();
    default void migrate() {
        // Do nothing
    }
    default void postCreation() {
        // Do nothing
    }
}
