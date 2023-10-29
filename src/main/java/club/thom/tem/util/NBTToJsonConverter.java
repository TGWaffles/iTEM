package club.thom.tem.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.nbt.*;

public class NBTToJsonConverter {

    /**
     * Entry method to convert NBTTagCompound to JsonObject.
     *
     * @param nbtCompound The NBTTagCompound to convert.
     * @return A JsonObject representation of the NBTTagCompound.
     */
    public static JsonObject convertToJSON(NBTTagCompound nbtCompound) {
        JsonObject jsonObject = new JsonObject();

        for (String key : nbtCompound.getKeySet()) {
            NBTBase nbtBase = nbtCompound.getTag(key);
            jsonObject.add(key, convertBaseToJSON(nbtBase));
        }

        return jsonObject;
    }

    /**
     * Handles the conversion of each NBTBase type to its corresponding JsonElement.
     *
     * @param nbtBase The NBTBase to convert.
     * @return A JsonElement corresponding to the NBTBase.
     */
    private static JsonElement convertBaseToJSON(NBTBase nbtBase) {
        switch (nbtBase.getId()) {
            case 1:
                return new JsonPrimitive(((NBTTagByte) nbtBase).getByte());
            case 2:
                return new JsonPrimitive(((NBTTagShort) nbtBase).getShort());
            case 3:
                return new JsonPrimitive(((NBTTagInt) nbtBase).getInt());
            case 4:
                return new JsonPrimitive(((NBTTagLong) nbtBase).getLong());
            case 5:
                return new JsonPrimitive(((NBTTagFloat) nbtBase).getFloat());
            case 6:
                return new JsonPrimitive(((NBTTagDouble) nbtBase).getDouble());
            case 8:
                return new JsonPrimitive(((NBTTagString) nbtBase).getString());
            case 9:
                return convertListToJSON((NBTTagList) nbtBase);
            case 10:
                return convertToJSON((NBTTagCompound) nbtBase);
            default:
                return null; // Handle other types as needed
        }
    }

    /**
     * Handles the conversion of NBTTagList to JsonArray.
     *
     * @param nbtList The NBTTagList to convert.
     * @return A JsonArray representation of the NBTTagList.
     */
    private static JsonArray convertListToJSON(NBTTagList nbtList) {
        JsonArray jsonArray = new JsonArray();

        for (int i = 0; i < nbtList.tagCount(); i++) {
            NBTBase element = nbtList.get(i);
            jsonArray.add(convertBaseToJSON(element));
        }

        return jsonArray;
    }
}
