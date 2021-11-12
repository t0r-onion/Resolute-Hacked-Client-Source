package vip.Resolute.modules.impl.render;

import vip.Resolute.modules.Module;
import vip.Resolute.settings.impl.ColorSetting;

import java.awt.*;

public class WorldColor extends Module {
    public static ColorSetting lightMapColorProperty = new ColorSetting("Color", new Color(-32640));

    public static boolean enabled = false;

    public WorldColor() {
        super("WorldColor", 0, "", Category.RENDER);
        this.addSettings(lightMapColorProperty);
    }

    public void onEnable() {
        enabled = true;
    }

    public void onDisable() {
        enabled = false;
    }
}
