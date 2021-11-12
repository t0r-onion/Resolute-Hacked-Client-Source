package vip.Resolute.modules.impl.combat;

import vip.Resolute.events.Event;
import vip.Resolute.events.impl.EventTick;
import vip.Resolute.modules.Module;
import vip.Resolute.settings.impl.BooleanSetting;
import vip.Resolute.settings.impl.NumberSetting;
import vip.Resolute.util.world.RandomUtil;
import vip.Resolute.util.misc.TimerUtil;
import net.minecraft.client.Minecraft;

public class AutoClicker extends Module {

    public NumberSetting mincps = new NumberSetting("Min CPS", 9, 1, 20, 1);
    public NumberSetting maxcps = new NumberSetting("Max CPS", 12, 1, 20, 1);

    public BooleanSetting rightClick = new BooleanSetting("Right Click", false);

    TimerUtil timer = new TimerUtil();

    public AutoClicker() {
        super("AutoClicker", 0, "Automatically clicks fo you", Category.COMBAT);
        this.addSettings(mincps, maxcps, rightClick);
    }

    public void onEvent(Event e) {
        if(e instanceof EventTick) {
            if(rightClick.isEnabled() && mc.gameSettings.keyBindRight.isKeyDown()) {
                Minecraft.getMinecraft().rightClickMouse();
            } else if(mc.gameSettings.keyBindAttack.isKeyDown() && !mc.thePlayer.isUsingItem()) {
                double cps = RandomUtil.getRandomInRange(mincps.getValue(), maxcps.getValue());

                if(timer.hasElapsed((long) (1000 / cps))) {
                    Minecraft.getMinecraft().clickMouse();
                    this.timer.reset();
                }
            }
        }
    }
}

