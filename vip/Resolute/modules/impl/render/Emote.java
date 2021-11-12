package vip.Resolute.modules.impl.render;

import vip.Resolute.modules.Module;
import vip.Resolute.settings.impl.ModeSetting;

public class Emote extends Module {
    public static boolean enabled = false;

    public static ModeSetting mode = new ModeSetting("Mode", "Dab", "Dab", "Zombie", "Panic", "Bedrock", "Autist");

    public Emote() {
        super("Emote", 0, "Haha funny emote", Category.PLAYER);
        this.addSettings(mode);
    }

    @Override
    public void onEnable() {
        enabled = true;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        enabled = false;
        super.onDisable();
    }
}
