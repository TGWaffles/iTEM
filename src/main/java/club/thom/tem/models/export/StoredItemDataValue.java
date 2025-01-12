package club.thom.tem.models.export;

public class StoredItemDataValue {
    int id;
    String value;
    String type;

    public StoredItemDataValue(int id, String value, String type) {
        this.id = id;
        this.value = value;
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public String getValue() {
        return value;
    }

    public String getType() {
        return type;
    }

    public boolean canHaveChildren() {
        switch (type) {
            case "STRING":
            case "INT":
            case "LONG":
            case "DOUBLE":
                return false;
            case "LIST":
            case "COMPOUND":
            case "ITEM":
                return true;
            default:
                throw new IllegalArgumentException("Unknown type: " + type);
        }
    }
}
