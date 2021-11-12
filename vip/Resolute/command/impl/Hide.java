package vip.Resolute.command.impl;

import vip.Resolute.Resolute;
import vip.Resolute.command.Command;
import vip.Resolute.modules.Module;
import net.minecraft.client.Minecraft;

public class Hide extends Command {

    public Minecraft mc = Minecraft.getMinecraft();

    public Hide() {
        super("Hide", "Hides modules / Unhides modules", ".hide <name> | .hide all", "hide");
    }

    @Override
    public void onCommand(String[] args, String command) {
        if(args.length > 0) {
            if(args[0].equalsIgnoreCase("all")) {
                for(Module module : Resolute.modules) {
                    module.setHidden(true);
                }
            }

            String moduleName = args[0];

            boolean foundModule = false;

            for(Module.Category c : Module.Category.values()) {
                if(c.name.equalsIgnoreCase(moduleName)) {
                    for(Module m : Resolute.getModulesByCategory(c)) {
                        m.setHidden(true);
                    }
                }
            }

            for(Module module : Resolute.modules) {
                if(module.name.equalsIgnoreCase(moduleName)) {
                    if(module.isHidden()) {
                        module.setHidden(false);
                    } else {
                        module.setHidden(true);
                    }


                    Resolute.addChatMessage((module.isHidden() ? "Hidden: " + moduleName : "Shown: " + moduleName));

                    foundModule = true;
                    break;
                }
            }

            if(!foundModule) {
                Resolute.addChatMessage("Could not locate given module");
            }
        }
    }
}
