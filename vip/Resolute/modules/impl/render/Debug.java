package vip.Resolute.modules.impl.render;

import vip.Resolute.modules.Module;

public class Debug extends Module {

    public static boolean enabled = false;

    public Debug() {
        super("Debug", 0, "", Category.RENDER);
    }

    public void onEnable() {
        enabled = true;
    }

    public void onDisable() {
        enabled = false;
    }
}
