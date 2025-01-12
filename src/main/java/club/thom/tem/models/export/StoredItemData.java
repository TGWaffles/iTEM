package club.thom.tem.models.export;

import java.util.ArrayList;
import java.util.List;

public class StoredItemData {
    public int id;
    public int parent;
    private StoredItemData parentReference;
    public int key;
    private StoredItemDataKey keyReference;
    public int value;
    private StoredItemDataValue valueReference;
    private List<StoredItemData> children = new ArrayList<>();

    public StoredItemData(int id, int parent, StoredItemData parentReference, int key, StoredItemDataKey keyReference, int value, StoredItemDataValue valueReference) {
        this.id = id;
        this.parent = parent;
        this.parentReference = parentReference;
        this.key = key;
        this.keyReference = keyReference;
        this.value = value;
        this.valueReference = valueReference;
    }

    public int getId() {
        return id;
    }

    public int getParentId() {
        return parent;
    }

    public void addChild(StoredItemData child) {
        children.add(child);
    }

    public StoredItemDataValue getValueReference() {
        return valueReference;
    }

    public StoredItemDataKey getKeyReference() {
        return keyReference;
    }

    public StoredItemData getChild(String key) {
        for (StoredItemData child : children) {
            if (child.keyReference.getKey().equals(key)) {
                return child;
            }
        }
        return null;
    }

    public List<StoredItemData> getChildren() {
        return children;
    }
}
