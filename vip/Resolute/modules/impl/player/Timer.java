package vip.Resolute.modules.impl.player;

import vip.Resolute.events.Event;
import vip.Resolute.events.impl.EventUpdate;
import vip.Resolute.modules.Module;
import vip.Resolute.settings.impl.NumberSetting;

public class Timer extends Module {

    public NumberSetting timer = new NumberSetting("Timer", 1.0D, 0.1D, 5.0D, 0.1D);

    public Timer() {
        super("Timer", 0, "Changes world timer", Category.PLAYER);
        this.addSettings(timer);
    }

    public void onDisable() {
        super.onDisable();

        mc.timer.timerSpeed = 1.0F;
    }

    public void onEvent(Event e) {
        this.setSuffix("Speed: " + timer.getValue());

        if(e instanceof EventUpdate)
            mc.timer.timerSpeed = (float) timer.getValue();
    }
}

