package club.thom.tem.constants;

import java.util.HashMap;
import java.util.Map;

public class PureColours {
    public static final Map<String, String> pureColoursToName = createPureColours();

    private static HashMap<String, String> createPureColours() {
        HashMap<String, String> pureColours = new HashMap<>();
        pureColours.put("993333", "RED");
        pureColours.put("D87F33", "ORANGE");
        pureColours.put("E5E533", "YELLOW");
        pureColours.put("7FCC19", "GREEN");
        pureColours.put("667F33", "DARK GREEN");
        pureColours.put("6699D8", "LIGHT BLUE");
        pureColours.put("4C7F99", "CYAN");
        pureColours.put("334CB2", "BLUE");
        pureColours.put("F27FA5", "PINK");
        pureColours.put("7F3FB2", "PURPLE");
        pureColours.put("B24CD8", "MAGENTA");
        pureColours.put("664C33", "BROWN");
        pureColours.put("FFFFFF", "WHITE");
        pureColours.put("999999", "LIGHT GRAY");
        pureColours.put("4C4C4C", "DARK GRAY");
        pureColours.put("191919", "BLACK");
        return pureColours;
    }

    public static boolean isPureColour(String hex) {
        return pureColoursToName.containsKey(hex.toUpperCase());
    }

    public static String getPureColour(String hex) {
        return pureColoursToName.get(hex.toUpperCase());
    }
}
