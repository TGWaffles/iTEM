package club.thom.tem.constants;

import com.google.common.collect.ImmutableSet;

public class CrystalColours {
    public static final ImmutableSet<String> crystalColoursConstants = ImmutableSet.of(
            "1F0030", "46085E", "54146E", "5D1C78", "63237D", "6A2C82", "7E4196", "8E51A6", "9C64B3", "A875BD",
            "B88BC9", "C6A3D4", "D9C1E3", "E5D1ED", "EFE1F5", "FCF3FF"
    );

    public static boolean isCrystalColour(String hex) {
        return crystalColoursConstants.contains(hex.toUpperCase());
    }
}
