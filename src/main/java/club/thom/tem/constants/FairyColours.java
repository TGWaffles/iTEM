package club.thom.tem.constants;

import com.google.common.collect.ImmutableSet;

public class FairyColours {
    public static final ImmutableSet<String> fairyColourConstants = ImmutableSet.of(
            "330066", "4C0099", "660033", "660066", "6600CC", "7F00FF", "99004C", "990099", "9933FF", "B266FF",
            "CC0066", "CC00CC", "CC99FF", "E5CCFF", "FF007F", "FF00FF", "FF3399", "FF33FF", "FF66B2", "FF66FF",
            "FF99CC", "FF99FF", "FFCCE5", "FFCCFF"
    );

    public static boolean isFairyColour(String hex) {
        return fairyColourConstants.contains(hex.toUpperCase());
    }
}
