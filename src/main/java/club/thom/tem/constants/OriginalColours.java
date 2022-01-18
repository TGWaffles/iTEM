package club.thom.tem.constants;

import java.util.HashMap;

public class OriginalColours {
    public static HashMap<String, ArmourColour> OriginalColours = new HashMap<>();
    //Dragons
    static ArmourColour superior = new ArmourColour("F2DF11", "F2DF11", "F25D18");
    static ArmourColour wise = new ArmourColour("29F0E9");
    static ArmourColour young = new ArmourColour("DDE4F0");
    static ArmourColour strong = new ArmourColour("D91E41", "E09419", "F0D124");
    static ArmourColour unstable = new ArmourColour("B212E3");
    static ArmourColour old = new ArmourColour("F0E6AA");
    static ArmourColour protector = new ArmourColour("99978B");
    //Slayers
    static ArmourColour tara = new ArmourColour("0", "", "", "0");
    //Fishing
    static ArmourColour shark = new ArmourColour("002CA6");
    static ArmourColour angler = new ArmourColour("0B004F");
    static ArmourColour squidBoots = new ArmourColour("", "0");
    //Event
    static ArmourColour batPerson = new ArmourColour("0");
    //Craftable
    static ArmourColour farmerBoots = new ArmourColour("", "CC5500");
    static ArmourColour farmSuit = new ArmourColour("FFFF00");
    static ArmourColour farmArmour = new ArmourColour("FFD700");
    static ArmourColour speeedster = new ArmourColour("E0FCF7");
    static ArmourColour cactus = new ArmourColour("00FF00");
    static ArmourColour leaflet = new ArmourColour("4DCC4D");
    static ArmourColour minerOutfit = new ArmourColour("7A7964");
    static ArmourColour stereo = new ArmourColour("", "04CFD3");
    static ArmourColour mushroom = new ArmourColour("FF0000");
    static ArmourColour guardianChest = new ArmourColour("117391", "", "");
    static ArmourColour creeperLegs = new ArmourColour("", "7AE82C", "");
    static ArmourColour emerald = new ArmourColour("00FF00");
    static ArmourColour pumpkin = new ArmourColour("EDAA36");
    //Others
    static ArmourColour obsidianChest = new ArmourColour("0", "", "");
    static ArmourColour lapisArmour = new ArmourColour("0000FF");
    //Tux
    static ArmourColour cheapTux = new ArmourColour("383838", "C7C7C7", "383838");
    static ArmourColour fancyTux = new ArmourColour("332A2A", "D4D4D4", "332A2A");
    static ArmourColour elegantTux = new ArmourColour("191919", "FEFDFC", "191919");

    public static void PrepareMap() {
        //TODO: add all the item ids
        OriginalColours.put("Superior", superior);
    }
}
