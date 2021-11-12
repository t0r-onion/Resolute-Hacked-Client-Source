package vip.Resolute.command.impl;

import vip.Resolute.Resolute;
import vip.Resolute.command.Command;
import vip.Resolute.modules.Module;
import net.minecraft.client.Minecraft;

public class Unhide extends Command {

    public Minecraft mc = Minecraft.getMinecraft();

    public Unhide() {
        super("Unhide", "Unhides all", ".unhide all", "unhide");
    }

    @Override
    public void onCommand(String[] args, String command) {
        if(args.length > 0) {
                for(Module module : Resolute.modules) {
                    module.setHidden(false);
                }
        }
    }
}
