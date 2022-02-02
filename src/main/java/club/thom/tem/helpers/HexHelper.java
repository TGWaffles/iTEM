package club.thom.tem.helpers;

import com.google.common.base.Strings;

public class HexHelper {
    public static String convertSmallerHex(String smallerHex) {
        if (smallerHex.length() == 1) {
            return Strings.repeat(smallerHex, 6);
        } else if (smallerHex.length() == 3) {
            char[] characters = smallerHex.toCharArray();
            return Strings.repeat(Character.toString(characters[0]), 2) +
                    Strings.repeat(Character.toString(characters[1]), 2) +
                    Strings.repeat(Character.toString(characters[2]), 2);
        } else {
            // Assume it's a 6 digit hex.
            return smallerHex;
        }
    }
}
