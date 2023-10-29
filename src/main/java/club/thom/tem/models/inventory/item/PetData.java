package club.thom.tem.models.inventory.item;

import club.thom.tem.models.messages.ClientMessages.InventoryItem;
import club.thom.tem.models.messages.ClientMessages.Pet;
import club.thom.tem.models.messages.ClientMessages.PetSkin;
import club.thom.tem.models.messages.ClientMessages.Rarity;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.nbt.NBTTagCompound;

import java.util.HashMap;

public class PetData extends InventoryItemData {
    private final JsonObject petJson;
    private String inventoryName;

    private String uuid = null;
    /**
     * @param itemData NBT data of a pet in an inventory. This method gets the json and runs the json init method.
     */
    public PetData(String inventoryName, NBTTagCompound itemData) {
        this(new JsonParser().parse(itemData.getCompoundTag("tag").getCompoundTag("ExtraAttributes").getString("petInfo")).getAsJsonObject());
        this.inventoryName = inventoryName;
        if (itemData.getCompoundTag("tag").getCompoundTag("ExtraAttributes").hasKey("uuid")) {
            uuid = itemData.getCompoundTag("tag").getCompoundTag("ExtraAttributes").getString("uuid");
        }
    }

    public PetData(JsonObject petJson) {
        inventoryName = "pet_menu";
        this.petJson = petJson;
    }

    @Override
    public InventoryItem toInventoryItem() {
        Pet.Builder builder = Pet.newBuilder().setCandied(isCandied())
                .setRarity(getRarity()).setHeldItem(getHeldItem()).setLevel(getPetLevel(getExp(), getRarity()))
                .setName(getName()).setCandy(getCandyCount());
        if (getSkin() != null) {
            builder.setSkin(getSkin());
        }
        return InventoryItem.newBuilder()
                .setPet(builder).setUuid(getUuid())
                .setCreationTimestamp(getCreationTimestamp())
                .setLocation(inventoryName).build();
    }

    private String getUuid() {
        if (uuid != null) {
            return uuid;
        }
        if (petJson.has("uuid") && !petJson.get("uuid").isJsonNull()) {
            return petJson.get("uuid").getAsString();
        }
        String fakeUuid = petJson.get("type").getAsString() + "_+_" + getRarity().toString();
        if (petJson.has("skin") && !petJson.get("skin").isJsonNull()) {
            fakeUuid += "_+_" + petJson.get("skin").getAsString();
        }
        if (isCandied()) {
            fakeUuid += "_+_CANDIED";
        }
        return fakeUuid;
    }

    private long getCreationTimestamp() {
        return 0;
    }

    private String getName() {
        return petJson.get("type").getAsString();
    }

    private Rarity getRarity() {
        return Rarity.valueOf(petJson.get("tier").getAsString());
    }

    private boolean isCandied() {
        if (!petJson.has("candyUsed")) {
            return false;
        }
        return petJson.get("candyUsed").getAsInt() > 0;
    }

    private int getCandyCount() {
        if (!petJson.has("candyUsed")) {
            return 0;
        }
        return petJson.get("candyUsed").getAsInt();
    }

    public PetSkin getSkin() {
        if (!petJson.has("skin") || petJson.get("skin").isJsonNull()) {
            return null;
        }
        return new PetSkinData(petJson.get("skin").getAsString()).toPetSkinMessage();
    }

    private String getHeldItem() {
        if (!petJson.has("heldItem") || petJson.get("heldItem").isJsonNull()) {
            return "";
        }
        return petJson.get("heldItem").getAsString();
    }

    private int getExp() {
        return petJson.get("exp").getAsInt();
    }

    public static int getPetLevel(int xp, Rarity rarity) {
        // Thanks to NEU
        HashMap<Rarity, Integer> rarityOffset = new HashMap<>();
        rarityOffset.put(Rarity.COMMON, 0);
        rarityOffset.put(Rarity.UNCOMMON, 6);
        rarityOffset.put(Rarity.RARE, 11);
        rarityOffset.put(Rarity.EPIC, 16);
        rarityOffset.put(Rarity.LEGENDARY, 20);
        rarityOffset.put(Rarity.MYTHIC, 20);

        int[] levelData = new int[]{0, 100, 210, 330, 460, 605, 765, 940, 1130, 1340, 1570, 1820, 2095, 2395, 2725,
                3085, 3485, 3925, 4415, 4955, 5555, 6215, 6945, 7745, 8625, 9585, 10635, 11785, 13045, 14425, 15935,
                17585, 19385, 21345, 23475, 25785, 28285, 30985, 33905, 37065, 40485, 44185, 48185, 52535, 57285, 62485,
                68185, 74485, 81485, 89285, 97985, 107685, 118485, 130485, 143785, 158485, 174685, 192485, 221985,
                233285, 256485, 281685, 309085, 338885, 371285, 406485, 444685, 486085, 530885, 579285, 631485, 687685,
                748085, 812885, 882285, 956485, 1035685, 1120385, 1211085, 1308285, 1412485, 1524185, 1643885, 1772085,
                1909285, 2055958, 2212685, 2380385, 2560085, 2752785, 2959485, 3181185, 3418885, 3673585, 3946285,
                4237985, 4549685, 4883385, 5241085, 5624785, 6036485, 6478185, 6954885, 7471585, 8033285, 8644985,
                9311685, 10038385, 10830085, 11691785, 12628485, 13645185, 14746885, 15938585, 17225285, 18611985,
                20108685, 21725385, 23472085, 25358785};
        int startingXp = levelData[rarityOffset.get(rarity)];
        for (int i = 0; i < levelData.length; i++) {
            if (xp < levelData[i] - startingXp) {
                return Math.min(i - rarityOffset.get(rarity), 100);
            }
        }
        return 100;
    }

    public static boolean isValidItem(NBTTagCompound itemData) {
        NBTTagCompound extraAttributes = itemData.getCompoundTag("tag").getCompoundTag("ExtraAttributes");
        if (!extraAttributes.getString("id").equals("PET")) {
            return false;
        }
        try {
            JsonObject petData = new JsonParser().parse(itemData.getCompoundTag("tag").getCompoundTag("ExtraAttributes").getString("petInfo")).getAsJsonObject();
            // json was able to be successfully parsed, contains a valid pet (probably)
            return petData.has("type");
        } catch (IllegalStateException e) {
            e.printStackTrace();
            System.out.println(itemData.getCompoundTag("tag").getCompoundTag("ExtraAttributes").getString("petInfo"));
            System.out.println("Pet info ^^^ weird couldnt be parsed...");
            return false;
        }
    }

    @Override
    public String toString() {
        return "PET: " + getName() + " (" + getRarity().toString() + ") [Level. " + getPetLevel(getExp(), getRarity()) + "]" +
                "extraAttributes: " + petJson.toString();
    }

    @Override
    public JsonObject toJson() {
        JsonObject data = new JsonObject();
        data.addProperty("name", getName());
        data.addProperty("rarity", getRarity().toString());
        data.addProperty("level", getPetLevel(getExp(), getRarity()));
        data.addProperty("candied", isCandied());
        data.addProperty("heldItem", getHeldItem());
        data.addProperty("candyCount", getCandyCount());
        if (getSkin() != null) {
            data.addProperty("skin", getSkin().getSkinId());
        }

        data.add("extraAttributes", petJson);
        return data;
    }
}
