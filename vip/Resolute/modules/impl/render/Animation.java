package vip.Resolute.modules.impl.render;

import vip.Resolute.events.Event;
import vip.Resolute.events.impl.EventUpdate;
import vip.Resolute.modules.Module;
import vip.Resolute.settings.impl.BooleanSetting;
import vip.Resolute.settings.impl.ModeSetting;
import vip.Resolute.settings.impl.NumberSetting;

public class Animation extends Module {

    public static boolean enabled = false;

    public static ModeSetting mode = new ModeSetting("Mode", "Resolute", "Resolute", "Swing", "Swang", "Swong", "Swank", "Slide", "Old", "Astro", "Exhibition", "Exhibobo");

    /*
    public static ModeSetting mode = new ModeSetting("Mode", "Resolute", "1.7", "1.8", "Resolute",
            "Exhibition", "Ethereal", "Old", "Remix", "Gravity", "Twist", "Down", "Autumn", "ETB", "Sigma", "Astolfo",
            "Swank", "Swang", "Tap", "Tap2", "Smooth", "Astro", "Summer", "Light");

     */

    public static NumberSetting speed = new NumberSetting("Slowdown", 1, 0.4, 5, 0.1);

    public static NumberSetting scale = new NumberSetting("Scale", 0.4, 0.1, 1.0, 0.1);

    public static NumberSetting xPos = new NumberSetting("X", 0, -1, 1, 0.05);
    public static NumberSetting yPos = new NumberSetting("Y", 0, -1, 1, 0.05);
    public static NumberSetting zPos = new NumberSetting("Z", 0, -1, 1, 0.05);

    public static BooleanSetting onItem = new BooleanSetting("Position on Item", true);

    public static BooleanSetting smoothHitting = new BooleanSetting("Smooth Hitting", false);

    public Animation() {
        super("Animation", 0, "Renders different block animations", Category.RENDER);
        this.addSettings(mode, speed, scale, onItem, xPos, yPos, zPos, smoothHitting);
        toggled = true;

    }

    public void onEnable() {
        enabled = true;
    }

    public void onDisable() {
        enabled = false;
        this.toggle();
    }

    public void onEvent(Event e) {
        if(e instanceof EventUpdate) {
            this.setSuffix("");
        }
    }
}
