package vip.Resolute.modules.impl.render;

import vip.Resolute.events.Event;
import vip.Resolute.events.impl.EventMotion;
import vip.Resolute.modules.Module;
import vip.Resolute.settings.impl.ModeSetting;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

public class FullBright extends Module {
    public ModeSetting mode = new ModeSetting("Mode", "Gamma", "Gamma", "Potion");

    public FullBright() {
        super("FullBright", 0, "Renders the world at full brightness", Category.RENDER);
        this.addSetting(mode);
    }

    public void onDisable() {
        super.onDisable();
        if(mode.is("Gamma")) {
            mc.gameSettings.gammaSetting = 1f;
        }
        if(mode.is("Potion")) {
            if (mc.thePlayer.isPotionActive(Potion.nightVision) ) {
                mc.thePlayer.removePotionEffect(Potion.nightVision.getId());
            }
        }
    }

    public void onEvent(Event e) {
        this.setSuffix(mode.getSelected());
        if(e instanceof EventMotion) {
            if(mode.is("Gamma")) {
                if (mc.gameSettings.gammaSetting == 1f || mc.gameSettings.gammaSetting < 1f) {
                    mc.gameSettings.gammaSetting = 100f;
                }
                if (mc.thePlayer.isPotionActive(Potion.nightVision)) {
                    mc.thePlayer.removePotionEffect(Potion.nightVision.getId());
                }
            }
            if(mode.is("Potion")) {
                if (mc.gameSettings.gammaSetting > 1f) {
                    mc.gameSettings.gammaSetting = 1f;
                }
                mc.thePlayer.addPotionEffect(new PotionEffect(Potion.nightVision.getId(), 5200, 1));
            }
        }
    }
}
