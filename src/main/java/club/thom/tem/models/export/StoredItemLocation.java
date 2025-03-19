package club.thom.tem.models.export;

import org.dizitart.no2.Document;
import org.dizitart.no2.mapper.Mappable;
import org.dizitart.no2.mapper.NitriteMapper;

public class StoredItemLocation implements Mappable {
    String profileId;
    String type;
    // Kept so nitrite doesn't think we're making an index on an absent field
    String position;
    int[] positionInternal;

    public StoredItemLocation() {
    }

    public StoredItemLocation(String profileId, String type, int[] position) {
        this.profileId = profileId;
        this.type = type;
        this.positionInternal = position;
    }

    public String getProfileId() {
        return profileId;
    }

    public String getType() {
        return type;
    }

    public void setType(String newType) {
        this.type = newType;
    }

    public void setPosition(int[] position) {
        this.positionInternal = position;
    }

    public int[] getPosition() {
        return positionInternal;
    }

    public String positionToString() {
        if (positionInternal == null || positionInternal.length == 0) {
            return null;
        }
        return String.format("%d %d %d", positionInternal[0], positionInternal[1], positionInternal[2]);
    }

    public String toString() {
        int[] position = getPosition();
        if (position != null && position.length == 3) {
            return String.format("%s @ %d,%d,%d", type, position[0], position[1], position[2]);
        }
        return type;
    }

    @Override
    public Document write(NitriteMapper mapper) {
        String positionAsString = null;
        if (getPosition() != null && getPosition().length > 0) {
            StringBuilder positionStringBuilder = new StringBuilder();
            for (int i = 0; i < getPosition().length; i++) {
                positionStringBuilder.append(getPosition()[i]);
                if (i < getPosition().length - 1) {
                    positionStringBuilder.append(" ");
                }
            }
            positionAsString = positionStringBuilder.toString();
        }

        return Document.createDocument("profileId", getProfileId())
                .put("type", getType())
                .put("position", positionAsString);
    }

    @Override
    public void read(NitriteMapper mapper, Document document) {
        Object positionAsString = document.get("position");
        int[] position = null;
        if (positionAsString != null) {
            String[] positionSplit = ((String) positionAsString).split(" ");
            position = new int[positionSplit.length];
            for (int i = 0; i < positionSplit.length; i++) {
                position[i] = Integer.parseInt(positionSplit[i]);
            }
        }
        this.profileId = document.get("profileId", String.class);
        this.type = document.get("type", String.class);
        this.positionInternal = position;
    }
}
