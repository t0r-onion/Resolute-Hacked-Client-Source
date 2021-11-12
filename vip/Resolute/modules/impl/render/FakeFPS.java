package vip.Resolute.modules.impl.render;

import vip.Resolute.modules.Module;
import vip.Resolute.settings.impl.BooleanSetting;
import vip.Resolute.settings.impl.ModeSetting;
import vip.Resolute.settings.impl.NumberSetting;

public class FakeFPS extends Module {
    public static boolean enabled = false;

    public static BooleanSetting fakefps = new BooleanSetting("FakeFPS", false);
    public static NumberSetting fakefpsnumber = new NumberSetting("FakeFPS Value", 1, 1, 100, 1);

    public FakeFPS() {
        super("FakeFPS", 0, "Fake FPS", Category.RENDER);
        this.addSettings(fakefpsnumber);
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
