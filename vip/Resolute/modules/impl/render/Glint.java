package vip.Resolute.modules.impl.render;

import vip.Resolute.modules.Module;
import vip.Resolute.settings.impl.ColorSetting;

import java.awt.*;

public class Glint extends Module {

    public static ColorSetting color = new ColorSetting("Color", new Color(255, 0, 0));

    public static boolean enabled = false;

    public Glint() {
        super("Glint", 0, "Changes enchantment color", Category.RENDER);
        this.addSettings(color);
    }

    @Override
    public void onEnable() {
        enabled = true;
    }

    @Override
    public void onDisable() {
        enabled = false;
    }

    public static int getColor() {
        return new Color((int) color.getValue().getRed(), (int) color.getValue().getGreen(), (int) color.getValue().getBlue()).getRGB();
    }
}
