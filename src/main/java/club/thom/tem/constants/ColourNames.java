package club.thom.tem.constants;

import java.awt.*;
import java.util.ArrayList;

/**
 * Java Code to get a color name from rgb/hex value/awt color
 * <p>
 * The part of looking up a color name from the rgb values is edited from
 * https://gist.github.com/nightlark/6482130#file-gistfile1-java (that has some errors) by Ryan Mast (nightlark)
 *
 * @author Xiaoxiao Li
 */
public class ColourNames {
    public static final ArrayList<ColourName> colourList = initColorList();
    /**
     * Initialize the color list that we have.
     */
    private static ArrayList<ColourName> initColorList() {
        ArrayList<ColourName> colourList = new ArrayList<>();
        colourList.add(new ColourName("AliceBlue", 0xF0, 0xF8, 0xFF));
        colourList.add(new ColourName("AntiqueWhite", 0xFA, 0xEB, 0xD7));
        colourList.add(new ColourName("Aqua", 0x00, 0xFF, 0xFF));
        colourList.add(new ColourName("Aquamarine", 0x7F, 0xFF, 0xD4));
        colourList.add(new ColourName("Azure", 0xF0, 0xFF, 0xFF));
        colourList.add(new ColourName("Beige", 0xF5, 0xF5, 0xDC));
        colourList.add(new ColourName("Bisque", 0xFF, 0xE4, 0xC4));
        colourList.add(new ColourName("Black", 0x00, 0x00, 0x00));
        colourList.add(new ColourName("BlanchedAlmond", 0xFF, 0xEB, 0xCD));
        colourList.add(new ColourName("Blue", 0x00, 0x00, 0xFF));
        colourList.add(new ColourName("BlueViolet", 0x8A, 0x2B, 0xE2));
        colourList.add(new ColourName("Brown", 0xA5, 0x2A, 0x2A));
        colourList.add(new ColourName("BurlyWood", 0xDE, 0xB8, 0x87));
        colourList.add(new ColourName("CadetBlue", 0x5F, 0x9E, 0xA0));
        colourList.add(new ColourName("Chartreuse", 0x7F, 0xFF, 0x00));
        colourList.add(new ColourName("Chocolate", 0xD2, 0x69, 0x1E));
        colourList.add(new ColourName("Coral", 0xFF, 0x7F, 0x50));
        colourList.add(new ColourName("CornflowerBlue", 0x64, 0x95, 0xED));
        colourList.add(new ColourName("Cornsilk", 0xFF, 0xF8, 0xDC));
        colourList.add(new ColourName("Crimson", 0xDC, 0x14, 0x3C));
        colourList.add(new ColourName("Cyan", 0x00, 0xFF, 0xFF));
        colourList.add(new ColourName("DarkBlue", 0x00, 0x00, 0x8B));
        colourList.add(new ColourName("DarkCyan", 0x00, 0x8B, 0x8B));
        colourList.add(new ColourName("DarkGoldenRod", 0xB8, 0x86, 0x0B));
        colourList.add(new ColourName("DarkGray", 0xA9, 0xA9, 0xA9));
        colourList.add(new ColourName("DarkGreen", 0x00, 0x64, 0x00));
        colourList.add(new ColourName("DarkKhaki", 0xBD, 0xB7, 0x6B));
        colourList.add(new ColourName("DarkMagenta", 0x8B, 0x00, 0x8B));
        colourList.add(new ColourName("DarkOliveGreen", 0x55, 0x6B, 0x2F));
        colourList.add(new ColourName("DarkOrange", 0xFF, 0x8C, 0x00));
        colourList.add(new ColourName("DarkOrchid", 0x99, 0x32, 0xCC));
        colourList.add(new ColourName("DarkRed", 0x8B, 0x00, 0x00));
        colourList.add(new ColourName("DarkSalmon", 0xE9, 0x96, 0x7A));
        colourList.add(new ColourName("DarkSeaGreen", 0x8F, 0xBC, 0x8F));
        colourList.add(new ColourName("DarkSlateBlue", 0x48, 0x3D, 0x8B));
        colourList.add(new ColourName("DarkSlateGray", 0x2F, 0x4F, 0x4F));
        colourList.add(new ColourName("DarkTurquoise", 0x00, 0xCE, 0xD1));
        colourList.add(new ColourName("DarkViolet", 0x94, 0x00, 0xD3));
        colourList.add(new ColourName("DeepPink", 0xFF, 0x14, 0x93));
        colourList.add(new ColourName("DeepSkyBlue", 0x00, 0xBF, 0xFF));
        colourList.add(new ColourName("DimGray", 0x69, 0x69, 0x69));
        colourList.add(new ColourName("DodgerBlue", 0x1E, 0x90, 0xFF));
        colourList.add(new ColourName("FireBrick", 0xB2, 0x22, 0x22));
        colourList.add(new ColourName("FloralWhite", 0xFF, 0xFA, 0xF0));
        colourList.add(new ColourName("ForestGreen", 0x22, 0x8B, 0x22));
        colourList.add(new ColourName("Fuchsia", 0xFF, 0x00, 0xFF));
        colourList.add(new ColourName("Gainsboro", 0xDC, 0xDC, 0xDC));
        colourList.add(new ColourName("GhostWhite", 0xF8, 0xF8, 0xFF));
        colourList.add(new ColourName("Gold", 0xFF, 0xD7, 0x00));
        colourList.add(new ColourName("GoldenRod", 0xDA, 0xA5, 0x20));
        colourList.add(new ColourName("Gray", 0x80, 0x80, 0x80));
        colourList.add(new ColourName("Green", 0x00, 0x80, 0x00));
        colourList.add(new ColourName("GreenYellow", 0xAD, 0xFF, 0x2F));
        colourList.add(new ColourName("HoneyDew", 0xF0, 0xFF, 0xF0));
        colourList.add(new ColourName("HotPink", 0xFF, 0x69, 0xB4));
        colourList.add(new ColourName("IndianRed", 0xCD, 0x5C, 0x5C));
        colourList.add(new ColourName("Indigo", 0x4B, 0x00, 0x82));
        colourList.add(new ColourName("Ivory", 0xFF, 0xFF, 0xF0));
        colourList.add(new ColourName("Khaki", 0xF0, 0xE6, 0x8C));
        colourList.add(new ColourName("Lavender", 0xE6, 0xE6, 0xFA));
        colourList.add(new ColourName("LavenderBlush", 0xFF, 0xF0, 0xF5));
        colourList.add(new ColourName("LawnGreen", 0x7C, 0xFC, 0x00));
        colourList.add(new ColourName("LemonChiffon", 0xFF, 0xFA, 0xCD));
        colourList.add(new ColourName("LightBlue", 0xAD, 0xD8, 0xE6));
        colourList.add(new ColourName("LightCoral", 0xF0, 0x80, 0x80));
        colourList.add(new ColourName("LightCyan", 0xE0, 0xFF, 0xFF));
        colourList.add(new ColourName("LightGoldenRodYellow", 0xFA, 0xFA, 0xD2));
        colourList.add(new ColourName("LightGray", 0xD3, 0xD3, 0xD3));
        colourList.add(new ColourName("LightGreen", 0x90, 0xEE, 0x90));
        colourList.add(new ColourName("LightPink", 0xFF, 0xB6, 0xC1));
        colourList.add(new ColourName("LightSalmon", 0xFF, 0xA0, 0x7A));
        colourList.add(new ColourName("LightSeaGreen", 0x20, 0xB2, 0xAA));
        colourList.add(new ColourName("LightSkyBlue", 0x87, 0xCE, 0xFA));
        colourList.add(new ColourName("LightSlateGray", 0x77, 0x88, 0x99));
        colourList.add(new ColourName("LightSteelBlue", 0xB0, 0xC4, 0xDE));
        colourList.add(new ColourName("LightYellow", 0xFF, 0xFF, 0xE0));
        colourList.add(new ColourName("Lime", 0x00, 0xFF, 0x00));
        colourList.add(new ColourName("LimeGreen", 0x32, 0xCD, 0x32));
        colourList.add(new ColourName("Linen", 0xFA, 0xF0, 0xE6));
        colourList.add(new ColourName("Magenta", 0xFF, 0x00, 0xFF));
        colourList.add(new ColourName("Maroon", 0x80, 0x00, 0x00));
        colourList.add(new ColourName("MediumAquaMarine", 0x66, 0xCD, 0xAA));
        colourList.add(new ColourName("MediumBlue", 0x00, 0x00, 0xCD));
        colourList.add(new ColourName("MediumOrchid", 0xBA, 0x55, 0xD3));
        colourList.add(new ColourName("MediumPurple", 0x93, 0x70, 0xDB));
        colourList.add(new ColourName("MediumSeaGreen", 0x3C, 0xB3, 0x71));
        colourList.add(new ColourName("MediumSlateBlue", 0x7B, 0x68, 0xEE));
        colourList.add(new ColourName("MediumSpringGreen", 0x00, 0xFA, 0x9A));
        colourList.add(new ColourName("MediumTurquoise", 0x48, 0xD1, 0xCC));
        colourList.add(new ColourName("MediumVioletRed", 0xC7, 0x15, 0x85));
        colourList.add(new ColourName("MidnightBlue", 0x19, 0x19, 0x70));
        colourList.add(new ColourName("MintCream", 0xF5, 0xFF, 0xFA));
        colourList.add(new ColourName("MistyRose", 0xFF, 0xE4, 0xE1));
        colourList.add(new ColourName("Moccasin", 0xFF, 0xE4, 0xB5));
        colourList.add(new ColourName("NavajoWhite", 0xFF, 0xDE, 0xAD));
        colourList.add(new ColourName("Navy", 0x00, 0x00, 0x80));
        colourList.add(new ColourName("OldLace", 0xFD, 0xF5, 0xE6));
        colourList.add(new ColourName("Olive", 0x80, 0x80, 0x00));
        colourList.add(new ColourName("OliveDrab", 0x6B, 0x8E, 0x23));
        colourList.add(new ColourName("Orange", 0xFF, 0xA5, 0x00));
        colourList.add(new ColourName("OrangeRed", 0xFF, 0x45, 0x00));
        colourList.add(new ColourName("Orchid", 0xDA, 0x70, 0xD6));
        colourList.add(new ColourName("PaleGoldenRod", 0xEE, 0xE8, 0xAA));
        colourList.add(new ColourName("PaleGreen", 0x98, 0xFB, 0x98));
        colourList.add(new ColourName("PaleTurquoise", 0xAF, 0xEE, 0xEE));
        colourList.add(new ColourName("PaleVioletRed", 0xDB, 0x70, 0x93));
        colourList.add(new ColourName("PapayaWhip", 0xFF, 0xEF, 0xD5));
        colourList.add(new ColourName("PeachPuff", 0xFF, 0xDA, 0xB9));
        colourList.add(new ColourName("Peru", 0xCD, 0x85, 0x3F));
        colourList.add(new ColourName("Pink", 0xFF, 0xC0, 0xCB));
        colourList.add(new ColourName("Plum", 0xDD, 0xA0, 0xDD));
        colourList.add(new ColourName("PowderBlue", 0xB0, 0xE0, 0xE6));
        colourList.add(new ColourName("Purple", 0x80, 0x00, 0x80));
        colourList.add(new ColourName("Red", 0xFF, 0x00, 0x00));
        colourList.add(new ColourName("RosyBrown", 0xBC, 0x8F, 0x8F));
        colourList.add(new ColourName("RoyalBlue", 0x41, 0x69, 0xE1));
        colourList.add(new ColourName("SaddleBrown", 0x8B, 0x45, 0x13));
        colourList.add(new ColourName("Salmon", 0xFA, 0x80, 0x72));
        colourList.add(new ColourName("SandyBrown", 0xF4, 0xA4, 0x60));
        colourList.add(new ColourName("SeaGreen", 0x2E, 0x8B, 0x57));
        colourList.add(new ColourName("SeaShell", 0xFF, 0xF5, 0xEE));
        colourList.add(new ColourName("Sienna", 0xA0, 0x52, 0x2D));
        colourList.add(new ColourName("Silver", 0xC0, 0xC0, 0xC0));
        colourList.add(new ColourName("SkyBlue", 0x87, 0xCE, 0xEB));
        colourList.add(new ColourName("SlateBlue", 0x6A, 0x5A, 0xCD));
        colourList.add(new ColourName("SlateGray", 0x70, 0x80, 0x90));
        colourList.add(new ColourName("Snow", 0xFF, 0xFA, 0xFA));
        colourList.add(new ColourName("SpringGreen", 0x00, 0xFF, 0x7F));
        colourList.add(new ColourName("SteelBlue", 0x46, 0x82, 0xB4));
        colourList.add(new ColourName("Tan", 0xD2, 0xB4, 0x8C));
        colourList.add(new ColourName("Teal", 0x00, 0x80, 0x80));
        colourList.add(new ColourName("Thistle", 0xD8, 0xBF, 0xD8));
        colourList.add(new ColourName("Tomato", 0xFF, 0x63, 0x47));
        colourList.add(new ColourName("Turquoise", 0x40, 0xE0, 0xD0));
        colourList.add(new ColourName("Violet", 0xEE, 0x82, 0xEE));
        colourList.add(new ColourName("Wheat", 0xF5, 0xDE, 0xB3));
        colourList.add(new ColourName("White", 0xFF, 0xFF, 0xFF));
        colourList.add(new ColourName("WhiteSmoke", 0xF5, 0xF5, 0xF5));
        colourList.add(new ColourName("Yellow", 0xFF, 0xFF, 0x00));
        colourList.add(new ColourName("YellowGreen", 0x9A, 0xCD, 0x32));
        return colourList;
    }

    /**
     * Get the closest color name from our list
     *
     * @param r red
     * @param g green
     * @param b blue
     * @return name
     */
    public static String getColorNameFromRgb(int r, int g, int b) {
        ColourName closestMatch = null;
        int minMSE = Integer.MAX_VALUE;
        int mse;
        for (ColourName c : colourList) {
            mse = c.computeMSE(r, g, b);
            if (mse < minMSE) {
                minMSE = mse;
                closestMatch = c;
            }
        }

        if (closestMatch != null) {
            return closestMatch.getName();
        } else {
            return "No matched color name.";
        }
    }

    /**
     * Convert hexColor to rgb, then call getColorNameFromRgb(r, g, b)
     *
     * @param hexColor hex colour represented as an integer
     * @return colour name
     */
    public static String getColorNameFromHex(int hexColor) {
        int r = (hexColor & 0xFF0000) >> 16;
        int g = (hexColor & 0xFF00) >> 8;
        int b = (hexColor & 0xFF);
        return getColorNameFromRgb(r, g, b);
    }

    public static int colorToHex(Color c) {
        return Integer.decode("0x"
                + Integer.toHexString(c.getRGB()).substring(2));
    }

    public static String getColorNameFromColor(Color color) {
        return getColorNameFromRgb(color.getRed(), color.getGreen(),
                color.getBlue());
    }

    /**
     * SubClass of ColorUtils. In order to lookup color name
     *
     * @author Xiaoxiao Li
     */
    public static class ColourName {
        public final int r;
        public final int g;
        public final int b;
        public final String name;

        public ColourName(String name, int r, int g, int b) {
            this.r = r;
            this.g = g;
            this.b = b;
            this.name = name;
        }

        public int computeMSE(int pixR, int pixG, int pixB) {
            return ((pixR - r) * (pixR - r) + (pixG - g) * (pixG - g) + (pixB - b)
                    * (pixB - b)) / 3;
        }

        public int getR() {
            return r;
        }

        public int getG() {
            return g;
        }

        public int getB() {
            return b;
        }

        public String getName() {
            return name;
        }
    }
}
