package vip.Resolute.modules.impl.combat;

import vip.Resolute.modules.Module;

public class DelayRemover extends Module {
    public static boolean enabled = false;

    public DelayRemover() {
        super("DelayRemover", 0, "Allows for 1.7 hit reg", Category.COMBAT);
    }

    public void onEnable() {
        enabled = true;
    }

    public void onDisable() {
        enabled = false;
    }
}
