package vip.Resolute.modules.impl.combat;

import vip.Resolute.modules.Module;
import vip.Resolute.settings.impl.NumberSetting;

public class Hitboxes extends Module {
    public static NumberSetting size = new NumberSetting("Size", 0.3, 0.1, 1.0, 0.1);

    public static boolean enabled = false;

    public Hitboxes() {
        super("Hitboxes", 0, "Expands entity hitboxes", Category.COMBAT);
        this.addSettings(size);
    }

    public void onEnable() {
        enabled = true;
    }

    public void onDisable() {
        enabled = false;
    }
}
