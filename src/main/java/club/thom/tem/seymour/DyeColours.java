package club.thom.tem.seymour;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

public class DyeColours {
    static ImmutableMap<String, String> dyes = ImmutableMap.<String, String>builder()
            .put("Aquamarine Dye", "7FFFD4")
            .put("Archfiend Dye", "B80036")
            .put("Bingo Blue Dye", "002FA7")
            .put("Bone Dye", "E3DAC9")
            .put("Brick Red Dye", "CB4154")
            .put("Byzantium Dye", "702963")
            .put("Carmine Dye", "960018")
            .put("Celadon Dye", "ACE1AF")
            .put("Celeste Dye", "B2FFFF")
            .put("Chocolate Dye", "7B3F00")
            .put("Copper Dye", "B87333")
            .put("Cyclamen Dye", "F56FA1")
            .put("Dark Purple Dye", "301934")
            .put("Dung Dye", "4F2A2A")
            .put("Emerald Dye", "50C878")
            .put("Flame Dye", "E25822")
            .put("Fossil Dye", "866F12")
            .put("Frostbitten Dye", "09D8EB")
            .put("Holly Dye", "3C6746")
            .put("Iceberg Dye", "71A6D2")
            .put("Jade Dye", "00A86B")
            .put("Livid Dye", "CEB7AA")
            .put("Mango Dye", "FDBE02")
            .put("Matcha Dye", "74A12E")
            .put("Midnight Dye", "50216C")
            .put("Mocha Dye", "967969")
            .put("Nadeshiko Dye", "F6ADC6")
            .put("Necron Dye", "E7413C")
            .put("Nyanza Dye", "E9FFDB")
            .put("Pearlescent Dye", "115555")
            .put("Pelt Dye", "50414C")
            .put("Periwinkle Dye", "CCCCFF")
            .put("Pure Black Dye", "000000")
            .put("Pure Blue Dye", "0013FF")
            .put("Pure White Dye", "FFFFFF")
            .put("Pure Yellow Dye", "FFF700")
            .put("Sangria Dye", "D40808")
            .put("Secret Dye", "7D7D7D")
            .put("Wild Strawberry Dye", "FF43A4")
            .put("Tentacle Dye", "324D6C")
            .put("BLEACHED", "A06540")
            .build();

    public static Map<String, String> getDyes() {
        return dyes;
    }
}
