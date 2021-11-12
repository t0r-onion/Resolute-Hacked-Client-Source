package vip.Resolute.modules.impl.render;

import vip.Resolute.modules.Module;
import vip.Resolute.settings.impl.ColorSetting;

import java.awt.*;

public class Ghost extends Module {
    public static ColorSetting color = new ColorSetting("Color", new Color(192, 0, 255));
    public Ghost() {
        super("Ghost", 0, "Renders a ghost of the player", Category.RENDER);
        this.addSettings(color);
    }

    public static boolean enabled = false;

    public void onEnable() { enabled = true; }
    public void onDisable() { enabled = false; }
}
