package vip.Resolute.command.impl;

import vip.Resolute.Resolute;
import vip.Resolute.command.Command;
import vip.Resolute.modules.Module;
import org.lwjgl.input.Keyboard;

public class Bind extends Command {

    public Bind() {
        super("Bind", "Binds a module by name.", "bind <name> <key> | clear", "b");
    }

    @Override
    public void onCommand(String[] args, String command) {
        if(args.length == 2) {
            String moduleName = args[0];
            String keyName = args[1];

            boolean foundModule = false;

            for(Module module : Resolute.modules) {
                if(module.name.equalsIgnoreCase(moduleName)) {
                    module.keyBind.setCode(Keyboard.getKeyIndex(keyName.toUpperCase()));

                    Resolute.addChatMessage(String.format("Bound %s to %s", module.name, Keyboard.getKeyName(module.getKey())));
                    foundModule = true;
                    break;
                }
            }

            if(!foundModule) {
                Resolute.addChatMessage("Could not find given module");
            }
        }

        if(args.length == 1) {
            if(args[0].equalsIgnoreCase("clear")) {
                for(Module module : Resolute.modules) {
                    module.keyBind.setCode(Keyboard.KEY_NONE);
                }
            }

            Resolute.addChatMessage("Cleared all keybinds");
        }
    }
}
