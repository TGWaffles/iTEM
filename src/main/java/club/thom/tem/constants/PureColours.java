package club.thom.tem.constants;

import com.google.common.collect.ImmutableSet;

public class PureColours {
    public static final ImmutableSet<String> pureColoursConstats = ImmutableSet.of(
            "993333", "D87F33", "E5E533", "7FCC19", "667F33", "6699D8", "4C7F99", "334CB2", "F27FA5", "7F3FB2",
            "B24CD8", "664C33", "FFFFFF", "999999", "4C4C4C", "191919"
    );

    public static boolean isPureColour(String hex) {
        return pureColoursConstats.contains(hex);
    }
}
