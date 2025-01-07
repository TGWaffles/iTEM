package club.thom.tem.tweaker;

import gg.essential.loader.stage0.EssentialSetupTweaker;
import net.hypixel.modapi.tweaker.HypixelModAPITweaker;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;

import java.util.List;

public class HypixelAndEssentialTweaker extends EssentialSetupTweaker {

    @Override
    public void injectIntoClassLoader(LaunchClassLoader classLoader) {
        super.injectIntoClassLoader(classLoader);

        @SuppressWarnings("unchecked")
        List<String> tweakClassNames = (List<String>) Launch.blackboard.get("TweakClasses");
        tweakClassNames.add(HypixelModAPITweaker.class.getName());
    }
}
