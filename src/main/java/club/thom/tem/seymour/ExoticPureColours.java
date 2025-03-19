package club.thom.tem.seymour;

import club.thom.tem.constants.PureColours;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.text.WordUtils;

import java.util.Map;

public class ExoticPureColours {
    static ImmutableMap<String, String> exoticPureColours = createExoticPureColours();

    public static ImmutableMap<String, String> createExoticPureColours() {
        ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
        for (Map.Entry<String, String> exoticPureEntry : PureColours.getPureColours().entrySet()) {
            builder.put(WordUtils.capitalizeFully("Exotic Pure " + exoticPureEntry.getValue()), exoticPureEntry.getKey());
        }
        return builder.build();
    }

    public static ImmutableMap<String, String> getExoticPureColours() {
        return exoticPureColours;
    }
}
