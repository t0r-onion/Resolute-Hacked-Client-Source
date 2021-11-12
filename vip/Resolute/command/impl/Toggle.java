package vip.Resolute.command.impl;

import vip.Resolute.Resolute;
import vip.Resolute.command.Command;
import vip.Resolute.modules.Module;

public class Toggle extends Command {

    public Toggle() {
        super("Toggle", "Toggles a module by name.", "toggle <name>", "t");
    }

    @Override
    public void onCommand(String[] args, String command) {
        if(args.length > 0) {
            String moduleName = args[0];

            boolean foundModule = false;

            for(Module module : Resolute.modules) {
                if(module.name.equalsIgnoreCase(moduleName)) {
                    module.toggle();

                    Resolute.addChatMessage((module.isEnabled() ? "Enabled" : "Disabled") + " " + module.name);

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
