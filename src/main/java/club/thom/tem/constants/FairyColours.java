package club.thom.tem.constants;

import com.google.common.collect.ImmutableSet;

public class FairyColours {
    public static final ImmutableSet<String> FairyoloursConstats = ImmutableSet.of(
            "660066","660033","99004C","CC0066","FF007F","FF3399","FF66B2","FF99CC","FFCCE5","FF99CC","FF66B2",
            "FF3399","FF007F","CC0066","99004C","660033","660066","990099","CC00CC","FF00FF","FF33FF","FF66FF",
            "FF99FF","FFCCFF","E5CCFF","CC99FF","B266FF","9933FF","7F00FF","9933FF","B266FF","CC99FF","E5CCFF",
            "FFCCFF","FF99FF","FF66FF","FF33FF","FF00FF","CC00CC","990099"
    );
    public static boolean isFairyColour(String Hex){
        return FairyoloursConstats.contains(Hex);
    }
}
