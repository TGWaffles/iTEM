package club.thom.tem.models.export;

public class StoredItemDataKey {
    int id;
    String key;

    public StoredItemDataKey(int id, String key) {
        this.id = id;
        this.key = key;
    }

    public int getId() {
        return id;
    }

    public String getKey() {
        return key;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof StoredItemDataKey) {
            StoredItemDataKey other = (StoredItemDataKey) obj;
            return other.id == this.id && other.key.equals(this.key);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return id + key.hashCode();
    }
}
