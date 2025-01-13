package club.thom.tem.seymour;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

public class DyeColours {
    static ImmutableMap<String, String> dyes = ImmutableMap.<String, String>builder()
            .put("CARMINE_DYE", "960018")
            .put("NECRON_DYE", "E7413C")
            .put("FLAME_DYE", "E25822")
            .put("MANGO_DYE", "FDBE02")
            .put("NYANZA_DYE", "E9FFDB")
            .put("CELADON_DYE", "ACE1AF")
            .put("EMERALD_DYE", "50C878")
            .put("HOLLY_DYE", "3C6746")
            .put("AQUAMARINE_DYE", "7FFFD4")
            .put("CELESTE_DYE", "B2FFFF")
            .put("ICEBERG_DYE", "71A6D2")
            .put("TENTACLE_DYE", "324D6C")
            .put("BINGO_BLUE_DYE", "002FA7")
            .put("DARK_PURPLE_DYE", "301934")
            .put("MIDNIGHT_DYE", "702670")
            .put("BYZANTIUM_DYE", "702963")
            .put("CYCLAMEN_DYE", "F56FA1")
            .put("NADESHIKO_DYE", "F6ADC6")
            .put("WILD_STRAWBERRY_DYE", "FF43A4")
            .put("BRICK_RED_DYE", "CB4154")
            .put("BONE_DYE", "E3DAC9")
            .put("PURE_WHITE_DYE", "FFFFFF")
            .put("PURE_BLACK_DYE", "000000")
            .put("BLEACHED", "A06540")
            .build();

    public static Map<String, String> getDyes() {
        return dyes;
    }
}
