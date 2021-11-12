package vip.Resolute.modules.impl.movement;

import vip.Resolute.events.Event;
import vip.Resolute.events.impl.EventUpdate;
import vip.Resolute.modules.Module;
import vip.Resolute.settings.impl.NumberSetting;

public class TimerSlowdown extends Module {

    public NumberSetting start = new NumberSetting("Start Time", 1.8, 0.1, 10, 0.1);
    public NumberSetting end = new NumberSetting("End Time", 1.0, 0.1, 10, 0.1);
    public NumberSetting speed = new NumberSetting("Speed", 1, 1, 20, 1);
    public NumberSetting delay = new NumberSetting("Delay", 3, 1, 20, 1);

    int ticks = 0;
    float currentTimer = 1;

    public TimerSlowdown() {
        super("TimerSlow", 0, "Slows timer over time", Category.MOVEMENT);
        this.addSettings(start, end, speed, delay);
    }

    public void onEnable() {
        currentTimer = (float) start.getValue();
        ticks = 0;
    }

    public void onDisable() {
        mc.timer.timerSpeed = 1.0f;
    }

    public void onEvent(Event e) {
        if(e instanceof EventUpdate) {
            ticks++;
            mc.timer.timerSpeed = currentTimer;

            if(currentTimer > end.getValue() && ticks == delay.getValue()) {
                currentTimer -= 0.05 * speed.getValue();
                ticks = 0;
            }
        }
    }
}
