package club.thom.tem.seymour;

import com.google.common.collect.ImmutableMap;

public class TruePureColours {
    static ImmutableMap<String, String> truePureColours = ImmutableMap.<String, String>builder()
            .put("Pure Red", "FF0000")
            .put("Pure Green", "00FF00")
            .put("Pure Yellow", "FFFF00")
            .put("Pure Cyan", "00FFFF")
            .put("Pure Pink", "FF00FF")
            .put("Pure White", "FFFFFF")
            .put("Pure Black", "000000")
            .build();

    public static ImmutableMap<String, String> getTruePureColours() {
        return truePureColours;
    }
}
