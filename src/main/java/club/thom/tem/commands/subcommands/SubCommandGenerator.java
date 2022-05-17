package club.thom.tem.commands.subcommands;

import club.thom.tem.TEM;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SubCommandGenerator {

    public static List<SubCommand> getSubCommands(TEM main) {
        List<SubCommand> subCommands = new ArrayList<>();
        Reflections reflections = new Reflections("club.thom.tem.commands.subcommands");
        Set<Class<? extends SubCommand>> classes = reflections.getSubTypesOf(SubCommand.class);
        for (Class<? extends SubCommand> extensionClass : classes) {
            SubCommand subCommand = null;
            try {
                subCommand = extensionClass.getConstructor(TEM.class).newInstance(main);
            } catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException ignored) {
                try {
                    subCommand = (SubCommand) extensionClass.getConstructors()[0].newInstance();
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
            subCommands.add(subCommand);
        }
        return subCommands;
    }

}
