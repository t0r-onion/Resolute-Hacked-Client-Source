package vip.Resolute.modules.impl.render;

import vip.Resolute.modules.Module;

public class RearView extends Module {

    public static boolean enabled = false;

    public RearView() {
        super("RearView", 0, "", Category.RENDER);
    }

    public void onEnable() {
        enabled = true;
    }

    public void onDisable() {
        enabled = false;
    }
}
