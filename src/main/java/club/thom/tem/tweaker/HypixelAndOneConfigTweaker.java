package club.thom.tem.tweaker;

import cc.polyfrost.oneconfig.loader.stage0.LaunchWrapperTweaker;
import net.hypixel.modapi.tweaker.HypixelModAPITweaker;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.fml.relauncher.CoreModManager;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

public class HypixelAndOneConfigTweaker extends LaunchWrapperTweaker {

    @Override
    public void injectIntoClassLoader(LaunchClassLoader classLoader) {
        super.injectIntoClassLoader(classLoader);

        @SuppressWarnings("unchecked")
        List<String> tweakClassNames = (List<String>) Launch.blackboard.get("TweakClasses");
        tweakClassNames.add(HypixelModAPITweaker.class.getName());
    }

    public File getModFile() {
        for (URL url : Launch.classLoader.getSources()) {
            try {
                URI uri = url.toURI();
                if (!"file".equals(uri.getScheme())) {
                    continue;
                }
                File file = new File(uri);
                if (!file.exists() || !file.isFile()) {
                    continue;
                }
                String tweakClass = null;
                String coreMod = null;
                boolean mixin = false;
                try (JarFile jar = new JarFile(file)) {
                    if (jar.getManifest() != null) {
                        Attributes attributes = jar.getManifest().getMainAttributes();
                        tweakClass = attributes.getValue("TweakClass");
                    }
                }
                if (this.getClass().getName().equalsIgnoreCase(tweakClass)) {
                    return file;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    protected boolean getNextInstance() {
        boolean output = super.getNextInstance();

        File modFile = getModFile();
        if (modFile == null) {
            System.out.println("iTEM: Failed to find mod file");
            return output;
        }
        try {
            // Forge will by default ignore a mod file if it contains a tweaker
            // So we need to remove ourselves from that exclusion list
            Field ignoredModFile = CoreModManager.class.getDeclaredField("ignoredModFiles");
            ignoredModFile.setAccessible(true);
            ((List<String>) ignoredModFile.get(null)).remove(modFile.getName());

            // And instead add ourselves to the mod candidate list
            CoreModManager.getReparseableCoremods().add(modFile.getName());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("iTEM: Failed to add mod file to Forge's mod candidate list");
        }

        return output;
    }
}
